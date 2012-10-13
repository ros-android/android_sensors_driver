package org.ros.android.android_sensors_driver;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.http.util.ByteArrayBuffer;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.CvBoost;
import org.ros.internal.message.MessageBuffers;
import org.ros.message.Time;
import org.ros.namespace.NameResolver;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

import android.app.NativeActivity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Log;
import android.view.SurfaceHolder;

class Sample2View extends SampleCvViewBase {
    private Mat mRgba;
    private Mat mGray;
    private Mat mIntermediateMat;
    
    private double stats[][];
    private int counter;
    private int numSamples = 50;
    private Time oldTime;
    private Time newTime;
        
    private ByteBuffer bb;
    
    private Bitmap bmp;
    
    private static final String TAG = "CICCIO::Sample2View";
    
    private Publisher<sensor_msgs.CompressedImage> imagePublisher;
    private Publisher<sensor_msgs.Image> rawImagePublisher;
    private final Publisher<sensor_msgs.CameraInfo> cameraInfoPublisher;
    
    private ChannelBufferOutputStream stream;
    
    sensor_msgs.CameraInfo cameraInfo;
    private final ConnectedNode connectedNode;

    public Sample2View(Context context, ConnectedNode connectedNode)
    {
        super(context);
        
        this.connectedNode = connectedNode;
        Log.i(TAG,"Constructor 1");
        NameResolver resolver = null;
      	resolver = connectedNode.getResolver().newChild("camera");
        Log.i(TAG,"Constructor 2");
        this.imagePublisher = connectedNode.newPublisher(resolver.resolve("image/compressed"), sensor_msgs.CompressedImage._TYPE);
        this.cameraInfoPublisher = connectedNode.newPublisher(resolver.resolve("camera_info"), sensor_msgs.CameraInfo._TYPE);
        Log.i(TAG,"Constructor 3");
        this.rawImagePublisher = connectedNode.newPublisher(resolver.resolve("image/raw"), sensor_msgs.Image._TYPE);
        Log.i(TAG,"Constructor 4");
        stream = new ChannelBufferOutputStream(MessageBuffers.dynamicBuffer());
        Log.i(TAG,"Constructor 5");
        bmp = null;
        bb = null;
        
        stats = new double[10][numSamples];

        oldTime = connectedNode.getCurrentTime();
        Log.i(TAG,"Constructor 6");
        counter = 0;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        synchronized (this)
        {
            // initialize Mats before usage
            mGray = new Mat();
            mRgba = new Mat();
            mIntermediateMat = new Mat();
        }
        
        super.surfaceCreated(holder);
    }

    @Override
    protected Bitmap processFrame(VideoCapture capture)
    {
    	Time[] measureTime = new Time[9];
    	String[] compDescStrings = {"Total processFrame","Grab a new frame","MatToBitmap","Publish cameraInfo",
    								"Create ImageMsg","Compress image","Transfer to Stream","Image.SetData","Publish Image","Total econds per frame"};
    	String[] rawDescStrings = { "Total processFrame","Grab a new frame","MatToBitmap","Publish cameraInfo",
									"Create ImageMsg","Pixel to buffer","Transfer to Stream","Image.SetData","Publish Image","Total seconds per frame"};
    	
    	measureTime[0] = connectedNode.getCurrentTime();
    	
        switch (MainActivity.viewMode)
        {
	        case MainActivity.VIEW_MODE_GRAY:
//	            capture.retrieve(mGray, Highgui.CV_CAP_ANDROID_GREY_FRAME);
	            capture.retrieve(mRgba, Highgui.CV_CAP_ANDROID_GREY_FRAME);
//	            Imgproc.cvtColor(mGray, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
	            break;
	        case MainActivity.VIEW_MODE_RGBA:
	            capture.retrieve(mRgba, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
	//            Core.putText(mRgba, "OpenCV + Android", new Point(10, 100), 3, 2, new Scalar(255, 0, 0, 255), 3);
	            break;
	        case MainActivity.VIEW_MODE_CANNY:
	            capture.retrieve(mGray, Highgui.CV_CAP_ANDROID_GREY_FRAME);
	            Imgproc.Canny(mGray, mIntermediateMat, 80, 100);
	            Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2BGRA, 4);
				break;
        }
        Time currentTime = connectedNode.getCurrentTime();
        
//        Log.i(TAG,"Mat size: " + mRgba.size() + "\tDepth: " + mRgba.channels());
        
        measureTime[1] = connectedNode.getCurrentTime();
        
        if(bmp == null)
        	bmp = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);

        if(MainActivity.imageCompression == MainActivity.IMAGE_TRANSPORT_COMPRESSION_NONE && bb == null)
        {
        	bb = ByteBuffer.allocate(bmp.getRowBytes()*bmp.getHeight());
        	bb.clear();
        }
        try
        {
        	Utils.matToBitmap(mRgba, bmp);
        	measureTime[2] = connectedNode.getCurrentTime();
        	
        	cameraInfo = cameraInfoPublisher.newMessage();
            cameraInfo.getHeader().setFrameId("camera");
            cameraInfo.getHeader().setStamp(currentTime);
            cameraInfo.setWidth(640);
            cameraInfo.setHeight(480);
            cameraInfoPublisher.publish(cameraInfo);
            measureTime[3] = connectedNode.getCurrentTime();
            
            if(MainActivity.imageCompression >= MainActivity.IMAGE_TRANSPORT_COMPRESSION_PNG)
            {
            	//Compressed image
            	
            	sensor_msgs.CompressedImage image = imagePublisher.newMessage();
	            if(MainActivity.imageCompression == MainActivity.IMAGE_TRANSPORT_COMPRESSION_PNG)
	            	image.setFormat("png");
	            else if(MainActivity.imageCompression == MainActivity.IMAGE_TRANSPORT_COMPRESSION_JPEG)
	            	image.setFormat("jpeg");
	            image.getHeader().setStamp(currentTime);
	            image.getHeader().setFrameId("camera");
	            measureTime[4] = connectedNode.getCurrentTime();
	
	        	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	            if(MainActivity.imageCompression == MainActivity.IMAGE_TRANSPORT_COMPRESSION_PNG)
	            	bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
	            else if(MainActivity.imageCompression == MainActivity.IMAGE_TRANSPORT_COMPRESSION_JPEG)
	            	bmp.compress(Bitmap.CompressFormat.JPEG, MainActivity.imageCompressionQuality, baos);
	        	measureTime[5] = connectedNode.getCurrentTime();
	
	        	stream.buffer().writeBytes(baos.toByteArray());
	        	measureTime[6] = connectedNode.getCurrentTime();
	
	        	image.setData(stream.buffer().copy());
	        	measureTime[7] = connectedNode.getCurrentTime();
	
	            stream.buffer().clear();
	        	imagePublisher.publish(image);
	        	measureTime[8] = connectedNode.getCurrentTime();
            }
            else
            {
	        	// Raw image
		        	
	            sensor_msgs.Image rawImage = rawImagePublisher.newMessage();
	            rawImage.getHeader().setStamp(currentTime);
	            rawImage.getHeader().setFrameId("camera");
	            rawImage.setEncoding("rgba8");
	            rawImage.setWidth(bmp.getWidth());
	            rawImage.setHeight(bmp.getHeight());
	            rawImage.setStep(640);
	            measureTime[4] = connectedNode.getCurrentTime();
		
	            bmp.copyPixelsToBuffer(bb);
	            measureTime[5] = connectedNode.getCurrentTime();
	
	            stream.buffer().writeBytes(bb.array());
	            bb.clear();
	            measureTime[6] = connectedNode.getCurrentTime();
	
	        	rawImage.setData(stream.buffer().copy());
	        	stream.buffer().clear();
	        	measureTime[7] = connectedNode.getCurrentTime();
	
	            rawImagePublisher.publish(rawImage);
	            measureTime[8] = connectedNode.getCurrentTime();
	            
            }
            
            newTime = connectedNode.getCurrentTime();
            stats[9][counter] = (newTime.subtract(oldTime)).nsecs/1000000.0;
            oldTime = newTime;

        	for(int i=1;i<9;i++)
        	{
        		stats[i][counter] = (measureTime[i].subtract(measureTime[i-1])).nsecs/1000000.0;
        	}
        	
        	
        	stats[0][counter] = measureTime[8].subtract(measureTime[0]).nsecs/1000000.0;
        	
        	counter++;
        	if(counter == numSamples)
        	{
        		double[] sts = new double[10];
        		Arrays.fill(sts, 0.0);
        		
        		for(int i=0;i<10;i++)
            	{
        			for(int j=0;j<numSamples;j++)
        				sts[i] += stats[i][j];
        			
        			sts[i] /= (double)numSamples;
        			
        			if(MainActivity.imageCompression >= MainActivity.IMAGE_TRANSPORT_COMPRESSION_PNG)
        				Log.i(TAG,String.format("Mean time for %s:\t\t%4.2fms", compDescStrings[i], sts[i]));
        			else
        				Log.i(TAG,String.format("Mean time for %s:\t\t%4.2fms", rawDescStrings[i], sts[i]));
            	}
        		Log.i(TAG,"\n\n");
        		counter = 0;
        	}
        	
        	
        	
            return bmp;
        } catch(Exception e)
        {
        	Log.e(TAG, "Frame conversion and publishing throws an exception: " + e.getMessage());
            bmp.recycle();
            return null;
        }
    }

    @Override
    public void run() {
        super.run();

        synchronized (this) {
            // Explicitly deallocate Mats
            if (mRgba != null)
                mRgba.release();
            if (mGray != null)
                mGray.release();
            if (mIntermediateMat != null)
                mIntermediateMat.release();

            mRgba = null;
            mGray = null;
            mIntermediateMat = null;
        }
    }
}

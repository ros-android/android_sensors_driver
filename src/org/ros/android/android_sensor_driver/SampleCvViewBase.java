package org.ros.android.android_sensor_driver;

import java.util.List;

import org.opencv.core.Size;
import org.opencv.highgui.VideoCapture;
import org.opencv.highgui.Highgui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public abstract class SampleCvViewBase extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private static final String TAG = "CICCIO::SurfaceView";

    private SurfaceHolder       mHolder;
    private VideoCapture        mCamera;

    public SampleCvViewBase(Context context) {
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this);
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    public boolean openCamera() {
        Log.i(TAG, "openCamera");
        synchronized (this) {
	        releaseCamera();
	        mCamera = new VideoCapture(Highgui.CV_CAP_ANDROID);
	        if (!mCamera.isOpened()) {
	            mCamera.release();
	            mCamera = null;
	            Log.e(TAG, "Failed to open native camera");
	            return false;
	        }
	    }
        return true;
    }
    
    public void releaseCamera() {
        Log.i(TAG, "releaseCamera");
        synchronized (this) {
	        if (mCamera != null) {
	                mCamera.release();
	                mCamera = null;
            }
        }
    }
    
    public void setupCamera(int width, int height) {
        Log.i(TAG, "setupCamera out("+width+", "+height+")");
        synchronized (this) {
            if (mCamera != null && mCamera.isOpened())
            {
                List<Size> sizes = mCamera.getSupportedPreviewSizes();
                int mFrameWidth = width;
                int mFrameHeight = height;

                // selecting optimal camera preview size
                {
                    double minDiff = Double.MAX_VALUE;
                    for (Size size : sizes) {
                        if ((Math.abs(size.height - height) + Math.abs(size.width - width))< minDiff) {
                            mFrameWidth = (int) size.width;
                            mFrameHeight = (int) size.height;
                            minDiff = Math.abs(size.height - height);
                        }
                    }
                }


                Log.i(TAG, "setupCamera 1");
                mCamera.set(Highgui.CV_CAP_PROP_ANDROID_ANTIBANDING, Highgui.CV_CAP_ANDROID_ANTIBANDING_OFF);
                Log.i(TAG, "setupCamera 2");
                mCamera.set(Highgui.CV_CAP_PROP_ANDROID_FLASH_MODE, Highgui.CV_CAP_ANDROID_FLASH_MODE_OFF);
                Log.i(TAG, "setupCamera 3");
                mCamera.set(Highgui.CV_CAP_PROP_ANDROID_FOCUS_MODE, Highgui.CV_CAP_ANDROID_FOCUS_MODE_CONTINUOUS_VIDEO);
                Log.i(TAG, "setupCamera 4");
                mCamera.set(Highgui.CV_CAP_PROP_ANDROID_WHITE_BALANCE, Highgui.CV_CAP_ANDROID_WHITE_BALANCE_FLUORESCENT);
                Log.i(TAG, "setupCamera 5");
//                mCamera.set(Highgui.CV_CAP_PROP_IOS_DEVICE_EXPOSURE,
//                Log.i(TAG, "setupCamera 6: " + mCamera.get(Highgui.CV_CAP_PROP_IOS_DEVICE_EXPOSURE));
                
                mCamera.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, mFrameWidth);
                mCamera.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, mFrameHeight);
            }
        }

    }
    
    public void surfaceChanged(SurfaceHolder _holder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged");
//        setupCamera(width, height);
        setupCamera(640,480);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated");
        (new Thread(this)).start();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed");
        releaseCamera();
    }

    protected abstract Bitmap processFrame(VideoCapture capture);
//    protected abstract void processFrame(VideoCapture capture);

    public void run() {
        Log.i(TAG, "Starting processing thread");
        while (true) {
            Bitmap bmp = null;

            synchronized (this) {
                if (mCamera == null)
                    break;

                if (!mCamera.grab()) {
                    Log.e(TAG, "mCamera.grab() failed");
                    break;
                }

                bmp = processFrame(mCamera);
//                processFrame(mCamera);
            }

            if (bmp != null) {
                Canvas canvas = mHolder.lockCanvas();
                if (canvas != null)
                {
                    canvas.drawBitmap(bmp, (canvas.getWidth() - bmp.getWidth()) / 2, (canvas.getHeight() - bmp.getHeight()) / 2, null);
                    mHolder.unlockCanvasAndPost(canvas);
                }
//                bmp.recycle();
            }
        }

        Log.i(TAG, "Finishing processing thread");
    }
}
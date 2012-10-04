/*
 * Copyright (c) 2011, Chad Rockey
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Android Sensors Driver nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.ros.android.android_sensor_driver;

//import geometry_msgs.Vector3;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.ros.node.ConnectedNode;
import org.ros.message.Time;
import org.ros.namespace.GraphName;
import sensor_msgs.Imu;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;

//import org.opencv.android.BaseLoaderCallback;
//import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

/**
 * @author chadrockey@gmail.com (Chad Rockey)
 */
public class CameraPublisher implements NodeMain
{
	private Sample2View mView;
	private Activity mainActivity;
	private static final String TAG = "CICCIO::CameraPublisher";
	private ConnectedNode node = null;
	private Publisher<sensor_msgs.CompressedImage> cameraPublisher;
	private Publisher<sensor_msgs.CameraInfo> cameraInfoPublisher;
	
	
	
	private BaseLoaderCallback  mOpenCVCallBack= new BaseLoaderCallback(mainActivity)
	  {
	  	@SuppressWarnings("deprecation")
		@Override
	  	public void onManagerConnected(int status)
	  	{
//	  		Log.i(TAG, "onManagerConnected 1");
	  		
	  		switch (status)
	  		{
					case LoaderCallbackInterface.SUCCESS:
					{
//						Log.i(TAG, "OpenCV loaded successfully");
						// Create and set View
//						Log.i(TAG, "onManagerConnected 2");
//						mView = new Sample2View(mAppContext, cameraPublisher, cameraInfoPublisher, node);
//						mView = new Sample2View(mainActivity, cameraPublisher, cameraInfoPublisher, node);
						mView = new Sample2View(mainActivity, null, null, node);
//						Log.i(TAG, "onManagerConnected 3");
						mainActivity.setContentView(mView);
//						mAppContext.setContentView(mView);
//						Log.i(TAG, "onManagerConnected 4");
						// Check native OpenCV camera
						if( !mView.openCamera() )
						{
//							Log.i(TAG, "onManagerConnected CAN'T OPEN CAMERA");
//							AlertDialog ad = new AlertDialog.Builder(mAppContext).create();
							AlertDialog ad = new AlertDialog.Builder(mainActivity).create();
							ad.setCancelable(false); // This blocks the 'BACK' button
							ad.setMessage("Fatal error: can't open camera!");
							ad.setButton("OK", new DialogInterface.OnClickListener() {
							    public void onClick(DialogInterface dialog, int which)
							    {
							    	dialog.dismiss();
//							    	mAppContext.finish();
							    	mainActivity.finish();
							    }
							});
							ad.show();
						}
					} break;
					default:
					{
//						Log.i(TAG, "OpenCV loading FAIL!!!");
						super.onManagerConnected(status);
					} break;
				}
//	  		Log.i(TAG, "onManagerConnected 5");
	  	}
		};
	
	
//		= new BaseLoaderCallback(mainActivity) {
//	  	@SuppressWarnings("deprecation")
//			@Override
//	  	public void onManagerConnected(int status) {
//	  		switch (status) {
//					case LoaderCallbackInterface.SUCCESS:
//					{
//						Log.i(TAG, "OpenCV loaded successfully");
//						// Create and set View
//						mView = new Sample2View(mAppContext, publisher, connectedNode);
//						mAppContext.setContentView(mView);
//						// Check native OpenCV camera
//						if( !mView.openCamera() ) {
//							AlertDialog ad = new AlertDialog.Builder(mAppContext).create();
//							ad.setCancelable(false); // This blocks the 'BACK' button
//							ad.setMessage("Fatal error: can't open camera!");
//							ad.setButton("OK", new DialogInterface.OnClickListener() {
//							    public void onClick(DialogInterface dialog, int which) {
//								dialog.dismiss();
//								mAppContext.finish();
//							    }
//							});
//							ad.show();
//						}
//					} break;
//					default:
//					{
//						super.onManagerConnected(status);
//					} break;
//				}
//	  	}
//		};
	
//  private ImuThread imuThread;
//  private SensorListener sensorListener;
//  private SensorManager sensorManager;
//  private Publisher<Imu> publisher;
//  private Publisher<sensor_msgs.CompressedImage> cameraPublisher;
//  private Publisher<sensor_msgs.CameraInfo> cameraInfoPublisher;
  
//  private class ImuThread extends Thread
//  {
//	  private final SensorManager sensorManager;
//	  private SensorListener sensorListener;
//	  private Looper threadLooper;
//	  
//	  private final Sensor accelSensor;
//	  private final Sensor gyroSensor;
//	  private final Sensor quatSensor;
//	  
//	  private ImuThread(SensorManager sensorManager, SensorListener sensorListener)
//	  {
//		  this.sensorManager = sensorManager;
//		  this.sensorListener = sensorListener;
//		  this.accelSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//		  this.gyroSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
//		  this.quatSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
//	  }
//	  
//	    
//	  public void run()
//	  {
//			Looper.prepare();
//			this.threadLooper = Looper.myLooper();
//			this.sensorManager.registerListener(this.sensorListener, this.accelSensor, SensorManager.SENSOR_DELAY_FASTEST);
//			this.sensorManager.registerListener(this.sensorListener, this.gyroSensor, SensorManager.SENSOR_DELAY_FASTEST);
//			this.sensorManager.registerListener(this.sensorListener, this.quatSensor, SensorManager.SENSOR_DELAY_FASTEST);
//			Looper.loop();
//	  }
//	    
//	    
//	  public void shutdown()
//	  {
//	    	this.sensorManager.unregisterListener(this.sensorListener);
//	    	if(this.threadLooper != null)
//	    	{
//	            this.threadLooper.quit();
//	    	}
//	  }
//	}
  
//  private class SensorListener implements SensorEventListener
//  {
//
//    private Publisher<Imu> publisher;
//    
//    private boolean hasAccel;
//    private boolean hasGyro;
//    private boolean hasQuat;
//    
//    private long accelTime;
//    private long gyroTime;
//    private long quatTime;
//    
//    private Imu imu;
//
//    private SensorListener(Publisher<Imu> publisher, boolean hasAccel, boolean hasGyro, boolean hasQuat)
//    {
//      this.publisher = publisher;
//      this.hasAccel = hasAccel;
//      this.hasGyro = hasGyro;
//      this.hasQuat = hasQuat;
//      this.accelTime = 0;
//      this.gyroTime = 0;
//      this.quatTime = 0;
//      this.imu = this.publisher.newMessage();
////      this.imu = new Imu(); //Use Sensor Factory
//    }
//
////	@Override
//	public void onAccuracyChanged(Sensor sensor, int accuracy)
//	{
//	}
//
////	@Override
//	public void onSensorChanged(SensorEvent event)
//	{
//		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
//		{
//			this.imu.getLinearAcceleration().setX(event.values[0]);
//			this.imu.getLinearAcceleration().setY(event.values[1]);
//			this.imu.getLinearAcceleration().setZ(event.values[2]);
////	        this.imu.linear_acceleration.x = event.values[0];
////	        this.imu.linear_acceleration.y = event.values[1];
////	        this.imu.linear_acceleration.z = event.values[2];
//			
//			double[] tmpCov = {0.01,0,0, 0,0.01,0, 0,0,0.01};
//			this.imu.setLinearAccelerationCovariance(tmpCov);
////	        this.imu.linear_acceleration_covariance[0] = 0.01; // TODO Make Parameter
////	        this.imu.linear_acceleration_covariance[4] = 0.01;
////	        this.imu.linear_acceleration_covariance[8] = 0.01;
//			this.accelTime = event.timestamp;
//		}
//		else if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE)
//		{
//			this.imu.getAngularVelocity().setX(event.values[0]);
//			this.imu.getAngularVelocity().setY(event.values[1]);
//			this.imu.getAngularVelocity().setZ(event.values[2]);
////	        this.imu.angular_velocity.x = event.values[0];
////	        this.imu.angular_velocity.y = event.values[1];
////	        this.imu.angular_velocity.z = event.values[2];
//			double[] tmpCov = {0.0025,0,0, 0,0.0025,0, 0,0,0.0025};
//			this.imu.setAngularVelocityCovariance(tmpCov);
////	        this.imu.angular_velocity_covariance[0] = 0.0025; // TODO Make Parameter
////	        this.imu.angular_velocity_covariance[4] = 0.0025;
////	        this.imu.angular_velocity_covariance[8] = 0.0025;
//	        this.gyroTime = event.timestamp;
//		}
//		else if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR)
//		{
//	        float[] quaternion = new float[4];
//	        SensorManager.getQuaternionFromVector(quaternion, event.values);
//	        this.imu.getOrientation().setW(quaternion[0]);
//	        this.imu.getOrientation().setX(quaternion[1]);
//	        this.imu.getOrientation().setY(quaternion[2]);
//	        this.imu.getOrientation().setZ(quaternion[3]);
////		    this.imu.orientation.w = quaternion[0];
////	        this.imu.orientation.x = quaternion[1];
////	        this.imu.orientation.y = quaternion[2];
////	       	this.imu.orientation.z = quaternion[3];
//			double[] tmpCov = {0.001,0,0, 0,0.001,0, 0,0,0.001};
//			this.imu.setOrientationCovariance(tmpCov);
////	       	this.imu.orientation_covariance[0] = 0.001; // TODO Make Parameter
////	       	this.imu.orientation_covariance[4] = 0.001;
////	       	this.imu.orientation_covariance[8] = 0.001;
//	       	this.quatTime = event.timestamp;
//		}
//		
//		// Currently storing event times in case I filter them in the future.  Otherwise they are used to determine if all sensors have reported.
//		if((this.accelTime != 0 || !this.hasAccel) && (this.gyroTime != 0 || !this.hasGyro) && (this.quatTime != 0 || !this.hasQuat))
//		{
//			// Convert event.timestamp (nanoseconds uptime) into system time, use that as the header stamp
//			long time_delta_millis = System.currentTimeMillis() - SystemClock.uptimeMillis();
//			this.imu.getHeader().setStamp(Time.fromMillis(time_delta_millis + event.timestamp/1000000));
////			this.imu.header.stamp = Time.fromMillis(time_delta_millis + event.timestamp/1000000);
//			this.imu.getHeader().setFrameId("/imu");
////			this.imu.header.frame_id = "/imu"; // TODO Make parameter
//			
//			publisher.publish(this.imu);
//			
//			// Reset times
//			this.accelTime = 0;
//			this.gyroTime = 0;
//			this.quatTime = 0;
//		}
//	}
//  }

  @SuppressWarnings("deprecation")
public void resume()
  {
//	  Log.i(TAG, "resume 1");
		if((null != mView) && !mView.openCamera() )
		{
//			Log.i(TAG, "resume 2");
			AlertDialog ad = new AlertDialog.Builder(mainActivity).create();  
			ad.setCancelable(false); // This blocks the 'BACK' button  
			ad.setMessage("Fatal error: can't open camera!");  
//			Log.i(TAG, "resume 3");
			ad.setButton("OK", new DialogInterface.OnClickListener()
			{  
			    public void onClick(DialogInterface dialog, int which)
			    {  
			        dialog.dismiss();
			        mainActivity.finish();
			    }  
			});
//			Log.i(TAG, "resume 4");
			ad.show();
//			Log.i(TAG, "resume 5");
		}
//		Log.i(TAG, "resume 6");
  }
  
  public void releaseCamera()
  {
	  if (null != mView)
		  mView.releaseCamera();
  }
  
  public CameraPublisher(Activity mainAct)
  {
	  this.mainActivity = mainAct;
//	  this.sensorManager = manager;
//      Log.i(TAG, "Trying to load OpenCV library");
      if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this.mainActivity, mOpenCVCallBack))
      {
    	  Log.e(TAG, "Cannot connect to OpenCV Manager");
      }
//      else
//    	  Log.i(TAG, "OpenCV library succesfully loaded!");
  }

  public GraphName getDefaultNodeName()
  {
	    return GraphName.of("android_sensor_driver/cameraPublisher");
  }
  
  public void onError(Node node, Throwable throwable)
  {
  }

  public void onStart(final ConnectedNode node)
  {
//	  Log.i(TAG, "onStart 1");
	  this.node = node;
//	  this.cameraPublisher = node.newPublisher("android/camera", sensor_msgs.CompressedImage._TYPE);
//	  this.cameraInfoPublisher = node.newPublisher("camera_info", sensor_msgs.CameraInfo._TYPE);
//	  Log.i(TAG, "onStart 2");
//	  mOpenCVCallBack = new BaseLoaderCallback(mainActivity)
//	  {
//		  	@SuppressWarnings("deprecation")
//			@Override
//		  	public void onManagerConnected(int status)
//		  	{
//		  		Log.i(TAG, "onManagerConnected 1");
//		  		switch (status)
//		  		{
//						case LoaderCallbackInterface.SUCCESS:
//						{
//							Log.i(TAG, "OpenCV loaded successfully");
//							// Create and set View
//							mView = new Sample2View(mAppContext, cameraPublisher, cameraInfoPublisher, node);
//							mAppContext.setContentView(mView);
//							// Check native OpenCV camera
//							if( !mView.openCamera() ) {
//								AlertDialog ad = new AlertDialog.Builder(mAppContext).create();
//								ad.setCancelable(false); // This blocks the 'BACK' button
//								ad.setMessage("Fatal error: can't open camera!");
//								ad.setButton("OK", new DialogInterface.OnClickListener() {
//								    public void onClick(DialogInterface dialog, int which) {
//									dialog.dismiss();
//									mAppContext.finish();
//								    }
//								});
//								ad.show();
//							}
//						} break;
//						default:
//						{
//							Log.i(TAG, "OpenCV FAIL loading successfully");
//							super.onManagerConnected(status);
//						} break;
//					}
//		  	}
//			};
	  
//	  try
//	  {
//			this.publisher = node.newPublisher("android/camera", sensor_msgs.CompressedImage._TYPE);
			
//			this.sensorListener = new SensorListener(publisher, hasAccel, hasGyro, hasQuat);
//			this.imuThread = new ImuThread(this.sensorManager, sensorListener);
//			this.imuThread.start();
			
			
//			this.mView = new Sample2View(mAppContext);
//			this.imuThread.start();
//	  }
//	  catch (Exception e)
//	  {
//		  if (node != null)
//		  {
//			  node.getLog().fatal(e);
//		  }
//		  else
//		  {
//			  e.printStackTrace();OpenCV loaded successfully
//		  }
//	  }
  }

//@Override
  public void onShutdown(Node arg0)
  {
//	  this.imuThread.shutdown();
//	
//	  try
//	  {
//		  this.imuThread.join();
//	  }
//	  catch (InterruptedException e)
//	  {
//		  e.printStackTrace();
//	  }
  }

//@Override
  public void onShutdownComplete(Node arg0)
  {
  }

}


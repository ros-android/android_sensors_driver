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

package org.ros.android.android_sensors_driver;

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
import android.widget.Toast;

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
//	private AlertDialog ad;
	
	
	private BaseLoaderCallback  mOpenCVCallBack = new BaseLoaderCallback(mainActivity)
	{
	  	@SuppressWarnings("deprecation")
		@Override
	  	public void onManagerConnected(int status)
	  	{
	  		Log.i(TAG, "onManagerConnected 1");
	  		
	  		switch (status)
	  		{
					case LoaderCallbackInterface.SUCCESS:
					{
						// Create and set View
						if(node == null)
						{
//							Log.i(TAG, "onManagerConnected 2");
//							Toast toast = Toast.makeText(mainActivity.getApplication().getApplicationContext(), "Fatal error: Wrong RosMaster URI", Toast.LENGTH_LONG);
//							toast.show();
//							Log.i(TAG, "onManagerConnected 3");

							
							Log.i(TAG, "onManagerConnected Node NULL");
//							
//							ad.setCancelable(false); // This blocks the 'BACK' button
//							ad.setMessage("Fatal error: Wrong RosMaster URI");
//							ad.setButton("OK", new DialogInterface.OnClickListener() {
//							    public void onClick(DialogInterface dialog, int which)
//							    {
//							    	dialog.dismiss();
////							    	mainActivity.finish();
//							    }
//							});
//							ad.show();
							
							
							try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								Log.e(TAG,e.toString());
							}
							System.exit(-1);
						}
						mView = new Sample2View(mainActivity, node);
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
	
	

  @SuppressWarnings("deprecation")
public void resume()
  {
		AlertDialog ad = new AlertDialog.Builder(mainActivity).create();  
		ad.setCancelable(false); // This blocks the 'BACK' button  
		ad.setMessage("Cicciopasticcio!");
		ad.setButton("OK", new DialogInterface.OnClickListener()
		{  
		    public void onClick(DialogInterface dialog, int which)
		    {  
		        dialog.dismiss();
		        mainActivity.finish();
		    }  
		});
		ad.show();
		if((null != mView) && !mView.openCamera() )
		{
//			AlertDialog ad = new AlertDialog.Builder(mainActivity).create();  
//			ad.setCancelable(false); // This blocks the 'BACK' button  
//			ad.setMessage("Fatal error: can't open camera!");
//			ad.setButton("OK", new DialogInterface.OnClickListener()
//			{  
//			    public void onClick(DialogInterface dialog, int which)
//			    {  
//			        dialog.dismiss();
//			        mainActivity.finish();
//			    }  
//			});
//			ad.show();
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
	  
//	  ad = new AlertDialog.Builder(mainAct).create();
	  
//      if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this.mainActivity, mOpenCVCallBack))
//      {
//    	  Log.e(TAG, "Cannot connect to OpenCV Manager");
//      }
//      else
//    	  Log.i(TAG, "OpenCV library succesfully loaded!");
  }

  public GraphName getDefaultNodeName()
  {
	    return GraphName.of("android_sensors_driver/cameraPublisher");
  }
  
  public void onError(Node node, Throwable throwable)
  {
  }

  public void onStart(final ConnectedNode node)
  {
	  Log.i(TAG, "onStart 1: " + node);
	  this.node = node;
	  
      if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this.mainActivity, mOpenCVCallBack))
      {
    	  Log.e(TAG, "Cannot connect to OpenCV Manager");
      }
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


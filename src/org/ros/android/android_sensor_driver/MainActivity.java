/*
 * Copyright (C) 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.android.android_sensor_driver;

import java.net.URI;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

//import org.opencv.android.BaseLoaderCallback;
//import org.opencv.android.LoaderCallbackInterface;
//import org.opencv.android.OpenCVLoader;

import org.ros.address.InetAddressFactory;

//import org.ros.android.MessageCallable;
import org.ros.android.RosActivity;
//import org.ros.android.view.RosTextView;
import org.ros.android.view.camera.RosCameraPreviewView;
import org.ros.master.uri.MasterUriProvider;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;
//import org.ros.rosjava_tutorial_pubsub.Talker;

/**
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MainActivity extends RosActivity {

//  private int cameraId;
//  private RosCameraPreviewView rosCameraPreviewView;
  
  
  private NavSatFixPublisher fix_pub;
  private ImuPublisher imu_pub;
  
  private LocationManager mLocationManager;
  private SensorManager mSensorManager;
  
  
  

  // OpenCV Camera
  private static final String TAG             = "CICCIO::MainActivity";

  public static final int     VIEW_MODE_RGBA  = 0;
  public static final int     VIEW_MODE_GRAY  = 1;
  public static final int     VIEW_MODE_CANNY = 2;
  
  public static final int     IMAGE_TRANSPORT_COMPRESSION_NONE = 0;
  public static final int     IMAGE_TRANSPORT_COMPRESSION_PNG = 1;
  public static final int     IMAGE_TRANSPORT_COMPRESSION_JPEG = 2;

  private MenuItem            mItemPreviewRGBA;
  private MenuItem            mItemPreviewGray;
  private MenuItem            mItemPreviewCanny;
  private MenuItem            mItemCompressionNone;
  private MenuItem            mItemCompressionPng;
  private MenuItem            mItemCompressionJpeg;

  public static int           viewMode        = VIEW_MODE_RGBA;
  public static int			  imageCompression = IMAGE_TRANSPORT_COMPRESSION_JPEG;
  public static int			  imageCompressionQuality = 80;
  
//  private Sample2View 		mView;
  private CameraPublisher cam_pub;
  
  
//  private BaseLoaderCallback  mOpenCVCallBack = new BaseLoaderCallback(this) {
//  	@SuppressWarnings("deprecation")
//		@Override
//  	public void onManagerConnected(int status) {
//  		switch (status) {
//				case LoaderCallbackInterface.SUCCESS:
//				{
//					Log.i(TAG, "OpenCV loaded successfully");
//					// Create and set View
//					mView = new Sample2View(mAppContext);
//					setContentView(mView);
//					// Check native OpenCV camera
//					if( !mView.openCamera() ) {
//						AlertDialog ad = new AlertDialog.Builder(mAppContext).create();
//						ad.setCancelable(false); // This blocks the 'BACK' button
//						ad.setMessage("Fatal error: can't open camera!");
//						ad.setButton("OK", new DialogInterface.OnClickListener() {
//						    public void onClick(DialogInterface dialog, int which) {
//							dialog.dismiss();
//							finish();
//						    }
//						});
//						ad.show();
//					}
//				} break;
//				default:
//				{
//					super.onManagerConnected(status);
//				} break;
//			}
//  	}
//	};

  public MainActivity() {
    super("SensorDriver", "SensorDriver");
//	  Log.i(TAG, "Constructor");
  }
  
  @Override
  protected void onPause()
  {
//	  Log.i(TAG, "onPause");
	  super.onPause();
	  if(cam_pub != null)
	  {
//			Log.i(TAG, "onPause NOT-NULL");
			cam_pub.releaseCamera();
	  }
//	  else
//			Log.i(TAG, "onPause NULL");
	  
	  
//	  cam_pub.releaseCamera();
//	  if (null != mView)
//		  mView.releaseCamera();
  }
  
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
//	  Log.i(TAG, "onCreate");
	  super.onCreate(savedInstanceState);
	  requestWindowFeature(Window.FEATURE_NO_TITLE);
	  getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	  setContentView(R.layout.main);
	    
//      Log.i(TAG, "Trying to load OpenCV library");
//      if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this, mOpenCVCallBack))
//      {
//    	  Log.e(TAG, "Cannot connect to OpenCV Manager");
//      }
	    
//	    rosCameraPreviewView = (RosCameraPreviewView)findViewById(R.id.ros_camera_preview_view);
		mLocationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
		mSensorManager = (SensorManager)this.getSystemService(SENSOR_SERVICE);
  }
  
	
//  @SuppressWarnings("deprecation")
  @Override
  protected void onResume()
  {
//      Log.i(TAG, "onResume 1");
		super.onResume();
//		Log.i(TAG, "onResume 2");
		if(cam_pub != null)
		{
//			Log.i(TAG, "onResume NOT-NULL");
			cam_pub.resume();
		}
//		else
//			Log.i(TAG, "onResume NULL");
		
		
//		if((null != mView) && !mView.openCamera() )
//		{
//			AlertDialog ad = new AlertDialog.Builder(this).create();  
//			ad.setCancelable(false); // This blocks the 'BACK' button  
//			ad.setMessage("Fatal error: can't open camera!");  
//			ad.setButton("OK", new DialogInterface.OnClickListener()
//			{  
//			    public void onClick(DialogInterface dialog, int which)
//			    {  
//			        dialog.dismiss();
//					finish();
//			    }  
//			});  
//			ad.show();
//		}
	}

//  @SuppressLint({ "ShowToast", "ShowToast" })
//@Override
//  public boolean onTouchEvent(MotionEvent event) {
//    if (event.getAction() == MotionEvent.ACTION_UP) {
//      int numberOfCameras = Camera.getNumberOfCameras();
//      final Toast toast;
//      if (numberOfCameras > 1) {
//        cameraId = (cameraId + 1) % numberOfCameras;
//        rosCameraPreviewView.releaseCamera();
//        rosCameraPreviewView.setCamera(Camera.open(cameraId));
//        toast = Toast.makeText(this, "Switching cameras.", Toast.LENGTH_SHORT);
//      } else {
//        toast = Toast.makeText(this, "No alternative cameras to switch to.", Toast.LENGTH_SHORT);
//      }
//      runOnUiThread(new Runnable() {
////        @Override
//        public void run() {
//          toast.show();
//        }
//      });
//    }
//    return true;
//  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

	  SubMenu subPreview = menu.addSubMenu("Color settings");
      mItemPreviewRGBA = subPreview.add(1,1,0,"RGB Color");
      mItemPreviewGray = subPreview.add(1,2,0,"Grayscale");
      mItemPreviewCanny = subPreview.add(1,3,0,"Canny edges");
      mItemPreviewRGBA.setChecked(true);
      subPreview.setGroupCheckable(1, true, true);
      
      SubMenu subCompression = menu.addSubMenu("Compression");
      mItemCompressionNone = subCompression.add(2,4,0,"None");
      mItemCompressionPng = subCompression.add(2,5,0,"Png");
      subCompression.setGroupCheckable(2, true, true);
      
      SubMenu subCompressionRate = subCompression.addSubMenu(2,6,0,"Jpeg");
      subCompressionRate.setHeaderTitle("Compression quality");
      subCompressionRate.getItem().setChecked(true);
      subCompressionRate.add(3,50,0,"50");
      subCompressionRate.add(3,60,0,"60");
      subCompressionRate.add(3,70,0,"70");
      subCompressionRate.add(3,80,0,"80").setChecked(true);
      subCompressionRate.add(3,90,0,"90");
      subCompressionRate.add(3,100,0,"100");
      subCompressionRate.setGroupCheckable(3, true, true);

      return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
//		  Log.i(TAG, "Menu Item selected " + item.getTitle() + " - " + item.getGroupId() + " - " + item.getItemId());
      if (item == mItemPreviewRGBA)
          viewMode = VIEW_MODE_RGBA;
      else if (item == mItemPreviewGray)
          viewMode = VIEW_MODE_GRAY;
      else if (item == mItemPreviewCanny)
          viewMode = VIEW_MODE_CANNY;
      else if (item == mItemCompressionNone)
    	  imageCompression = IMAGE_TRANSPORT_COMPRESSION_NONE;
      else if (item == mItemCompressionPng)
    	  imageCompression = IMAGE_TRANSPORT_COMPRESSION_PNG;
      
      if(item.getGroupId() == 3)
      {
    	  imageCompressionQuality = item.getItemId();
    	  imageCompression = IMAGE_TRANSPORT_COMPRESSION_JPEG;
      }
      return true;
  }

  @Override
  protected void init(NodeMainExecutor nodeMainExecutor)
  {
//    cameraId = 0;
//    rosCameraPreviewView.setCamera(Camera.open(cameraId));
//    NodeConfiguration nodeConfiguration1 = NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
//    nodeConfiguration1.setMasterUri(getMasterUri());
//    nodeMainExecutor.execute(rosCameraPreviewView, nodeConfiguration1);
	  
//    NodeConfiguration nodeConfiguration2 = NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
//    nodeConfiguration2.setMasterUri(getMasterUri());
//    nodeConfiguration2.setNodeName("android_sensor_driver_nav_sat_fix");
//    this.fix_pub = new NavSatFixPublisher(mLocationManager);
//    nodeMainExecutor.execute(this.fix_pub, nodeConfiguration2);
   
    NodeConfiguration nodeConfiguration3 = NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
//    nodeConfiguration3.setMasterUri(getMasterUri());
    nodeConfiguration3.setMasterUri(URI.create("http://192.168.1.213:11311/"));
    nodeConfiguration3.setNodeName("android_sensors_driver_imu");
    this.imu_pub = new ImuPublisher(mSensorManager);
    nodeMainExecutor.execute(this.imu_pub, nodeConfiguration3);
    
    NodeConfiguration nodeConfiguration4 = NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
//    nodeConfiguration4.setMasterUri(getMasterUri());
    nodeConfiguration4.setMasterUri(URI.create("http://192.168.1.213:11311/"));
    nodeConfiguration4.setNodeName("android_sensors_driver_camera");
    this.cam_pub = new CameraPublisher(this);
    nodeMainExecutor.execute(this.cam_pub, nodeConfiguration4);
    
  }
}
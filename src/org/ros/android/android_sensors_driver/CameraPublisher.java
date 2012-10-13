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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.ros.node.ConnectedNode;
import org.ros.namespace.GraphName;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.opencv.android.OpenCVLoader;

/**
 * @author chadrockey@gmail.com (Chad Rockey)
 * @author axelfurlan@gmail.com (Axel Furlan)
 */

public class CameraPublisher implements NodeMain
{
	private Sample2View mView;
	private Activity mainActivity;
	private static final String TAG = "SENSORS::CameraPublisher";
	private ConnectedNode node = null;	

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
						mView = new Sample2View(mainActivity, node);
						mainActivity.setContentView(mView);

						// Check native OpenCV camera
						if( !mView.openCamera() )
						{
							AlertDialog ad = new AlertDialog.Builder(mainActivity).create();
							ad.setCancelable(false); // This blocks the 'BACK' button
							ad.setMessage("Fatal error: can't open camera!");
							ad.setButton("OK", new DialogInterface.OnClickListener() {
							    public void onClick(DialogInterface dialog, int which)
							    {
							    	dialog.dismiss();
							    	mainActivity.finish();
							    }
							});
							ad.show();
						}
					} break;
					default:
					{
						Log.i(TAG, "OpenCV loading FAIL!!!");
						super.onManagerConnected(status);
					} break;
				}
	  	}
		};
	
	

  @SuppressWarnings("deprecation")
  public void resume()
  {
		if((null != mView) && !mView.openCamera() )
		{
			AlertDialog ad1 = new AlertDialog.Builder(mainActivity).create();  
			ad1.setCancelable(false); // This blocks the 'BACK' button  
			ad1.setMessage("Fatal error: can't open camera!");
			ad1.setButton("OK", new DialogInterface.OnClickListener()
			{  
			    public void onClick(DialogInterface dialog, int which)
			    {  
			        dialog.dismiss();
			        mainActivity.finish();
			    }  
			});
			ad1.show();
		}
  }
  
  public void releaseCamera()
  {
	  if (null != mView)
		  mView.releaseCamera();
  }
  
  public CameraPublisher(Activity mainAct)
  {
	  this.mainActivity = mainAct;
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
	  this.node = node;
	  
      if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this.mainActivity, mOpenCVCallBack))
      {
    	  Log.e(TAG, "Cannot connect to OpenCV Manager");
      }
  }

//@Override
  public void onShutdown(Node arg0)
  {
  }

//@Override
  public void onShutdownComplete(Node arg0)
  {
  }

}


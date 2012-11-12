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

package org.ros.android.android_sensors_driver;

import android.content.Context;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.Window;
import android.view.WindowManager;
import org.ros.address.InetAddressFactory;
import org.ros.android.RosActivity;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

/**
 * @author chadrockey@gmail.com (Chad Rockey)
 * @author axelfurlan@gmail.com (Axel Furlan)
 */


public class MainActivity extends RosActivity
{
  
  
  private NavSatFixPublisher fix_pub;
  private ImuPublisher imu_pub;
  
  private LocationManager mLocationManager;
  private SensorManager mSensorManager;
  

  public MainActivity()
  {
	  super("Ros Android Sensors Driver", "Ros Android Sensors Driver");
  }
  
  @Override
  protected void onPause()
  {
	  super.onPause();
  }
  
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
	  super.onCreate(savedInstanceState);
	  //requestWindowFeature(Window.FEATURE_NO_TITLE);
	  //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	  setContentView(R.layout.main);
	  
	  mLocationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
	  mSensorManager = (SensorManager)this.getSystemService(SENSOR_SERVICE);
  }
  
  @Override
  protected void onResume()
  {
		super.onResume();
	}

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

	  /*SubMenu subPreview = menu.addSubMenu("Color settings");
      subPreview.add(1,VIEW_MODE_RGBA,0,"RGB Color").setChecked(true);
      subPreview.add(1,VIEW_MODE_GRAY,0,"Grayscale");
      subPreview.add(1,VIEW_MODE_CANNY,0,"Canny edges");
      subPreview.setGroupCheckable(1, true, true);
      
      SubMenu subCompression = menu.addSubMenu("Compression");
//      subCompression.add(2,IMAGE_TRANSPORT_COMPRESSION_NONE,0,"None");
      subCompression.add(2,IMAGE_TRANSPORT_COMPRESSION_PNG,0,"Png");
      
      SubMenu subCompressionRate = subCompression.addSubMenu(2,IMAGE_TRANSPORT_COMPRESSION_JPEG,0,"Jpeg");
      subCompression.setGroupCheckable(2, true, true);
      subCompressionRate.setHeaderTitle("Compression quality");
      subCompressionRate.getItem().setChecked(true);
      subCompressionRate.add(3,50,0,"50");
      subCompressionRate.add(3,60,0,"60");
      subCompressionRate.add(3,70,0,"70");
      subCompressionRate.add(3,80,0,"80").setChecked(true);
      subCompressionRate.add(3,90,0,"90");
      subCompressionRate.add(3,100,0,"100");
      subCompressionRate.setGroupCheckable(3, true, true);*/

      return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {      
      /*if(item.getGroupId() == 1)
      {
    	  viewMode = item.getItemId();
    	  item.setChecked(true);
      }
      
      if(item.getGroupId() == 2)
      {
    	  imageCompression = item.getItemId();
    	  item.setChecked(true);
      }
      
      if(item.getGroupId() == 3)
      {
    	  imageCompressionQuality = item.getItemId();
    	  item.setChecked(true);
      }*/
      return true;
  }

  @Override
  protected void init(NodeMainExecutor nodeMainExecutor)
  {	  
    NodeConfiguration nodeConfiguration2 = NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
    nodeConfiguration2.setMasterUri(getMasterUri());
    nodeConfiguration2.setNodeName("android_sensors_driver_nav_sat_fix");
    this.fix_pub = new NavSatFixPublisher(mLocationManager);
    nodeMainExecutor.execute(this.fix_pub, nodeConfiguration2);
	  
    NodeConfiguration nodeConfiguration3 = NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
    nodeConfiguration3.setMasterUri(getMasterUri());
    nodeConfiguration3.setNodeName("android_sensors_driver_imu");
    this.imu_pub = new ImuPublisher(mSensorManager);
    nodeMainExecutor.execute(this.imu_pub, nodeConfiguration3);
    
  }
}

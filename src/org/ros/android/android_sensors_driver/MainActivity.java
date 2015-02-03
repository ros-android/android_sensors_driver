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

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.common.base.Preconditions;

import org.ros.address.InetAddressFactory;
import org.ros.android.RosActivity;
import org.ros.exception.RosRuntimeException;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author chadrockey@gmail.com (Chad Rockey)
 * @author axelfurlan@gmail.com (Axel Furlan)
 */


public class MainActivity extends RosActivity {
    protected final int MASTER_CHOOSER_REQUEST_CODE = 1;
    protected final int currentApiVersion = android.os.Build.VERSION.SDK_INT;

    protected NavSatFixPublisher fix_pub;
    protected ImuPublisher imu_pub;
    protected MagneticFieldPublisher magnetic_field_pub;
    protected FluidPressurePublisher fluid_pressure_pub;
    protected IlluminancePublisher illuminance_pub;
    protected TemperaturePublisher temperature_pub;
    protected LocationManager mLocationManager;
    protected SensorManager mSensorManager;
    protected String robotName;


    public MainActivity() {
        super("ROS Sensors Driver", "ROS Sensors Driver");
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);


    }

    @Override
    public void startMasterChooser() {
        Preconditions.checkState(getMasterUri() == null);
        // Call this method on super to avoid triggering our precondition in the
        // overridden startActivityForResult().
        Intent intent = new Intent(this, MasterChooser.class);
        super.startActivityForResult(intent, MASTER_CHOOSER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == MASTER_CHOOSER_REQUEST_CODE) {
                if (data.getBooleanExtra("ROS_MASTER_CREATE_NEW", false)) {
                    nodeMainExecutorService.startMaster(data.getBooleanExtra("ROS_MASTER_PRIVATE", true));
                }
                else {
                    URI uri;
                    try {
                        uri = new URI(data.getStringExtra("ROS_MASTER_URI"));
                        robotName = data.getStringExtra("ROBOT_NAME");
                    }
                    catch (URISyntaxException e) {
                        throw new RosRuntimeException(e);
                    }
                    nodeMainExecutorService.setMasterUri(uri);
                }
                // Run init() in a new thread as a convenience since it often requires network access.
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        MainActivity.this.init(nodeMainExecutorService);
                        return null;
                    }
                }.execute();
            }
            else {
                // Without a master URI configured, we are in an unusable state.
                nodeMainExecutorService.shutdown();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) //API = 15
    protected void init(NodeMainExecutor nodeMainExecutor) {
        URI masterURI = getMasterUri();



        int sensorDelay = 10000; // 10,000 us == 100 Hz for Android 3.1 and above
        if (currentApiVersion <= android.os.Build.VERSION_CODES.HONEYCOMB) {
            sensorDelay = SensorManager.SENSOR_DELAY_UI; // 16.7Hz for older devices.  They only support enum values, not the microsecond version.
        }


        int tempSensor;
        if (currentApiVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

            tempSensor = Sensor.TYPE_AMBIENT_TEMPERATURE; // Use newer temperature if possible
        }
        else
        {
            //noinspection deprecation
            tempSensor = Sensor.TYPE_TEMPERATURE; // Older temperature
        }



        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
        nodeConfiguration.setMasterUri(masterURI);
        nodeConfiguration.setNodeName("android_sensors_driver_magnetic_field");
        this.magnetic_field_pub = new MagneticFieldPublisher(mSensorManager, sensorDelay, robotName);
        nodeMainExecutor.execute(this.magnetic_field_pub, nodeConfiguration);

        NodeConfiguration nodeConfiguration2 = NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
        nodeConfiguration2.setMasterUri(masterURI);
        nodeConfiguration2.setNodeName("android_sensors_driver_nav_sat_fix");
        this.fix_pub = new NavSatFixPublisher(mLocationManager, robotName);
        nodeMainExecutor.execute(this.fix_pub, nodeConfiguration2);

        NodeConfiguration nodeConfiguration3 = NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
        nodeConfiguration3.setMasterUri(masterURI);
        nodeConfiguration3.setNodeName("android_sensors_driver_imu");
        this.imu_pub = new ImuPublisher(mSensorManager, sensorDelay, robotName);
        nodeMainExecutor.execute(this.imu_pub, nodeConfiguration3);

        NodeConfiguration nodeConfiguration4 = NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
        nodeConfiguration4.setMasterUri(masterURI);
        nodeConfiguration4.setNodeName("android_sensors_driver_pressure");
        this.fluid_pressure_pub = new FluidPressurePublisher(mSensorManager, sensorDelay, robotName);
        nodeMainExecutor.execute(this.fluid_pressure_pub, nodeConfiguration4);

        NodeConfiguration nodeConfiguration5 = NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
        nodeConfiguration5.setMasterUri(masterURI);
        nodeConfiguration5.setNodeName("android_sensors_driver_illuminance");
        this.illuminance_pub = new IlluminancePublisher(mSensorManager, sensorDelay, robotName);
        nodeMainExecutor.execute(this.illuminance_pub, nodeConfiguration5);

        NodeConfiguration nodeConfiguration6 = NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
        nodeConfiguration6.setMasterUri(masterURI);
        nodeConfiguration6.setNodeName("android_sensors_driver_temperature");
        this.temperature_pub = new TemperaturePublisher(mSensorManager, sensorDelay, tempSensor, robotName);
        nodeMainExecutor.execute(this.temperature_pub, nodeConfiguration6);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_help) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle(getResources().getString(R.string.help_title));
            builder.setMessage(getResources().getString(R.string.help_message));
            builder.setInverseBackgroundForced(true);
            builder.setNegativeButton(getResources().getString(R.string.help_ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.setNeutralButton(getResources().getString(R.string.help_wiki),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            Uri u = Uri.parse("http://www.ros.org/wiki/android_sensors_driver");
                            try {
                                // Start the activity
                                i.setData(u);
                                startActivity(i);
                            } catch (ActivityNotFoundException e) {
                                // Raise on activity not found
                                Toast toast = Toast.makeText(MainActivity.this, "Browser not found.", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }
                    });
            builder.setPositiveButton(getResources().getString(R.string.help_report),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            Uri u = Uri.parse("https://github.com/ros-android/android_sensors_driver/issues/new");
                            try {
                                // Start the activity
                                i.setData(u);
                                startActivity(i);
                            } catch (ActivityNotFoundException e) {
                                // Raise on activity not found
                                Toast toast = Toast.makeText(MainActivity.this, "Browser not found.", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();

        }
        return super.onOptionsItemSelected(item);
    }
}

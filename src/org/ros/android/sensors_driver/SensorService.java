package org.ros.android.sensors_driver;

import java.net.URI;
import java.net.URISyntaxException;

import org.ros.address.InetAddressFactory;
import org.ros.node.DefaultNodeRunner;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeRunner;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

public class SensorService extends Service {
	private final NodeRunner nodeRunner;
    
    private NavSatFixPublisher fix_pub;
    private ImuPublisher imu_pub;
    
    private LocationManager mLocationManager;
    private SensorManager mSensorManager;
    
    public SensorService(){
        nodeRunner = DefaultNodeRunner.newDefault();
    }

    public void onCreate() { // Intent intent, NodeConfiguration nodeConfiguration
    	mLocationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
    	mSensorManager = (SensorManager)this.getSystemService(SENSOR_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
        
        // We use this bundle
        Bundle b = intent.getExtras();
    	
        if(this.fix_pub == null){
	    	try {
			  URI masterUri = new URI(b.getString("masterUri"));
			  NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
		      nodeConfiguration.setMasterUri(masterUri);
		      nodeConfiguration.setNodeName("android_sensors_driver_nav_sat_fix");
		      this.fix_pub = new NavSatFixPublisher(mLocationManager);
		      this.nodeRunner.run(this.fix_pub, nodeConfiguration);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
        }
        
        if(this.imu_pub == null){
	    	try {
			  URI masterUri = new URI(b.getString("masterUri"));
			  NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress());
		      nodeConfiguration.setMasterUri(masterUri);
		      nodeConfiguration.setNodeName("android_sensors_driver_imu");
		      this.imu_pub = new ImuPublisher(mSensorManager);
		      this.nodeRunner.run(this.imu_pub, nodeConfiguration);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
        }
        
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {    	
        // Cancel the persistent notification.
    	stopForeground(true);
        
        this.nodeRunner.shutdownNodeMain(this.fix_pub);
        this.nodeRunner.shutdownNodeMain(this.imu_pub);
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        Notification note=new Notification(R.drawable.sensor_icon,
                "ROS Android Sensors Driver is running.",
                System.currentTimeMillis());
		Intent i=new Intent(this, MainActivity.class);
		
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
		Intent.FLAG_ACTIVITY_SINGLE_TOP);
		
		PendingIntent pi=PendingIntent.getActivity(this, 0, i, 0);
		
		note.setLatestEventInfo(this, "ROS Android Sensors Driver Running",
		    "To preserve battery life, plug in or close this application.",
		    pi);
		note.flags|=Notification.FLAG_NO_CLEAR;
		
		startForeground(1, note);
    }

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}
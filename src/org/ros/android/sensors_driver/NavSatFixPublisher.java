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

package org.ros.android.sensors_driver;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Looper;

import org.ros.message.Time;
import org.ros.message.sensor_msgs.NavSatFix;
import org.ros.message.sensor_msgs.NavSatStatus;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;

/**
 * @author chadrockey@gmail.com (Chad Rockey)
 */
public class NavSatFixPublisher implements NodeMain {

  private NavSatThread navSatThread;
  private LocationManager locationManager;
  private NavSatListener navSatFixListener;
  private Publisher<NavSatFix> publisher;
  
  private class NavSatThread extends Thread {
	  LocationManager locationManager;
	  NavSatListener navSatListener;
	  private Looper threadLooper;
	  
	  private NavSatThread(LocationManager locationManager, NavSatListener navSatListener){
		  this.locationManager = locationManager;
		  this.navSatListener = navSatListener;
	  }
	  
	    public void run() {
	    	Looper.prepare();
	    	threadLooper = Looper.myLooper();
	    	this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this.navSatListener);
	    	Looper.loop();
	    }
	    
	    public void shutdown(){
	    	this.locationManager.removeUpdates(this.navSatListener);
	    	if(threadLooper != null){
	            threadLooper.quit();
	    	}
	    }
	}
  
  private class NavSatListener implements LocationListener {

    private Publisher<NavSatFix> publisher;

    private volatile byte currentStatus;

    private NavSatListener(Publisher<NavSatFix> publisher) {
      this.publisher = publisher;
      this.currentStatus = NavSatStatus.STATUS_FIX; // Default to fix until we are told otherwise.
    }

	@Override
	public void onLocationChanged(Location location) {
		NavSatFix fix = new NavSatFix();
		fix.header.stamp = Time.fromMillis(System.currentTimeMillis());
		fix.header.frame_id = "/gps"; // TODO Make frame ID configurable
		
		fix.status.status = currentStatus;
		fix.status.service = NavSatStatus.SERVICE_GPS;
		
		fix.latitude = location.getLatitude();
		fix.longitude = location.getLongitude();
		fix.altitude = location.getAltitude();
		fix.position_covariance_type = NavSatFix.COVARIANCE_TYPE_APPROXIMATED;
		double deviation = location.getAccuracy();
		double covariance = deviation*deviation;
		fix.position_covariance[0] = covariance;
		fix.position_covariance[4] = covariance;
		fix.position_covariance[8] = covariance;
		publisher.publish(fix);
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		switch (status) {
		case LocationProvider.OUT_OF_SERVICE:
			currentStatus = NavSatStatus.STATUS_NO_FIX;
			break;
		case LocationProvider.TEMPORARILY_UNAVAILABLE:
			currentStatus = NavSatStatus.STATUS_NO_FIX;
			break;
		case LocationProvider.AVAILABLE:
			currentStatus = NavSatStatus.STATUS_FIX;
			break;
		}
	}
  }

  public NavSatFixPublisher(LocationManager manager) {
	  this.locationManager = manager;
  }

@Override
public void onStart(Node node) {
  try {
	 this.publisher = node.newPublisher("android/fix", "sensor_msgs/NavSatFix");
  	this.navSatFixListener = new NavSatListener(publisher);
  	this.navSatThread = new NavSatThread(this.locationManager, this.navSatFixListener);
  	this.navSatThread.start();
  } catch (Exception e) {
    if (node != null) {
      node.getLog().fatal(e);
    } else {
      e.printStackTrace();
    }
  }
}

@Override
public void onShutdown(Node arg0) {
	this.navSatThread.shutdown();
	try {
		this.navSatThread.join();
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
}

@Override
public void onShutdownComplete(Node arg0) {
}
}

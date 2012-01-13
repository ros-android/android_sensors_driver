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

import java.util.List;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Looper;

import org.ros.message.Time;
import org.ros.message.sensor_msgs.Imu;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;

/**
 * @author chadrockey@gmail.com (Chad Rockey)
 */
public class ImuPublisher implements NodeMain {

  private ImuThread imuThread;
  private SensorListener sensorListener;
  private SensorManager sensorManager;
  private Publisher<Imu> publisher;
  
  private class ImuThread extends Thread {
	  private final SensorManager sensorManager;
	  private SensorListener sensorListener;
	  private Looper threadLooper;
	  
	  private final Sensor accelSensor;
	  private final Sensor gyroSensor;
	  private final Sensor quatSensor;
	  
	  private ImuThread(SensorManager sensorManager, SensorListener sensorListener){
		  this.sensorManager = sensorManager;
		  this.sensorListener = sensorListener;
		  this.accelSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		  this.gyroSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		  this.quatSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
	  }
	  
	    public void run() {
	    	Looper.prepare();
	    	this.threadLooper = Looper.myLooper();
	    	this.sensorManager.registerListener(this.sensorListener, this.accelSensor, SensorManager.SENSOR_DELAY_FASTEST);
	    	this.sensorManager.registerListener(this.sensorListener, this.gyroSensor, SensorManager.SENSOR_DELAY_FASTEST);
	    	this.sensorManager.registerListener(this.sensorListener, this.quatSensor, SensorManager.SENSOR_DELAY_FASTEST);
	    	Looper.loop();
	    }
	    
	    public void shutdown(){
	    	this.sensorManager.unregisterListener(this.sensorListener);
	    	if(this.threadLooper != null){
	            this.threadLooper.quit();
	    	}
	    }
	}
  
  private class SensorListener implements SensorEventListener {

    private Publisher<Imu> publisher;
    
    private boolean hasAccel;
    private boolean hasGyro;
    private boolean hasQuat;
    
    private long accelTime;
    private long gyroTime;
    private long quatTime;
    
    private Imu imu;

    private SensorListener(Publisher<Imu> publisher, boolean hasAccel, boolean hasGyro, boolean hasQuat) {
      this.publisher = publisher;
      this.hasAccel = hasAccel;
      this.hasGyro = hasGyro;
      this.hasQuat = hasQuat;
      this.accelTime = 0;
      this.gyroTime = 0;
      this.quatTime = 0;
      this.imu = new Imu();
    }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
	        imu.linear_acceleration.x = event.values[0];
	        imu.linear_acceleration.y = event.values[1];
	        imu.linear_acceleration.z = event.values[2];
			this.accelTime = event.timestamp;
		} else if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
	        imu.angular_velocity.x = event.values[0];
	        imu.angular_velocity.y = event.values[1];
	        imu.angular_velocity.z = event.values[2];
			this.gyroTime = event.timestamp;
		} else if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
	        float[] quaternion = new float[4];
	        SensorManager.getQuaternionFromVector(quaternion, event.values);
		    this.imu.orientation.w = quaternion[0];
	        this.imu.orientation.x = quaternion[1];
	        this.imu.orientation.y = quaternion[2];
	       	this.imu.orientation.z = quaternion[3];
	       	this.quatTime = event.timestamp;
		}
		
		if((this.accelTime != 0 || !this.hasAccel) && (this.gyroTime != 0 || !this.hasGyro) && (this.quatTime != 0 || !this.hasQuat)){
			// TODO Check timestamps
			//long timeAverage = ((event.timestamp - this.accelTime) + (event.timestamp - this.gyroTime) + (event.timestamp - this.quatTime))/3;
			this.imu.header.stamp = Time.fromMillis(System.currentTimeMillis()); // -timeAverage/1000000
			this.imu.header.frame_id = "/imu"; // TODO Make frame ID configurable
			publisher.publish(this.imu);
			this.accelTime = 0;
			this.gyroTime = 0;
			this.quatTime = 0;
		}
	}



  }

  public ImuPublisher(SensorManager manager) {
	  this.sensorManager = manager;
  }

@Override
public void onStart(Node node) {
  try {
	this.publisher = node.newPublisher("android/imu", "sensor_msgs/Imu");
	
	// Determine if we have the various needed sensors
	boolean hasAccel = false;
	boolean hasGyro = false;
	boolean hasQuat = false;
	List<Sensor> accelList = this.sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
	if(accelList.size() > 0){
		hasAccel = true;
	}
	List<Sensor> gyroList = this.sensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
	if(gyroList.size() > 0){
		hasGyro = true;
	}
	List<Sensor> quatList = this.sensorManager.getSensorList(Sensor.TYPE_ROTATION_VECTOR);
	if(quatList.size() > 0){
		hasQuat = true;
	}
  	this.sensorListener = new SensorListener(publisher, hasAccel, hasGyro, hasQuat);
  	this.imuThread = new ImuThread(this.sensorManager, sensorListener);
  	this.imuThread.start();
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
	this.imuThread.shutdown();
	try {
		this.imuThread.join();
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
}

@Override
public void onShutdownComplete(Node arg0) {
}
}

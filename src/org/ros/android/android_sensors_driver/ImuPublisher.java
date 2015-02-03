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


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Looper;
import android.os.SystemClock;

import org.ros.message.Time;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;

import java.util.List;

import sensor_msgs.Imu;

/**
 * @author chadrockey@gmail.com (Chad Rockey)
 * @author axelfurlan@gmail.com (Axel Furlan)
 * @author tal.regev@gmail.com  (Tal Regev)
 */
public class ImuPublisher implements NodeMain {

    private String robotName;
    private ImuThread imuThread;
    private SensorListener sensorListener;
    private SensorManager sensorManager;
    private Publisher<Imu> publisher;
    private int sensorDelay;

    public ImuPublisher(SensorManager manager, int sensorDelay, String robotName) {
        this.sensorManager = manager;
        this.sensorDelay = sensorDelay;
        this.robotName = robotName;
    }

    public GraphName getDefaultNodeName() {
        return GraphName.of("android_sensors_driver/imuPublisher");
    }

    public void onError(Node node, Throwable throwable) {
    }

    public void onStart(ConnectedNode node) {
        try {
            this.publisher = node.newPublisher(robotName + "/android/imu", "sensor_msgs/Imu");
            // 	Determine if we have the various needed sensors
            boolean hasAccel = false;
            boolean hasGyro = false;
            boolean hasQuat = false;

            List<Sensor> accelList = this.sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);

            if (accelList.size() > 0) {
                hasAccel = true;
            }

            List<Sensor> gyroList = this.sensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
            if (gyroList.size() > 0) {
                hasGyro = true;
            }

            List<Sensor> quatList = this.sensorManager.getSensorList(Sensor.TYPE_ROTATION_VECTOR);
            if (quatList.size() > 0) {
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

    //@Override
    public void onShutdown(Node arg0) {
        if (this.imuThread == null) {
            return;
        }
        this.imuThread.shutdown();

        try {
            this.imuThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //@Override
    public void onShutdownComplete(Node arg0) {
    }

    private class ImuThread extends Thread {
        private final SensorManager sensorManager;
        private final Sensor accelSensor;
        private final Sensor gyroSensor;
        private final Sensor quatSensor;
        private SensorListener sensorListener;
        private Looper threadLooper;

        private ImuThread(SensorManager sensorManager, SensorListener sensorListener) {
            this.sensorManager = sensorManager;
            this.sensorListener = sensorListener;
            this.accelSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            this.gyroSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            this.quatSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        }


        public void run() {
            Looper.prepare();
            this.threadLooper = Looper.myLooper();
            this.sensorManager.registerListener(this.sensorListener, this.accelSensor, sensorDelay);
            this.sensorManager.registerListener(this.sensorListener, this.gyroSensor, sensorDelay);
            this.sensorManager.registerListener(this.sensorListener, this.quatSensor, sensorDelay);
            Looper.loop();
        }


        public void shutdown() {
            this.sensorManager.unregisterListener(this.sensorListener);
            if (this.threadLooper != null) {
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
            this.imu = this.publisher.newMessage();
        }

        //	@Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        //	@Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                this.imu.getLinearAcceleration().setX(event.values[0]);
                this.imu.getLinearAcceleration().setY(event.values[1]);
                this.imu.getLinearAcceleration().setZ(event.values[2]);

                double[] tmpCov = {0, 0, 0, 0, 0, 0, 0, 0, 0};// TODO Make Parameter
                this.imu.setLinearAccelerationCovariance(tmpCov);
                this.accelTime = event.timestamp;
            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                this.imu.getAngularVelocity().setX(event.values[0]);
                this.imu.getAngularVelocity().setY(event.values[1]);
                this.imu.getAngularVelocity().setZ(event.values[2]);
                double[] tmpCov = {0, 0, 0, 0, 0, 0, 0, 0, 0};// TODO Make Parameter
                this.imu.setAngularVelocityCovariance(tmpCov);
                this.gyroTime = event.timestamp;
            } else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                float[] quaternion = new float[4];
                SensorManager.getQuaternionFromVector(quaternion, event.values);
                this.imu.getOrientation().setW(quaternion[0]);
                this.imu.getOrientation().setX(quaternion[1]);
                this.imu.getOrientation().setY(quaternion[2]);
                this.imu.getOrientation().setZ(quaternion[3]);
                double[] tmpCov = {0, 0, 0, 0, 0, 0, 0, 0, 0};// TODO Make Parameter
                this.imu.setOrientationCovariance(tmpCov);
                this.quatTime = event.timestamp;
            }

            // Currently storing event times in case I filter them in the future.  Otherwise they are used to determine if all sensors have reported.
            if ((this.accelTime != 0 || !this.hasAccel) &&
                    (this.gyroTime != 0 || !this.hasGyro) &&
                    (this.quatTime != 0 || !this.hasQuat)) {
                // Convert event.timestamp (nanoseconds uptime) into system time, use that as the header stamp
                long time_delta_millis = System.currentTimeMillis() - SystemClock.uptimeMillis();
                this.imu.getHeader().setStamp(Time.fromMillis(time_delta_millis + event.timestamp / 1000000));
                this.imu.getHeader().setFrameId("/imu");// TODO Make parameter

                publisher.publish(this.imu);

                // Create a new message
                this.imu = this.publisher.newMessage();

                // Reset times
                this.accelTime = 0;
                this.gyroTime = 0;
                this.quatTime = 0;
            }
        }
    }

}


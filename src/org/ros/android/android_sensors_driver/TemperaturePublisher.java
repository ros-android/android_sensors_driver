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

import sensor_msgs.Temperature;

/**
 * @author chadrockey@gmail.com (Chad Rockey)
 * @author tal.regev@gmail.com  (Tal Regev)
 */
public class TemperaturePublisher implements NodeMain {

    private String robotName;
    private TemperatureThread tmpThread;
    private SensorListener sensorListener;
    private SensorManager sensorManager;
    private Publisher<Temperature> publisher;
    private int sensorType;
    private int sensorDelay;

    public TemperaturePublisher(SensorManager manager, int sensorDelay, int sensorType, String robotName) {
        this.sensorManager = manager;
        this.sensorDelay = sensorDelay;
        this.sensorType = sensorType;
        this.robotName = robotName;
    }

    public GraphName getDefaultNodeName() {
        return GraphName.of("android_sensors_driver/temperature_publisher");
    }

    public void onError(Node node, Throwable throwable) {
    }

    public void onStart(ConnectedNode node) {
        try {
            List<Sensor> mfList = this.sensorManager.getSensorList(sensorType);

            if (mfList.size() > 0) {
                this.publisher = node.newPublisher(robotName + "/android/temperature", "sensor_msgs/Temperature");
                this.sensorListener = new SensorListener(this.publisher);
                this.tmpThread = new TemperatureThread(this.sensorManager, this.sensorListener);
                this.tmpThread.start();
            }

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
        if (this.tmpThread == null) {
            return;
        }

        this.tmpThread.shutdown();

        try {
            this.tmpThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //@Override
    public void onShutdownComplete(Node arg0) {
    }

    private class TemperatureThread extends Thread {
        private final SensorManager sensorManager;
        private final Sensor tmpSensor;
        private SensorListener sensorListener;
        private Looper threadLooper;

        private TemperatureThread(SensorManager sensorManager, SensorListener sensorListener) {
            this.sensorManager = sensorManager;
            this.sensorListener = sensorListener;
            this.tmpSensor = this.sensorManager.getDefaultSensor(sensorType);
        }


        public void run() {
            Looper.prepare();
            this.threadLooper = Looper.myLooper();
            this.sensorManager.registerListener(this.sensorListener, this.tmpSensor, sensorDelay);
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

        private Publisher<Temperature> publisher;

        private SensorListener(Publisher<Temperature> publisher) {
            this.publisher = publisher;
        }

        //	@Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        //	@Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == sensorType) {
                Temperature msg = this.publisher.newMessage();
                long time_delta_millis = System.currentTimeMillis() - SystemClock.uptimeMillis();
                msg.getHeader().setStamp(Time.fromMillis(time_delta_millis + event.timestamp / 1000000));
                msg.getHeader().setFrameId("/android/temperature");// TODO Make parameter

                msg.setTemperature(event.values[0]);
                msg.setVariance(0.0);

                this.publisher.publish(msg);
            }
        }
    }

}


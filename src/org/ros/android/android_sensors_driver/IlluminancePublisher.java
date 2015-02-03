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

import sensor_msgs.Illuminance;

/**
 * @author chadrockey@gmail.com (Chad Rockey)
 * @author tal.regev@gmail.com  (Tal Regev)
 */
public class IlluminancePublisher implements NodeMain {

    private String robotName;
    private IlluminanceThread ilThread;
    private SensorListener sensorListener;
    private SensorManager sensorManager;
    private Publisher<Illuminance> publisher;
    private int sensorDelay;

    public IlluminancePublisher(SensorManager manager, int sensorDelay, String robotName) {
        this.sensorManager = manager;
        this.sensorDelay = sensorDelay;
        this.robotName = robotName;
    }

    public GraphName getDefaultNodeName() {
        return GraphName.of("/android/illuminance_publisher");
    }

    public void onError(Node node, Throwable throwable) {
    }

    public void onStart(ConnectedNode node) {
        try {
            List<Sensor> mfList = this.sensorManager.getSensorList(Sensor.TYPE_LIGHT);

            if (mfList.size() > 0) {
                this.publisher = node.newPublisher(robotName + "/android/illuminance", "sensor_msgs/Illuminance");
                this.sensorListener = new SensorListener(this.publisher);
                this.ilThread = new IlluminanceThread(this.sensorManager, this.sensorListener);
                this.ilThread.start();
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
        if (this.ilThread == null) {
            return;
        }

        this.ilThread.shutdown();

        try {
            this.ilThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //@Override
    public void onShutdownComplete(Node arg0) {
    }

    private class IlluminanceThread extends Thread {
        private final SensorManager sensorManager;
        private final Sensor ilSensor;
        private SensorListener sensorListener;
        private Looper threadLooper;

        private IlluminanceThread(SensorManager sensorManager, SensorListener sensorListener) {
            this.sensorManager = sensorManager;
            this.sensorListener = sensorListener;
            this.ilSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }


        public void run() {
            Looper.prepare();
            this.threadLooper = Looper.myLooper();
            this.sensorManager.registerListener(this.sensorListener, this.ilSensor, sensorDelay);
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

        private Publisher<Illuminance> publisher;

        private SensorListener(Publisher<Illuminance> publisher) {
            this.publisher = publisher;
        }

        //	@Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        //	@Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                Illuminance msg = this.publisher.newMessage();
                long time_delta_millis = System.currentTimeMillis() - SystemClock.uptimeMillis();
                msg.getHeader().setStamp(Time.fromMillis(time_delta_millis + event.timestamp / 1000000));
                msg.getHeader().setFrameId("/illuminance"); // TODO Make parameter

                msg.setIlluminance(event.values[0]);
                msg.setVariance(0.0); // TODO Make parameter

                publisher.publish(msg);
            }
        }
    }

}


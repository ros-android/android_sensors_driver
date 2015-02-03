package org.ros.android.android_sensors_driver;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import org.ros.node.NodeConfiguration;

import java.net.URI;
import java.net.URISyntaxException;

//from https://code.google.com/p/rosjava/source/browse/android_gingerbread_mr1/src/org/ros/android/MasterChooser.java?repo=android&r=9327ca89c140c0c673d646cfe379ecf3943a0b64

/**
 * Created by tal regev on 10/01/15.
 * @author tal.regev@gmail.com  (Tal Regev)
 * My Master Chooser!! :)
 */
public class MasterChooser extends org.ros.android.MasterChooser {
    /**
     * The key with which the last used {@link java.net.URI} will be stored as a
     * preference.
     */
    private static final String PREFS_KEY_NAME = "URI_KEY";


    private EditText uriText;
    private EditText robotName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = super.getLayoutInflater();
        ViewGroup viewGroup = (ViewGroup) ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        layoutInflater.inflate(R.layout.my_master_chooser,viewGroup);
        robotName = (EditText) findViewById(R.id.robot_text);
        uriText = (EditText) findViewById(R.id.master_chooser_uri);
    }

    @Override
    public void okButtonClicked(View unused) {
        // Get the current text entered for URI.
        String userUri = uriText.getText().toString();
        String userRobotName = robotName.getText().toString();

        if (userUri.length() == 0) {
            // If there is no text input then set it to the default URI.
            userUri = NodeConfiguration.DEFAULT_MASTER_URI.toString();
            uriText.setText(userUri);
            Toast.makeText(MasterChooser.this, "Empty URI not allowed.", Toast.LENGTH_SHORT).show();
        }
        if (robotName.length() == 0) {
            // If there is no text input then set it to the default URI.
            userUri = NodeConfiguration.DEFAULT_MASTER_URI.toString();
            uriText.setText(userUri);
            Toast.makeText(MasterChooser.this, "Empty URI not allowed.", Toast.LENGTH_SHORT).show();
        }
        // Make sure the URI can be parsed correctly.
        try {
            new URI(userUri);
        } catch (URISyntaxException e) {
            Toast.makeText(MasterChooser.this, "Invalid URI.", Toast.LENGTH_SHORT).show();
            return;
        }

        // If the displayed URI is valid then pack that into the intent.
        userRobotName = userRobotName.replaceAll(" ", "_").toLowerCase();
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putString(PREFS_KEY_NAME, userUri);
        editor.commit();
        // Package the intent to be consumed by the calling activity.
        Intent intent = new Intent();
        intent.putExtra("ROS_MASTER_URI", userUri);
        intent.putExtra("ROBOT_NAME", userRobotName);
        setResult(RESULT_OK, intent);
        finish();
    }
}

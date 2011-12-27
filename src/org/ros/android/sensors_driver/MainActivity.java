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


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import org.ros.android.MasterChooser;
import org.ros.android.sensors_driver.R;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author chadrockey@gmail.com (Chad Rockey)
 */
public class MainActivity extends Activity {

  private URI masterUri;

  public MainActivity() {
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    setContentView(R.layout.main);
    if(masterUri == null){
    	startActivityForResult(new Intent(this, MasterChooser.class), 0);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (masterUri != null) {
      Intent intent = new Intent(this, SensorService.class);
      intent.putExtra("masterUri", masterUri.toString());
      startService(intent);
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == 0 && resultCode == RESULT_OK) {
      try {
        masterUri = new URI(data.getStringExtra("ROS_MASTER_URI"));
      } catch (URISyntaxException e) {
        throw new RuntimeException(e);
      }
    }
  }

@Override
public boolean onCreateOptionsMenu(Menu menu) {
	menu.add(0, 0, 0, R.string.app_about);
	menu.add(0, 1, 1, R.string.str_exit);
	return super.onCreateOptionsMenu(menu);
}

@Override
public boolean onOptionsItemSelected(MenuItem item) {
	super.onOptionsItemSelected(item);
	switch(item.getItemId())
	{
	case 0:
	openOptionsDialog();
	break;
	case 1:
	exitOptionsDialog();
	break;
	}
	return true;
}

private void openOptionsDialog(){
	new AlertDialog.Builder(this).setTitle(R.string.app_about).setMessage(R.string.app_about_message).setPositiveButton(R.string.str_ok,new DialogInterface.OnClickListener(){public void onClick(DialogInterface dialoginterface, int i){}}).show();
}

private void exitOptionsDialog()
{
	stopService(new Intent(this, SensorService.class));
	finish();
}

}

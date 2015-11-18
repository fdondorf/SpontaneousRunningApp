package org.spontaneous.logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class GPSLogEventReceiver extends BroadcastReceiver {

	   @Override
	   public void onReceive(Context context, Intent intent) {
	      Toast.makeText(context, "GPS Data received.", Toast.LENGTH_LONG).show();
	   }

	}

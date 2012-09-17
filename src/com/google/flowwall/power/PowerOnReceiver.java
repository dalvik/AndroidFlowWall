package com.google.flowwall.power;

import java.io.File;

import com.google.flowwall.service.FlowMonitor;
import com.google.flowwall.utils.Constants;
import com.google.flowwall.utils.FileUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PowerOnReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			FileUtil.writeFile("0,0,0,0,0,0,0,0,0,0,0,0", context.getFilesDir().getPath() + File.separator + Constants.ON_NAME);
			Intent i = new Intent(context, FlowMonitor.class);
			context.stopService(i);
			Api.applySavedIptablesRules(context, false);
		}
	}

}

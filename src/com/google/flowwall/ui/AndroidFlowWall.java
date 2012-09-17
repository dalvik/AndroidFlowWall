package com.google.flowwall.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.flowwall.R;
import com.google.flowwall.service.FlowMonitor;

public class AndroidFlowWall extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Intent i = new Intent(this, FlowMonitor.class);
		startService(i);
    }
}
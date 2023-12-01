package com.itschool.musicplayer;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class Launcher extends AppCompatActivity {

    public static final String TYPE = "type";
    public static final byte NULL = 0;
    public static final byte PLAY_PAUSE = 1;
    public static final byte KILL = 2;
    public static final byte PLAY = 3;
    public static final byte PAUSE = 4;
    public static final byte LOOP = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            findViewById(R.id.notif_settings).setOnClickListener(v -> {
                final Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, this.getPackageName());
                this.startActivity(intent);
            });
        }

        findViewById(R.id.button_play).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_DENIED) {
                Intent intent = new Intent(this, Service.class);
                if (foregroundServiceRunning()) {
                    stopService(intent);
                } else {
                    startService(intent);
                }
            }

        });

    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    private boolean foregroundServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (Service.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
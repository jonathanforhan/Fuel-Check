package com.example.fuelcheck.ui.home;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;

import androidx.annotation.Nullable;

public class TimerService extends Service {

    /*
     * this timer service runs in background keeping track of time
     * so that yuo can switch views and leave app without losing data
     */
    public static long downTime = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void startDownTime(long downStart) {
        //timestamp of when user left home-view
        downTime = downStart;
    }

    public long getDownTime() {
        // set chronometer to adjusted time
        return (getElapsedTime() - (getElapsedTime() - downTime));
    }

    public long getElapsedTime() {
        return SystemClock.elapsedRealtime();
    }
}

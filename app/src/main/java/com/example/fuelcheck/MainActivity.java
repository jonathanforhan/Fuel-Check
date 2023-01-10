package com.example.fuelcheck;

import static android.app.AlarmManager.INTERVAL_FIFTEEN_MINUTES;
import static com.example.fuelcheck.R.id.dark_theme;
import static com.example.fuelcheck.R.id.light_theme;
import static com.example.fuelcheck.R.id.settings_about;
import static com.example.fuelcheck.R.id.settings_history;
import static com.example.fuelcheck.R.id.settings_themes;
import static com.example.fuelcheck.R.layout.fragment_more;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.Fragment;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.fuelcheck.databinding.ActivityMainBinding;
import com.example.fuelcheck.ui.home.AlarmReceiver;
import com.example.fuelcheck.ui.home.HomeFragment;
import com.example.fuelcheck.ui.more.MoreFragment;

import java.nio.channels.CancelledKeyException;
import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {

    AlarmManager alarmManager;
    PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(android.R.style.Theme_DeviceDefault);
        super.onCreate(savedInstanceState);

        com.example.fuelcheck.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_calc, R.id.navigation_home, R.id.navigation_more).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        createNotificationChannel();
        setAlarm();
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "channel";
            String description = "myChannelDescription";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("ID", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void setAlarm() {


        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, AlarmReceiver.class);

        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000,
                1000 * 5,
                pendingIntent);

        Toast.makeText(this, "Alarm", Toast.LENGTH_SHORT).show();
    }

    @Override
    public Resources.Theme getTheme() {
        Resources.Theme theme = super.getTheme();
        SharedPreferences themeData = getSharedPreferences("themePref", MODE_PRIVATE);
        int themeKey = themeData.getInt("theme", 0);
        switch(themeKey) {
            case 0:
                setTheme(com.google.android.material.R.style.Theme_Material3_DynamicColors_Dark);
                return theme;
            case 1:
                setTheme(com.google.android.material.R.style.Theme_Material3_DynamicColors_Light);
                return theme;
            case 2:
                setTheme(R.style.Theme_CustomNVG);
                return theme;
            case 3:
                setTheme(com.google.android.material.R.style.Theme_Material3_Dark);
                return theme;
            default:
                setTheme(androidx.appcompat.R.style.Base_V7_Theme_AppCompat);
                return theme;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case settings_history:
                showHistory();
                return true;
            case settings_about:
                showInfo();
                return true;
            case settings_themes:
                return true;
            case dark_theme:
                applyTheme(0);
                return true;
            case light_theme:
                applyTheme(1);
                return true;
            case R.id.nvg_theme:
                applyTheme(2);
                return true;
            case R.id.dracula_theme:
                applyTheme(3);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void applyTheme(int id) {
        SharedPreferences sharedPreferences = getSharedPreferences("themePref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("theme", id);
        editor.apply();
        recreate();
    }

    @SuppressLint("ResourceType")
    private void showHistory() {

        Dialog history = new Dialog(this);
        history.setContentView(R.layout.dialog_history);

        TextView[] textArr = new TextView[10];
        textArr[0] = history.findViewById(R.id.history_check_1);
        textArr[1] = history.findViewById(R.id.history_check_2);
        textArr[2] = history.findViewById(R.id.history_check_3);
        textArr[3] = history.findViewById(R.id.history_check_4);
        textArr[4] = history.findViewById(R.id.history_check_5);
        textArr[5] = history.findViewById(R.id.history_check_6);
        textArr[6] = history.findViewById(R.id.history_check_7);
        textArr[7] = history.findViewById(R.id.history_check_8);
        textArr[8] = history.findViewById(R.id.history_check_9);
        textArr[9] = history.findViewById(R.id.history_check_10);

        SharedPreferences checkData = getSharedPreferences("checkHistory", MODE_PRIVATE);
        int index = checkData.getInt("index", 0);

        for(int i = 0; i < 10; i++) {
            index--;
            if(index < 0) {
                index = 9;
            }
            String text = checkData.getString("check"+(index), "no results");
            textArr[i].setText(text);
        }

        history.show();

        Button returnBtn = history.findViewById(R.id.dialog_button);
        returnBtn.setOnClickListener(view ->
                history.cancel());
    }

    @SuppressLint("ResourceType")
    private void showInfo() {

        Dialog info = new Dialog(this);
        info.setContentView(R.layout.dialog_info);

        info.show();

        Button returnBtn = info.findViewById(R.id.dialog_button);
        returnBtn.setOnClickListener(view ->
                info.cancel());
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        MoreFragment fragment = MoreFragment.getInstance();
        fragment.setTime(hourOfDay, minute);
    }

    public void setTitle(String title) {
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);
    }
}
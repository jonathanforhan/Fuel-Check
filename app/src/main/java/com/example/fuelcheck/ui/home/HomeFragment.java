package com.example.fuelcheck.ui.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.fuelcheck.databinding.FragmentHomeBinding;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private FragmentHomeBinding binding;
    private Context context;

    // background service to maintain proper time
    private static final TimerService service = new TimerService();
    private boolean running = false;
    private long pauseTime = 0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle("Fuel Check");
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        context = container.getContext();

        binding.linearLayout.setOnClickListener(this);
        binding.chronometer.setOnClickListener(this);
        binding.clearButton.setOnClickListener(this);
        binding.doneButton.setOnClickListener(this);

        // starting background service for timer to run independently
        getActivity().startService(new Intent(getActivity(), TimerService.class));

        // reset timer if theme is changed
        if(savedInstanceState != null) {
            resetSavedData(savedInstanceState);
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if(!running) {
            outState.putLong("pause", pauseTime);
        }
        outState.putBoolean("run", running);
    }

    public void resetSavedData(Bundle save) {

        running = save.getBoolean("run", false);
        if (!running) {
            long pauseTimeFromSaveData = save.getLong("pause", 0);
            binding.chronometer.setBase(service.getElapsedTime() - pauseTimeFromSaveData);
        }
        binding.chronometer.start();
        if(!running) {
            binding.chronometer.stop();
            pauseTime = service.getElapsedTime() - binding.chronometer.getBase();
            service.startDownTime(binding.chronometer.getBase());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(!running) { return; }
        service.startDownTime(binding.chronometer.getBase());
    }

    @Override
    public void onResume() {
        super.onResume();
        if(running) {
            binding.chronometer.setBase(service.getDownTime());
            binding.chronometer.start();
        } else {
            binding.chronometer.setBase(service.getElapsedTime() - pauseTime);
        }
    }

    @Override
    public void onClick(View v) {
        if (binding.chronometer.equals(v)) {
            startStopTimer();
        } else if (binding.clearButton.equals(v)) {
            resetTimer();
        } else if(binding.doneButton.equals(v)) {
            fuelCheck();
        } else if (binding.linearLayout.equals(v)) {
            clearFocus(v);
        }
    }

    @SuppressLint("SetTextI18n")
    public void fuelCheck() {
        if(binding.startingFuel.getText().toString().isEmpty() ||
                binding.endingFuel.getText().toString().isEmpty() ||
                binding.reserveTime.getText().toString().isEmpty()
        ) {
            setDelayText(binding.textOut2, "All Fields Required", 2000);
            return;
        }
        // stop timer
        if(running) {
            binding.chronometer.stop();
            pauseTime = service.getElapsedTime() - binding.chronometer.getBase();
            running = false;
        }
        // user input
        double startingFuel = Double.parseDouble(binding.startingFuel.getText().toString());
        double endingFuel = Double.parseDouble(binding.endingFuel.getText().toString());
        double reserveTime = Double.parseDouble(binding.reserveTime.getText().toString());
        double elapsedTime;
        if(!running) {
            elapsedTime = pauseTime;
        } else {
            elapsedTime = service.getElapsedTime() - binding.chronometer.getBase();
        }

        @SuppressLint("SimpleDateFormat") SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        // current time
        Date currentTime = new Date();
        // burn rate
        int burnRate = (int) ((startingFuel - endingFuel) / (elapsedTime / 3_600_000));
        // time until burnout
        Date timeUntilBurnout = new Date((long) (System.currentTimeMillis() + (endingFuel / burnRate) * 3_600_000 ));
        // time to reserve
        Date timeToReserve = new Date((long) (System.currentTimeMillis() + (endingFuel / burnRate) * 3_600_000 - reserveTime * 60_000));
        if(burnRate > 1_000_000) {
            burnRate = 0;
        }

        binding.textOut1.setText("Current Time: " + timeFormat.format(currentTime));
        binding.textOut2.setText("Burn Rate: " + burnRate + " lbs/hr");
        binding.textOut3.setText("Burnout Time: " + timeFormat.format(timeUntilBurnout));
        binding.textOut4.setText("Reserve Time: " + timeFormat.format(timeToReserve));

        if(burnRate <= 0) {
            return;
        }

        setCheckData(currentTime, burnRate, timeUntilBurnout, timeToReserve, (int)startingFuel, (int)endingFuel, timeFormat);
    }

    private void setCheckData(Date currentTime, int burnRate, Date timeUntilBurnout, Date timeToReserve,
                              int startingFuel, int endingFuel, SimpleDateFormat timeFormat) {
        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences("checkHistory", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int index = sharedPreferences.getInt("index", 0);
        if(index == 10) {
            index = 0;
        }
        String key = "check" + index;
        // apply check data to proper key
        editor.putString(key, "Time:                " + timeFormat.format(currentTime) +
                "\nBurn Rate:        " + burnRate + " lbs/hr" +
                "\nBurnout Time:  " + timeFormat.format(timeUntilBurnout) +
                "\nReserve Time:  " + timeFormat.format(timeToReserve) +
                "\nStarting Fuel:    " + startingFuel + " lbs" +
                "\nEnding Fuel:      " + endingFuel + " lbs"
        );

        // increment key

        editor.putInt("index", ++index);
        editor.apply();
    }

    private void startChronometer() {
        binding.chronometer.setBase(service.getElapsedTime() - pauseTime);
        binding.chronometer.start();
        running = true;
    }

    private void stopChronometer() {
        binding.chronometer.stop();
        pauseTime = service.getElapsedTime() - binding.chronometer.getBase();
        running = false;
    }

    private void startStopTimer() {
        if(!running) {
            startChronometer();
            clearOutputs();
        } else {
            stopChronometer();
        }
    }

    private void clearOutputs() {
        binding.textOut1.setText("");
        binding.textOut2.setText("");
        binding.textOut3.setText("");
        binding.textOut4.setText("");
    }

    private void resetTimer() {
        if(!running && pauseTime == 0) {
            binding.chronometer.stop();
            binding.chronometer.setBase(service.getElapsedTime());
            running = false;
            // reset inputs
            binding.startingFuel.setText("");
            binding.endingFuel.setText("");
            binding.reserveTime.setText("");
            clearOutputs();

            pauseTime = 0;
            return;
        }
        // confirmation pop up
        new AlertDialog.Builder(context)
                .setTitle("Clear Confirmation")
                .setMessage("Are you sure you want to clear? Timer will reset")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    // continue with delete
                    binding.chronometer.stop();
                    binding.chronometer.setBase(service.getElapsedTime());
                    running = false;
                    // reset inputs
                    binding.startingFuel.setText("");
                    binding.endingFuel.setText("");
                    binding.reserveTime.setText("");
                    clearOutputs();

                    pauseTime = 0;
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // do nothing
                })
                .show();
    }

    public void clearFocus(View v) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        binding.startingFuel.clearFocus();
        binding.endingFuel.clearFocus();
        binding.reserveTime.clearFocus();
    }

    private void setDelayText(TextView text, String s, long delay) {
        // set TextView text that disappears after specified delay
        text.setText(s);
        Handler handler = new Handler();
        handler.postDelayed(() -> text.setText(""), delay);
    }
}
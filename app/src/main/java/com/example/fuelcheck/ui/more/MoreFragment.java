package com.example.fuelcheck.ui.more;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.fuelcheck.databinding.FragmentMoreBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MoreFragment extends Fragment implements View.OnClickListener {

    private FragmentMoreBinding binding;
    private static MoreFragment instance;
    private Context context;
    private boolean clockRunning = true;
    private boolean more_clock_select = false;
    private boolean more_timer_select = false;

    private int currentHour = new Date().getHours();
    private int currentMin = new Date().getMinutes();
    private double duration = 0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle("Sandbox");

        // for use from main activity
        instance = this;

        binding = FragmentMoreBinding.inflate(inflater, container, false);
        context = container.getContext();
        View root = binding.getRoot();

        binding.moreClock.setOnClickListener(this);
        binding.moreTimer.setOnClickListener(this);
        binding.doneButton.setOnClickListener(this);
        binding.clearButton.setOnClickListener(this);
        binding.moreLinearLayout.setOnClickListener(this);
        maintainClock();

        return root;
    }

    public static MoreFragment getInstance() {
        return instance;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(View v) {

        if(binding.moreClock.equals(v)) {
            selectTime();
            more_clock_select = true;
            more_timer_select = false;
        } else if (binding.moreTimer.equals(v)) {
            selectTime();
            more_clock_select = false;
            more_timer_select = true;
        } else {
            more_clock_select = false;
            more_timer_select = false;

            if(binding.doneButton.equals(v)) {
                fuelCheck();
            } else if(binding.clearButton.equals(v)) {
                clearOutputs();
            } else if (binding.moreLinearLayout.equals(v)) {
                clearFocus(v);
            }
        }
    }

    private void selectTime() {
        DialogFragment timePicker = new TimeDialog();
        timePicker.show(this.getActivity().getSupportFragmentManager() , "timer picker");
    }

    @SuppressLint("SetTextI18n")
    public void fuelCheck() {
        if (binding.startingFuel.getText().toString().isEmpty() ||
                binding.endingFuel.getText().toString().isEmpty() ||
                binding.reserveTime.getText().toString().isEmpty()
        ) {
            setDelayText(binding.textOut2, "All Fields Required", 2000);
            return;
        }

        // user input
        double startingFuel = Double.parseDouble(binding.startingFuel.getText().toString());
        double endingFuel = Double.parseDouble(binding.endingFuel.getText().toString());
        double reserveTime = Double.parseDouble(binding.reserveTime.getText().toString());

        @SuppressLint("SimpleDateFormat") SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        Calendar currentTime = Calendar.getInstance();
        currentTime.set(Calendar.HOUR_OF_DAY, currentHour);
        currentTime.set(Calendar.MINUTE, currentMin);

        // burn rate
        int burnRate = (int) ((startingFuel - endingFuel) / (duration / 3_600_000));
        // time until burnout
        Date timeUntilBurnout = new Date((long)(currentTime.getTimeInMillis() + (endingFuel / burnRate) * 3_600_000));
        // time to reserve
        Date timeToReserve = new Date((long) (currentTime.getTimeInMillis() + (endingFuel / burnRate) * 3_600_000 - reserveTime * 60_000));
        if (burnRate > 1_000_000) {
            burnRate = 0;
        }

        binding.textOut1.setText("Current Time: " + timeFormat.format(currentTime.getTimeInMillis()));
        binding.textOut2.setText("Burn Rate: " + burnRate + " lbs/hr");
        binding.textOut3.setText("Burnout Time: " + timeFormat.format(timeUntilBurnout));
        binding.textOut4.setText("Reserve Time: " + timeFormat.format(timeToReserve));
    }

    @SuppressLint("SetTextI18n")
    public void setTime(int hour, int min) {

        String hour_string = Integer.toString(hour);
        String min_string = Integer.toString(min);

        // properly format
        if(hour_string.length() < 2) {
            hour_string = "0" + hour_string;
        }
        if(min_string.length() < 2) {
            min_string = "0" + min_string;
        }

        if(more_clock_select) {
            binding.moreClock.setText("Time of Check: " + hour_string + ":" + min_string);
            clockRunning = false;
            currentHour = hour;
            currentMin = min;
        } else if(more_timer_select) {
            binding.moreTimer.setText(hour_string + ":" + min_string);
            duration = hour * 60_000L + min * 1000L;
        }
    }

    @SuppressLint("SetTextI18n")
    private void setTrueTime() {
        if(!clockRunning) { return; }

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);

        String hour_string = Integer.toString(hour);
        String min_string = Integer.toString(min);

        if(hour_string.length() < 2) {
            hour_string = "0" + hour_string;
        }
        if(min_string.length() < 2) {
            min_string = "0" + min_string;
        }

        binding.moreClock.setText("Time of Check: " + hour_string + ":" + min_string);
    }


    @SuppressLint("SetTextI18n")
    private void maintainClock() {
        setTrueTime();
    }

    @SuppressLint("SetTextI18n")
    private void clearOutputs() {
        binding.textOut1.setText("");
        binding.textOut2.setText("");
        binding.textOut3.setText("");
        binding.textOut4.setText("");
        binding.startingFuel.setText("");
        binding.endingFuel.setText("");
        binding.reserveTime.setText("");
        binding.moreTimer.setText("00:00");
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
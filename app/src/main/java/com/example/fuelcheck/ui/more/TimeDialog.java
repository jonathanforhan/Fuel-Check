package com.example.fuelcheck.ui.more;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class TimeDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePicker = new TimePickerDialog(getActivity(),
                android.R.style.Theme_Holo_Dialog_NoActionBar,
                (TimePickerDialog.OnTimeSetListener) getActivity(),
                hour, minute,true
                );
        timePicker.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return timePicker;
    }
}

package com.aviation.fuelcheckerproprealphaversion;

import androidx.appcompat.app.AppCompatActivity;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private EditText edtTxtStartingFuel;
    private EditText edtTxtEndingFuel;
    private EditText edtTxtDuration;
    private EditText edtTxtReserveNeeded;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("Fuel Check Pro");

        Date currentDate = new Date();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        Button button = findViewById(R.id.Button);
        button.setOnClickListener(view -> {

           edtTxtStartingFuel = findViewById(R.id.edtTxtStartingFuel);
           edtTxtEndingFuel = findViewById(R.id.edtTxtEndingFuel);
           edtTxtDuration = findViewById(R.id.edtTxtDuration);
           edtTxtReserveNeeded = findViewById(R.id.edtTxtReserveNeeded);

           if (edtTxtStartingFuel.getText().toString().equals("")){
               TextView output1 = findViewById(R.id.output1);
               output1.setText("");

               TextView output2 = findViewById(R.id.output2);
               output2.setText("FILL ALL DATA FIELDS");

               TextView output3 = findViewById(R.id.output3);
               output3.setText("");

               TextView output4 = findViewById(R.id.output4);
               output4.setText("");
               return;
           }
            if (edtTxtEndingFuel.getText().toString().equals("")){
                TextView output1 = findViewById(R.id.output1);
                output1.setText("");

                TextView output2 = findViewById(R.id.output2);
                output2.setText("FILL ALL DATA FIELDS");

                TextView output3 = findViewById(R.id.output3);
                output3.setText("");

                TextView output4 = findViewById(R.id.output4);
                output4.setText("");
                return;
            }
            if (edtTxtDuration.getText().toString().equals("")){
                TextView output1 = findViewById(R.id.output1);
                output1.setText("");

                TextView output2 = findViewById(R.id.output2);
                output2.setText("FILL ALL DATA FIELDS");

                TextView output3 = findViewById(R.id.output3);
                output3.setText("");

                TextView output4 = findViewById(R.id.output4);
                output4.setText("");
                return;
            }
            if (edtTxtReserveNeeded.getText().toString().equals("")){
                TextView output1 = findViewById(R.id.output1);
                output1.setText("");

                TextView output2 = findViewById(R.id.output2);
                output2.setText("FILL ALL DATA FIELDS");

                TextView output3 = findViewById(R.id.output3);
                output3.setText("");

                TextView output4 = findViewById(R.id.output4);
                output4.setText("");
                return;
            }


            String startingFuelString = edtTxtStartingFuel.getText().toString();
            int startingFuelInt = Integer.parseInt(startingFuelString);
            String endingFuelString = edtTxtEndingFuel.getText().toString();
            int endingFuelInt = Integer.parseInt(endingFuelString);
            String durationString = edtTxtDuration.getText().toString();
            int durationInt = Integer.parseInt(durationString);
            String reserveNeededString = edtTxtReserveNeeded.getText().toString();
            int reserveNeededInt = Integer.parseInt(reserveNeededString);
            double startingFuelDub = Double.valueOf(startingFuelInt);
            double endingFuelDub = Double.valueOf(endingFuelInt);
            double durationDub = Double.valueOf(durationInt);
            double reserveNeededDub = Double.valueOf(reserveNeededInt);

            double burnRate = (startingFuelDub - endingFuelDub) / (durationDub /60);
            int burnRateFinal = (int)burnRate;

            double tilBurnout = (endingFuelDub / burnRate) * 60;
            int tilBurnoutInt = (int)tilBurnout;

            String timeToBurnout = timeFormat.format(
                    new Date(System.currentTimeMillis() + (tilBurnoutInt) * 60000));

            String timeToReserve = timeFormat.format(
                    new Date(System.currentTimeMillis() + (tilBurnoutInt) * 60000 - (reserveNeededInt * 60000)));

                    TextView output1 = findViewById(R.id.output1);
            output1.setText("Current Time: " + timeFormat.format(currentDate));

            TextView output2 = findViewById(R.id.output2);
            output2.setText("Burn Rate: " + burnRateFinal + "lbs per hour");

            TextView output3 = findViewById(R.id.output3);
            output3.setText("Burnout Time " + timeToBurnout);

            TextView output4 = findViewById(R.id.output4);
            output4.setText("Reserve Time " + timeToReserve);

        });

        Button clear = findViewById(R.id.Clear);
        clear.setOnClickListener(view -> {

            TextView output1 = findViewById(R.id.output1);
            output1.setText("");

            TextView output2 = findViewById(R.id.output2);
            output2.setText("");

            TextView output3 = findViewById(R.id.output3);
            output3.setText("");

            TextView output4 = findViewById(R.id.output4);
            output4.setText("");

        });
    }

}
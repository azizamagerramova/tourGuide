package com.example.aziza.tourguide;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by aziza on 2018-03-27.
 */

public class TourDetails extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, TimePickerFragment.TimeDialogListener {
    Date fromTimeDateObj;
    Date toTimeDateObj;
    Context ctx;
    EditText tourName, startTime, endTime;
    Button loadTourAttractions;
    String fromTimeString = "";
    String endTimeString = "";
    String tourNameString = "";
    String vehicleOption = "";
    RadioGroup radioButtonGroup;
    DBHelper mydb = new DBHelper(this);
    private static final String DIALOG_TIME = "MainActivity.TimeDialog";
    int indicator;
    Spinner city;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tour_info_activity);
        ctx = getApplicationContext();
        tourName = (EditText) findViewById(R.id.tourNameEnter);
        String[] citiesArray= getApplicationContext().getResources().getStringArray(R.array.cities_array);
        startTime = (EditText) findViewById(R.id.fromClock);
        endTime = (EditText) findViewById(R.id.toClock);
        city = (Spinner) findViewById(R.id.pickCity);
        loadTourAttractions = (Button) findViewById(R.id.loadTourAttractions);
        radioButtonGroup = (RadioGroup) findViewById(R.id.radioButtons);

        if (!mydb.currentTourName.equalsIgnoreCase("-1")) {
            tourName.setText(mydb.currentTourName);
            tourName.setEnabled(false);
            /*ugly method but it works so meh*/
            for (int i=0;i<citiesArray.length;i++) {
                if (mydb.getCurrent().equalsIgnoreCase(citiesArray[i])) {
                    city.setSelection(i);
                    city.setEnabled(false);
                }
            }
            Log.i("TourDetails", "Current Vehicle Option is " + mydb.currentVehicleOption);
            if (mydb.currentVehicleOption.equalsIgnoreCase("vehicle=foot")) {
                ((RadioButton) findViewById(R.id.walkingTourOption)).setChecked(true);
                ((RadioButton) findViewById(R.id.bikingTourOption)).setChecked(false);
            }
            else {
                ((RadioButton) findViewById(R.id.bikingTourOption)).setChecked(true);
                ((RadioButton) findViewById(R.id.walkingTourOption)).setChecked(false);

            }
            startTime.setText(mydb.getFromTime());
            endTime.setText(mydb.getToTime());
            fromTimeString = startTime.getText().toString();
            endTimeString = endTime.getText().toString();
        }
        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                indicator = 0;
                TimePickerFragment dialog = new TimePickerFragment();
                dialog.show(getSupportFragmentManager(), DIALOG_TIME);
            }
        });

        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                indicator = 1;
                TimePickerFragment dialog = new TimePickerFragment();
                dialog.show(getSupportFragmentManager(), DIALOG_TIME);
            }
        });

        startTime.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int inType = startTime.getInputType(); // backup the input type
                startTime.setInputType(InputType.TYPE_NULL); // disable soft input
                startTime.onTouchEvent(event); // call native handler
                startTime.setInputType(inType); // restore input type
                return true; // consume touch even
            }
        });

        endTime.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int inType = endTime.getInputType(); // backup the input type
                endTime.setInputType(InputType.TYPE_NULL); // disable soft input
                endTime.onTouchEvent(event); // call native handler
                endTime.setInputType(inType); // restore input type
                return true; // consume touch even
            }
        });

        loadTourAttractions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tourNameString = tourName.getText().toString();
                if (fromTimeString.equalsIgnoreCase("") || (endTimeString.equalsIgnoreCase("")))  {
                    fromTimeString = startTime.getText().toString();
                    RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioButtons);
                    endTimeString = endTime.getText().toString();
                    if (fromTimeString.equalsIgnoreCase("00:00 AM") || endTimeString.equalsIgnoreCase("00:00 AM"))
                    {
                        Log.i("Tour Details", "from time is: " + fromTimeString + " end time is: " + endTimeString);
                        Toast.makeText(ctx, "Time is incorrect!", Toast.LENGTH_SHORT).show();
                    }
                    if (tourNameString.equalsIgnoreCase("") || tourNameString == null || (radioGroup.getCheckedRadioButtonId() ==-1) ) {
                        Toast.makeText(ctx, "Provided information is incomplete!", Toast.LENGTH_SHORT).show();
                    }
                }
                /* set up infortmation for new tour creation */
                else {
                    RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioButtons);

                    int radioButtonID = radioGroup.getCheckedRadioButtonId();
                    if (radioButtonID == R.id.walkingTourOption)
                        vehicleOption = "vehicle=foot";
                    else
                        vehicleOption = "vehicle=bike";
                    mydb.setCurrentTourName(tourName.getText().toString());
                    mydb.markCurrent(String.valueOf(city.getSelectedItem()));
                    mydb.currentVehicleOption = vehicleOption;
                    mydb.currentTimeFrom = fromTimeString;
                    mydb.currentTimeTo = endTimeString;
                    Intent newTourDetails = new Intent(TourDetails.this, AttractionsInTour.class);
                    startActivityForResult(newTourDetails, 0);
                }
            }
        });
    }

    @Override
    public void onFinishDialog(String time) {
        Toast.makeText(this, time, Toast.LENGTH_SHORT).show();
        if (indicator == 0) {
            fromTimeString = time;
            startTime.setText(time, TextView.BufferType.NORMAL);
        }
        else {
            fromTimeDateObj = mydb.parseTime(fromTimeString);
            toTimeDateObj = mydb.parseTime(time);
            long differenceStartEnd = mydb.timeDifference(toTimeDateObj, fromTimeDateObj, TimeUnit.MINUTES);
            if (differenceStartEnd > 20) {
                endTimeString = time;
                endTime.setText(time, TextView.BufferType.NORMAL);
            }
            else {
                Toast.makeText(ctx, "Time is incorrect!", Toast.LENGTH_SHORT).show();
                endTimeString = "";
                endTime.setText("00:00 AM", TextView.BufferType.NORMAL);
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        // DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //drawer.closeDrawer(GravityCompat.START);
        return true;

    }

//    public void onRadioButtonClicked(View view) {
//        // Is the button now checked?
//        boolean checked = ((RadioButton) view).isChecked();
//
//        // Check which radio button was clicked
//        switch(view.getId()) {
//            case R.id.walkingTourOption:
//                if (checked)
//                    vehicleOption = "vehicle=foot";
//            case R.id.bikingTourOption:
//                if (checked)
//                    vehicleOption = "vehicle=bike";
//        }
//        Log.i("TOUR Details ", "vehicle option is " + vehicleOption);
//    }
}

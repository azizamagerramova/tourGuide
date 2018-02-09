package com.example.aziza.tourguide;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by aziza on 2018-01-12.
 */

public class CurrentCityTour extends AppCompatActivity implements TimePickerFragment.TimeDialogListener {
    public String city = "";
    private String fromTime = "";
    private String toTime = "";
    private TextView timeDisplay;
    private Button nextStep;
    private EditText pickTimeFrom;
    private EditText pickTimeTo;
    DBHelper mydb;
    private int indicator = 0;
    private static final String DIALOG_TIME = "MainActivity.TimeDialog";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.city_time_activity);
        timeDisplay = (TextView)findViewById(R.id.fromTime);
        pickTimeFrom = (EditText) findViewById(R.id.fromClock);
        pickTimeTo = (EditText) findViewById(R.id.toClock);
        nextStep = (Button) findViewById(R.id.nextStep);
        mydb = new DBHelper(this);
     //   mydb.dropDB();
        mydb.generateContentDB();
        city = mydb.getCurrent();
        pickTimeFrom.setText(mydb.getFromTime(city), TextView.BufferType.NORMAL);
        pickTimeTo.setText(mydb.getToTime(city), TextView.BufferType.NORMAL);

        nextStep.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View view) {
                if (fromTime.equalsIgnoreCase(""))
                    fromTime = mydb.getFromTime(city);
                if (toTime.equalsIgnoreCase(""))
                    toTime = mydb.getToTime(city);
                mydb.addHours(city, fromTime, toTime);
                Intent pickPointPage = new Intent(CurrentCityTour.this, startPoint.class);
                startActivityForResult(pickPointPage, 0);
            }
        });

        pickTimeFrom.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int inType = pickTimeFrom.getInputType(); // backup the input type
                pickTimeFrom.setInputType(InputType.TYPE_NULL); // disable soft input
                pickTimeFrom.onTouchEvent(event); // call native handler
                pickTimeFrom.setInputType(inType); // restore input type
                return true; // consume touch even
            }
        });

        pickTimeTo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int inType = pickTimeTo.getInputType(); // backup the input type
                pickTimeTo.setInputType(InputType.TYPE_NULL); // disable soft input
                pickTimeTo.onTouchEvent(event); // call native handler
                pickTimeTo.setInputType(inType); // restore input type
                return true; // consume touch even
            }
        });

        pickTimeFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                indicator = 0;
                TimePickerFragment dialog = new TimePickerFragment();
                dialog.show(getSupportFragmentManager(), DIALOG_TIME);
            }
        });

        pickTimeTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                indicator = 1;
                TimePickerFragment dialog = new TimePickerFragment();
                dialog.show(getSupportFragmentManager(), DIALOG_TIME);
            }
        });
    }

    @Override
    public void onFinishDialog(String time) {
        Toast.makeText(this, time, Toast.LENGTH_SHORT).show();
        if (indicator == 0) {
            fromTime = time;
            pickTimeFrom.setText(time, TextView.BufferType.NORMAL);
        }
        else {
            toTime = time;
            pickTimeTo.setText(time, TextView.BufferType.NORMAL);
        }
    }
}

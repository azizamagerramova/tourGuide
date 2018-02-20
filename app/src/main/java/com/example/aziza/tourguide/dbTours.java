package com.example.aziza.tourguide;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * Created by aziza on 2018-02-09.
 */

public class dbTours extends AppCompatActivity {
    Button next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.db_choose_tour_activity);
        next = (Button)findViewById(R.id.tourChosen);

        next.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View view) {
                Log.i("Got in the listener", "GUT");
                Intent cityTimePage = new Intent(dbTours.this, CurrentCityTour.class);
                startActivityForResult(cityTimePage, 0);
            }
        });

    }
}

package com.example.aziza.tourguide;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener  {
    Button createNewTour,loadExistingTour;
    private Spinner spinner1;
    DBHelper mydb = new DBHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_screen_activity);
        //mydb.dropDB();
        //mydb.generateContentDB();
        //this.getBackground().(120);
        createNewTour = (Button)findViewById(R.id.createNewTour);
        loadExistingTour = (Button)findViewById(R.id.loadExistingTour);

        createNewTour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mydb.currentTourName = "-1";
                Intent newTourDetails = new Intent(MainActivity.this, TourDetails.class);
                startActivityForResult(newTourDetails, 0);
            }
        });

        loadExistingTour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loadExistingTours = new Intent(MainActivity.this, dbTours.class);
                startActivityForResult(loadExistingTours, 0);
            }
        });

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
}

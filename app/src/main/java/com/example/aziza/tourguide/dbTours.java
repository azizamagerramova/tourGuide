package com.example.aziza.tourguide;

import android.app.LauncherActivity;
import android.app.LoaderManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;

/**
 * Created by aziza on 2018-02-09.
 */

public class dbTours extends AppCompatActivity  {
    DBHelper mydb = new DBHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load_dbtours_activity);

        if (mydb.toursExist()) {
            final ArrayList<String> allTours = mydb.getAllTourNames();
            final ArrayAdapter<String> adp = new ArrayAdapter<String>(dbTours.this,
                    android.R.layout.simple_list_item_1, allTours);
            final ListView lv = (ListView) findViewById(R.id.listViewTours);
            lv.setAdapter(adp);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
                    /*set current tour name and city */
                    Log.i("Db Tours: ", "position selected in array " + position);
                    String[] tourDetails = allTours.get(position).split(" \\(");
                    String tourName = tourDetails[0];
                    String city = tourDetails[1].split("\\)")[0];
                    mydb.setCurrentTourName(tourName);
                    mydb.markCurrent(city);
                    mydb.currentTimeTo = mydb.getToTime();
                    mydb.currentTimeFrom = mydb.getFromTime();;
                    mydb.currentVehicleOption = mydb.getCurrentVehicleOption();
                    Intent loadExistingTours = new Intent(dbTours.this, tourAction.class);
                    startActivityForResult(loadExistingTours, 0);

                }
            });
        }
    }
}

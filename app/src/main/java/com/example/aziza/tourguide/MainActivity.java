package com.example.aziza.tourguide;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener  {
    Button currentCityTourButton;
    private Spinner spinner1;
    DBHelper mydb = new DBHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //this.getBackground().setAlpha(120);
          //  mydb.dropDB();
         //   mydb.generateContentDB();
        currentCityTourButton = (Button)findViewById(R.id.CurrentCityTour);
        spinner1 = (Spinner)findViewById(R.id.pickCity);
        currentCityTourButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View view) {
                Log.i("Got in the listener", "GUT");

               // Intent cityTimePage = new Intent(MainActivity.this, CurrentCityTour.class);
                Intent cityTimePage = new Intent(MainActivity.this, CurrentCityTour.class);
                mydb.markCurrent(String.valueOf(spinner1.getSelectedItem()));
                if (mydb.toursExist()) {
                    ArrayList<String> s = mydb.getTourNamesForCurrentCity();
                    String addNew = "New Tour";
                    s.add(addNew);
                    final ArrayAdapter<String> adp = new ArrayAdapter<String>(MainActivity.this,
                            android.R.layout.simple_spinner_item, s);
                    final Spinner sp = new Spinner(MainActivity.this);
                    sp.setAdapter(adp);

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    /*mark current tour in database and load it on start
                                    and open next page with time properly set
                                     */
                                    String choice = sp.getSelectedItem().toString();
                                    if (choice.equalsIgnoreCase("New Tour"))
                                        mydb.setCurrentTourName("-1");
                                    else
                                        mydb.setCurrentTourName(choice);
                                    Intent cityTimePage = new Intent(MainActivity.this, CurrentCityTour.class);
                                    startActivityForResult(cityTimePage, 0);
                                }
                            });
                    builder.setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    builder.setView(sp);
                    builder.create().show();
                }
               else
                   startActivityForResult(cityTimePage, 0);
            }
        });
        addListenerOnSpinnerItemSelection();

    }

    public void addListenerOnSpinnerItemSelection() {
        spinner1 = (Spinner) findViewById(R.id.pickCity);
      //  spinner1.setOnItemSelectedListener(new CustomOnItemSelectedListener());
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

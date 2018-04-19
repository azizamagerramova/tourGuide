package com.example.aziza.tourguide;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by aziza on 2018-03-30.
 */

public class tourAction extends AppCompatActivity {
    TextView tourName;
    Button editTour, beginTour, deleteTour;
    DBHelper mydb = new DBHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loaded_tour_activity);
        tourName = (TextView) findViewById(R.id.loadedTourName);
        deleteTour = (Button) findViewById(R.id.deleteTour);
        beginTour = (Button) findViewById(R.id.startLoadedTour);
        editTour = (Button) findViewById(R.id.editLoadedTour);
        tourName.setText(mydb.currentTourName);

        deleteTour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(tourAction.this);
                builder.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mydb.removeCurrentTour();
                                Intent backToHome = new Intent(tourAction.this, MainActivity.class);
                                startActivityForResult(backToHome, 0);
                            }
                        });
                builder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                builder.setMessage("The tour will be deleted permanently!");
                builder.create().show();
            }
        });

        editTour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("tour Action", "got into edit listener");
                Intent editTour = new Intent(tourAction.this, TourDetails.class);
                startActivityForResult(editTour, 0);
            }
        });

        beginTour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pathReady = new Intent(tourAction.this, PathReady.class);
                startActivityForResult(pathReady, 0);

            }
        });
    }
}

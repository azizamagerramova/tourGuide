package com.example.aziza.tourguide;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.GraphHopperRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Created by aziza on 2018-01-18.
 */

public class startPoint extends AppCompatActivity implements LocationListener {

    MyLocationNewOverlay mLocationOverlay;
    CompassOverlay mCompassOverlay;
    Button addPoint, buildTour, saveTour;
    TextView newPoint, chosenRoute;
    Marker myLocation;
    int count = 0;
    MapView map;
    boolean added = false;
    boolean followme=true;
    protected ImageButton btFollowMe;
    private LocationManager lm;
    DBHelper mydb = new DBHelper(this);
    ArrayList<Attraction> attractions = new ArrayList<Attraction>();
    ArrayList<Attraction> chosenAttractions = new ArrayList<>();
    ArrayList<Polyline> overlays = new ArrayList<Polyline>();
    ArrayList<TextView> chosenSpotsAll = new ArrayList<TextView>();
    ArrayList<Button> removeButtonsArray = new ArrayList<Button>();
    RoadManager roadManager;
    HashMap<Button,TextView> remButToText = new HashMap<Button, TextView>();
    String tourName = "";
    int MAX_COUNT_ATT = 10;
    private Location currentLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context ctx = getApplicationContext();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        roadManager = new GraphHopperRoadManager("fae5bf0a-402a-48b2-96ac-324e138f53dc", true);
        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.pick_point_activity);

        buildTour = (Button) findViewById(R.id.buildTour);
        saveTour = (Button) findViewById(R.id.saveTour);

        newPoint = (TextView) findViewById(R.id.place);
        addPoint = (Button) findViewById(R.id.pointAddLoc);
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        final IMapController mapController = map.getController();
        mapController.setZoom(13);

        GpsMyLocationProvider provider = new GpsMyLocationProvider(ctx);
        provider.addLocationSource(LocationManager.NETWORK_PROVIDER);
        mLocationOverlay = new MyLocationNewOverlay(provider, map);
        this.mLocationOverlay.enableMyLocation();
        this.mCompassOverlay = new CompassOverlay(ctx, new InternalCompassOrientationProvider(ctx), map);
        this.mCompassOverlay.enableCompass();

        map.getOverlays().add(this.mCompassOverlay);
        map.getOverlays().add(this.mLocationOverlay);
        Location def = new Location(LocationManager.GPS_PROVIDER);
        def.setLatitude(mydb.getLatitude());
        def.setLongitude(mydb.setLongitude());
        mapController.animateTo(new GeoPoint(def));

        /* TO-DO:
            change order of spots to be in the order of the path
         */
        buildTour.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //drawTour();
                ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
                for (int i = 0; i < chosenAttractions.size(); i++) {
                    GeoPoint point = new GeoPoint(chosenAttractions.get(i).latitude, chosenAttractions.get(i).longitude);
                    waypoints.add(point);
                }
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    RoadManager roadManager = new GraphHopperRoadManager("fae5bf0a-402a-48b2-96ac-324e138f53dc", true);
                    roadManager.addRequestOption("vehicle=foot");
                    roadManager.addRequestOption("optimize=true");
                    Road road = roadManager.getRoad(waypoints);
                    Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
                    map.getOverlays().removeAll(overlays);
                    overlays.add(roadOverlay);
                    map.getOverlays().add(roadOverlay);
                    map.invalidate();

            }}    );

        saveTour.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (!mydb.currentTourName.equalsIgnoreCase("-1") ) {
                    mydb.addTour(mydb.currentTourName, chosenAttractions);
                    Toast toast = Toast.makeText(ctx, "Tour has been saved!", Toast.LENGTH_SHORT);
                    toast.show();
                }
                else {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            startPoint.this);

                    LayoutInflater li = LayoutInflater.from(startPoint.this);
                    View promptsView = li.inflate(R.layout.save_prompt, null);
                    // set prompts.xml to alertdialog builder
                    alertDialogBuilder.setView(promptsView);

                    final EditText userInput = (EditText) promptsView
                            .findViewById(R.id.editTextDialogUserInput);
                    // set dialog message
                    alertDialogBuilder
                            .setCancelable(false)
                            .setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // get user input and set it to result
                                            tourName = userInput.getText().toString();
                                            Log.i("START POINT", tourName);
                                            mydb.addTour(tourName, chosenAttractions);
                                            mydb.setCurrentTourName(tourName);
                                            Toast toast = Toast.makeText(ctx, "New tour has been saved!", Toast.LENGTH_SHORT);
                                            toast.show();
                                        }
                                    })
                            .setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();

                }
            }
        });

        addPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String newAtt = newPoint.getText().toString();
                if (!Attraction.containsAttraction(chosenAttractions, newAtt)) {
                    if (count <0)
                        count = 0;
                    count++;
                    /*set new attraction field*/
                    int resID = getResources().getIdentifier("chosenSpot" + count, "id", getPackageName());
                    chosenRoute = (TextView) findViewById(resID);
                    String text = count + " " + newAtt;
                    Log.i("Count is ", "Count is " + count);
                    chosenRoute.setVisibility(View.VISIBLE);
                    chosenRoute.setText(text);
                    chosenSpotsAll.add(chosenRoute);
                    chosenAttractions.add(Attraction.getAttraction(attractions, newAtt));

                    /*make remove button visible */
                    int remAttId = getResources().getIdentifier("chosenSpot" + count + "Remove", "id", getPackageName());
                    final Button addRemoveButton = (Button) findViewById(remAttId);
                    addRemoveButton.setVisibility(View.VISIBLE);

                } else {
                    Toast toast = Toast.makeText(ctx, "Already Added!", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        /*hook up all buttons to text field */
        for (int i = 1; i <= 10; i++) {
            int resID = getResources().getIdentifier("chosenSpot" + i, "id", getPackageName());
            chosenRoute = (TextView) findViewById(resID);
            int remAttId = getResources().getIdentifier("chosenSpot" + i + "Remove", "id", getPackageName());
            final Button addRemoveButton = (Button) findViewById(remAttId);
            remButToText.put(addRemoveButton, chosenRoute);
            chosenSpotsAll.add(chosenRoute);
            removeButtonsArray.add(addRemoveButton);
        }

        for (final Button b : removeButtonsArray) {
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    count--;
                    Log.i("START POINT", remButToText.get(b).getText().toString());
                    String attNameToDel = remButToText.get(b).getText().toString().substring(2);
                    Log.i("START POINT", attNameToDel);

                    Attraction.removeAttraction(chosenAttractions, attNameToDel);
                    setFieldsNButtons();

//                    if (count > 1) {
//                        drawTour();
//                    }
                }
            });
        }

        if (!mydb.currentTourName.equalsIgnoreCase("-1")) {
            Log.i("START POINT ", "SHOULD BE DRAWING A TOUR");
            chosenAttractions = mydb.getAttractionsForCurrentTour();
            setFieldsNButtons();
            drawTour();
        }
    }

        public void onResume() {
            super.onResume();
            //this will refresh the osmdroid configuration on resuming.
            //if you make changes to the configuration, use
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            Configuration.getInstance().save(this, prefs);
            Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

            super.onResume();

            /* get all permissions */
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
                Log.i("Start Point", "We have all permissions. Gut");

            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                Log.i("Start Point", "Don't have location access permissions");
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                Log.i("Start Point", "Don't have location access permissions");
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                Log.i("Start Point", "Don't have location access permissions");
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 1);
                Log.i("Start Point", "Don't have network access permissions");
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 1);
                Log.i("Start Point", "Don't have internet access permissions");
            }

            try {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, this);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            attractions = mydb.getAttractions();

            for (int i = 0; i < attractions.size(); i++) {
                Marker startMarker = new Marker(map);
                startMarker.setTitle(attractions.get(i).name);
                startMarker.setPosition(new GeoPoint(attractions.get(i).latitude, attractions.get(i).longitude));
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                startMarker.setIcon(getResources().getDrawable(R.drawable.marker));
                startMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker, MapView mapView) {
                        marker.showInfoWindow();
                        newPoint.setText(marker.getTitle());
                        return true;
                    }
                });
                map.getOverlays().add(startMarker);

            }
        }
    public void addOverlays() {
        myLocation = new Marker(map);
        myLocation.setIcon(getResources().getDrawable(R.drawable.icon));
        myLocation.setImage(getResources().getDrawable(R.drawable.icon));

    }

    @Override
    public void onLocationChanged(Location location) {
        myLocation.setPosition(new GeoPoint(location.getLatitude(), location.getLongitude()));

        if (!added) {
            map.getOverlayManager().add(myLocation);
            added = true;
        }
        if (followme) {
            map.getController().animateTo(myLocation.getPosition());
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void setFieldsNButtons() {
        for (TextView t : chosenSpotsAll) {
            t.setVisibility(View.INVISIBLE);
        }
        for (Button b : remButToText.keySet()) {
            b.setVisibility(View.INVISIBLE);
        }
        for (int i = 0; i < chosenAttractions.size(); i++) {
            int forText = i + 1;
            chosenSpotsAll.get(i).setText(forText + " " + chosenAttractions.get(i).name);
            removeButtonsArray.get(i).setVisibility(View.VISIBLE);
            chosenSpotsAll.get(i).setVisibility(View.VISIBLE);
        }
    }


    public void drawTour () {
        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
        for (int i = 0; i < chosenAttractions.size(); i++) {
            GeoPoint point = new GeoPoint(chosenAttractions.get(i).latitude, chosenAttractions.get(i).longitude);
            waypoints.add(point);
        }
        roadManager.addRequestOption("vehicle=foot");
        roadManager.addRequestOption("optimize=true");
        Road road = roadManager.getRoad(waypoints);
        Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
        map.getOverlays().removeAll(overlays);
        overlays.add(roadOverlay);
        map.getOverlays().add(roadOverlay);
        map.invalidate();
    }

    /*function to add all text views and
        buttons to one array when drawing the tour
        from database  */
    public void addTextViewsNButtons() {
                            /*set new attraction field*/
        for (int i=0;i<chosenAttractions.size();i++) {
            count = i+1;
            int resID = getResources().getIdentifier("chosenSpot" + count, "id", getPackageName());
            chosenRoute = (TextView) findViewById(resID);
            chosenSpotsAll.add(chosenRoute);
        }
    }

//    public ArrayList<Attraction> loadTour() {
//
//    }


}

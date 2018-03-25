package com.example.aziza.tourguide;

import android.Manifest;
import android.content.DialogInterface;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.GeomagneticField;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
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
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;


import com.example.aziza.tourguide.TSP.TSP;
import com.example.aziza.tourguide.dijkstra_impl.DijkstraAlgorithm;
import com.example.aziza.tourguide.location_services.MyLocationListener;
import com.example.aziza.tourguide.location_services.RouteOptimization;

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
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.IOrientationConsumer;
import org.osmdroid.views.overlay.compass.IOrientationProvider;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.w3c.dom.Attr;
import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;


/**
 * Created by aziza on 2018-01-18.
 */

public class startPoint extends AppCompatActivity implements LocationListener, IOrientationConsumer {

    MyLocationNewOverlay mLocationOverlay;
    CompassOverlay mCompassOverlay;
    Button addPoint, buildTour, saveTour, startTour;
    TextView info;
    TextView newPoint, chosenRoute;
    float lat = 0;
    float lon = 0;
    float alt = 0;
    long timeOfFix = 0;
    float gpsspeed;
    float gpsbearing;
    Marker myLocation;
    int count = 0;
    MapView map;
    boolean added = false;
    boolean followme =true;
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
    GpsMyLocationProvider provider;
    GeoPoint currentLocation;
    IOrientationProvider compass = null;
    Context ctx;
    float lastOrientation = 0;
    Polyline roadOverlay;
    Polyline userMoves;
    GeoPoint lastKnownLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = getApplicationContext();
        userMoves = new Polyline();
        setContentView(R.layout.pick_point_activity);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        roadManager = new GraphHopperRoadManager("fae5bf0a-402a-48b2-96ac-324e138f53dc", true);
        roadManager.addRequestOption("vehicle=foot");
        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        buildTour = (Button) findViewById(R.id.buildTour);
        info = (TextView) findViewById(R.id.TimeToDest);
        saveTour = (Button) findViewById(R.id.saveTour);
        startTour = (Button) findViewById(R.id.startTour);

        newPoint = (TextView) findViewById(R.id.place);
        addPoint = (Button) findViewById(R.id.pointAddLoc);
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        final IMapController mapController = map.getController();
        mapController.setZoom(13);

        provider = new GpsMyLocationProvider(ctx);
        provider.addLocationSource(LocationManager.NETWORK_PROVIDER);
        provider.addLocationSource(LocationManager.GPS_PROVIDER);
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

        buildTour.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                drawTour();
                startTour.setVisibility(View.VISIBLE);
            }});

        startTour.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                saveTour.setVisibility(View.INVISIBLE);
                buildTour.setVisibility(View.INVISIBLE);
                if (provider.getLastKnownLocation() != null) {
                    Attraction current = new Attraction("", "", "", "00:00am", "00:00am", "00:00am", "00:00am", provider.getLastKnownLocation().getLatitude(), provider.getLastKnownLocation().getLongitude(), 0);
                    chosenAttractions.add(0, current);
                    mapController.setZoom(20);
                    mapController.animateTo(new GeoPoint(current.latitude, current.longitude));
                    drawTour();
                    resetFieldsNButtons();
                    newPoint.setVisibility(View.INVISIBLE);
                    addPoint.setVisibility(View.INVISIBLE);
                    startTour.setVisibility(View.INVISIBLE);
                    startTracking();
                    info.setVisibility(View.VISIBLE);
                }
                else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Wait for location manager", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }});

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
                                            if (mydb.toursNameExistsCurrCity(tourName)) {
                                                Toast toast = Toast.makeText(ctx, "Tour with this name already exists!", Toast.LENGTH_SHORT);
                                                toast.show();
                                            }
                                            else {
                                                mydb.addTour(tourName, chosenAttractions);
                                                mydb.setCurrentTourName(tourName);
                                                Toast toast = Toast.makeText(ctx, "New tour has been saved!", Toast.LENGTH_SHORT);
                                                toast.show();
                                            }
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
                if (!newAtt.equalsIgnoreCase("")) {
                    if (!Attraction.containsAttraction(chosenAttractions, newAtt)) {
                        if (count < 0)
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

                    } else if (chosenAttractions.size() >4){
                        Toast toast = Toast.makeText(ctx, "Limit Reached!", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                    else {
                        Toast toast = Toast.makeText(ctx, "Already Added!", Toast.LENGTH_SHORT);
                        toast.show();
                    }
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
            Log.i("START POINT ", "Retrieving tour from db");
            chosenAttractions = mydb.getAttractionsForCurrentTour();
            setFieldsNButtons();
            drawTour();
            startTour.setVisibility(View.VISIBLE);
            count = chosenAttractions.size();
        }
    }

    public void startTracking() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)  == PackageManager.PERMISSION_GRANTED) {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0 , 0, this);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,this);
        }
        if (compass==null)
            compass = new InternalCompassOrientationProvider(ctx);
        compass.startOrientationProvider(this);
    }

        public void onResume() {
            super.onResume();
            /*TODO: make sense out of that thing  */
            //this will refresh the osmdroid configuration on resuming.
            //if you make changes to the configuration, use
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

            Configuration.getInstance().save(this, prefs);
            Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

            super.onResume();

            /* get all permissions */
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED){
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
                Log.i("Start Point", "Don't have storage access permissions");
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 1);
                Log.i("Start Point", "Don't have network access permissions");
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 1);
                Log.i("Start Point", "Don't have internet access permissions");
            }

            attractions = mydb.getAttractions();

            for (int i = 0; i < attractions.size(); i++) {
                Marker startMarker = new Marker(map);
                startMarker.setTitle(attractions.get(i).name);
                String descrHours = attractions.get(i).description + " <br> Weekday hours: " + attractions.get(i).from_time_week + "-" + attractions.get(i).to_time_week;
                descrHours = descrHours + "<br> Weekend hours: " + attractions.get(i).from_time_wed + "-" + attractions.get(i).to_time_wed;
                startMarker.setSnippet(descrHours);
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

    /* TODO: test users path gets drawn */
    @Override
    public void onLocationChanged(Location location) {
        Log.i("start Point", "Location Changed has been called");
        gpsbearing = location.getBearing();
        gpsspeed = location.getSpeed();
        lat = (float) location.getLatitude();
        lon = (float) location.getLongitude();
        alt = (float) location.getAltitude(); //meters
        timeOfFix = location.getTime();

        if (followme) {
             map.getController().animateTo(mLocationOverlay.getMyLocation());
        }

        /*trying to rotate map according to the position */
        float t = (360 - gpsbearing);
        if (t < 0) {
            t += 360;
        }
        if (t > 360) {
            t -= 360;
        }
        //help smooth everything out
        t = (int) t;
        t = t / 5;
        t = (int) t;
        t = t * 5;
        Log.i("start Points Location", "GPS speed " + gpsspeed);

        /*work that out to redraw the path as user moves */
        if ((gpsspeed >=0.01) && (lastKnownLocation != null)) {
            List<GeoPoint> pathPoints = roadOverlay.getPoints();
           // pathPoints.remove()
        }

        float difference  = Math.abs(lastOrientation-t);
        if ((gpsspeed >= 0.01) && (difference > 20)) {
            lastOrientation = t;
            map.setMapOrientation(t);
            GeoPoint loc = new GeoPoint(location.getLatitude(), location.getLongitude(), location.getAltitude());
            if (lastKnownLocation != null) {
                ArrayList<GeoPoint> prevToCurrPath = new ArrayList<GeoPoint>();
                prevToCurrPath.add(lastKnownLocation);
                prevToCurrPath.add(loc);
                userMoves.setPoints(prevToCurrPath);
                map.getOverlays().add(userMoves);
            }
                lastKnownLocation = loc;
            //otherwise let the compass take over
        }
    }

    Float trueNorth = 0f;

    @Override
    public void onOrientationChanged(final float orientationToMagneticNorth, IOrientationProvider source) {
        //note, on devices without a compass this never fires...
        //only use the compass bit if we aren't moving, since gps is more accurate when we are moving
        if (gpsspeed < 0.01) {
            GeomagneticField gf = new GeomagneticField(lat, lon, alt, timeOfFix);
            trueNorth = orientationToMagneticNorth + gf.getDeclination();
            gf = null;
            gf = null;
            synchronized (trueNorth) {
                if (trueNorth > 360.0f) {
                    trueNorth = trueNorth - 360.0f;
                }
                float actualHeading = 0f;

                //this part adjusts the desired map rotation based on device orientation and compass heading
                float t = (360 - trueNorth);
                if (t < 0) {
                    t += 360;
                }
                if (t > 360) {
                    t -= 360;
                }
                //help smooth everything out
                t = (int) t;
                t = t / 5;
                t = (int) t;
                t = t * 5;
                float difference  = Math.abs(lastOrientation-t);
                if (difference > 20) {
                    lastOrientation = t;
                    map.setMapOrientation(t);
                }
            }
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
        resetFieldsNButtons();
        for (int i = 0; i < chosenAttractions.size(); i++) {
            int forText = i + 1;
            chosenSpotsAll.get(i).setText(forText + " " + chosenAttractions.get(i).name);
            removeButtonsArray.get(i).setVisibility(View.VISIBLE);
            chosenSpotsAll.get(i).setVisibility(View.VISIBLE);
        }
    }

    public void resetFieldsNButtons() {
        for (TextView t : chosenSpotsAll) {
            t.setVisibility(View.INVISIBLE);
        }
        for (Button b : remButToText.keySet()) {
            b.setVisibility(View.INVISIBLE);
        }
    }

    public void drawTour () {
        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
        ArrayList<Attraction> sortedAttractions = new ArrayList<Attraction>();
        Log.i("TSP", "Attractions we are passing are ");
        for (Attraction a : chosenAttractions) {
            Log.i("TSP Attraction: ", a.name);
        }

        TSP tspAlgo = new TSP(roadManager, chosenAttractions,mydb);
        if (chosenAttractions.size() > 5) {
            Toast toast = Toast.makeText(getApplicationContext(), "Only 5 attractions are supported!", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        waypoints = tspAlgo.findBestRoute(chosenAttractions.get(0));
        Log.i("Start Point: ", "TSP ran once supposedly");
        if (waypoints.size() == 0) {
            Toast toast = Toast.makeText(getApplicationContext(), "First attraction is not open yet!", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        if (!tspAlgo.enoughTime()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Not enough time to visit all places!", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        waypoints = tspAlgo.findBestRoute(chosenAttractions.get(0));
        Log.i("Start Point: ", "TSP ran twice supposedly");

        for (int i = 0; i < waypoints.size(); i++) {
            sortedAttractions.add(Attraction.getAttractionByGeo(chosenAttractions, waypoints.get(i)));
        }

        Log.i("TSP ", "Attractions received from algorithm are: ");
        for (Attraction a : sortedAttractions) {
            Log.i("TSP Attraction: ", a.name);
        }

        /*TODO: make that more meaningful aka where we cannot go */
        if (waypoints.size() != chosenAttractions.size()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(startPoint.this);
            builder.setMessage("Cannot visit all attractions");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                }
            });
            builder.create().show();
            for (Attraction a : chosenAttractions) {
                if (!waypoints.contains(new GeoPoint(a.latitude, a.longitude))) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Cannot make it to " + a.name, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
            Log.i("Start point", "Case when we are out too early or too late for stuff");
        }

        /* do not do any drawing if we are only going to less than 2 points */
        if (waypoints.size() < 2)
            return;

        chosenAttractions = sortedAttractions;
        Road road = roadManager.getRoad(waypoints);
        long minutesToDest = TimeUnit.MINUTES.convert((long)road.mDuration, TimeUnit.SECONDS);
        String textInfo = timeToDest(minutesToDest) + " \n " + road.mLength + " km";
        info.setText(textInfo);
        roadOverlay = RoadManager.buildRoadOverlay(road);
        roadOverlay.setColor(Color.parseColor("#FF04635B"));
        roadOverlay.setWidth(10);
        map.getOverlays().removeAll(overlays);
        overlays.add(roadOverlay);
        map.getOverlays().add(roadOverlay);
        map.invalidate();
        setFieldsNButtons();
    }

    public String timeToDest(long minutes) {
        if (minutes < 60) {
            return minutes + "minutes";
        }
        else {
            int hours = (int) minutes / 60; //since both are ints, you get an int
            int min = (int) minutes % 60;
            return hours + " hours, " + min + " minutes";
        }
    }

}

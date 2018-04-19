package com.example.aziza.tourguide;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.GeomagneticField;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aziza.tourguide.TSP.TSP;

import org.joda.time.LocalDateTime;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.GraphHopperRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.IOrientationConsumer;
import org.osmdroid.views.overlay.compass.IOrientationProvider;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.w3c.dom.Attr;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by aziza on 2018-03-29.
 */

public class PathReady extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LocationListener, IOrientationConsumer {

    Context ctx;
    MyLocationNewOverlay mLocationOverlay;
    CompassOverlay mCompassOverlay;
    String totalTimeBeginning = "";
    MapView map;
    float lat = 0;
    float lon = 0;
    float alt = 0;
    TSP tspAlgo;
    boolean followme =true;
    long timeOfFix = 0;
    float gpsspeed;
    float gpsbearing;
    ArrayList<RoadNode> roadNodes;
    double totalTripTime= 0;
    Road road;
    Polyline userMoves = new Polyline();
    Marker myLocation;
    ArrayList<Polyline> overlays = new ArrayList<Polyline>();
    RoadManager roadManager;
    GpsMyLocationProvider provider;
    TextView navigationInfo, completeInfo, tripTimeTotal;
    Button startNavigation, rebuildTour;
    ArrayList<Attraction> chosenAttractions = new ArrayList<>();
    ArrayList<Attraction> attractionsToDisplayText = new ArrayList<Attraction>();
    int attractionsNoCurrentLoc;
    double timeInTheTourNodes = 0;
    int legIndex = 0;
    Polyline roadOverlay;
    private LocationManager lm;
    float lastOrientation = 0;
    IOrientationProvider compass = null;
    GeoPoint lastKnownLocation = new GeoPoint(0.00,0.00);
    DBHelper mydb = new DBHelper(this);
    ArrayList<GeoPoint> prevToCurrPath = new ArrayList<GeoPoint>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.build_path_content);
        ctx = getApplicationContext();
        map = (MapView) findViewById(R.id.map);
        navigationInfo = (TextView) findViewById(R.id.navigationInfo);
        completeInfo = (TextView) findViewById(R.id.totalMetersTotalTime);
        rebuildTour = (Button) findViewById(R.id.rebuildTour);
        tripTimeTotal = (TextView) findViewById(R.id.timeInTheTrip);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        final IMapController mapController = map.getController();
        mapController.setZoom(14);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        roadManager = new GraphHopperRoadManager("eb3f3902-52ca-48e3-83b7-ec09e3deb945" , true);
        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        startNavigation = (Button)  findViewById(R.id.startNavigation);
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

        chosenAttractions = mydb.getAttractionsForCurrentTour();
       // attractionsNoCurrentLoc = chosenAttractions.size();

        attractionsToDisplayText = chosenAttractions;
        drawTour();
        tripTimeTotal.setText("Trip time: "+ timeToDest((long)totalTripTime));

        rebuildTour.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (provider.getLastKnownLocation() != null) {
                    lastKnownLocation = new GeoPoint(provider.getLastKnownLocation().getLatitude(), provider.getLastKnownLocation().getLongitude());
                    mydb.currentTimeFrom =  LocalDateTime.now().toString("hh:mmaa");
                    startNavigation.setVisibility(View.INVISIBLE);
                    rebuildTour.setVisibility(View.VISIBLE);
                    tspAlgo.navigation = true;
                    startTracking();
                    drawTour();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Wait for location manager", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        startNavigation.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (provider.getLastKnownLocation() != null) {
                    lastKnownLocation = new GeoPoint(provider.getLastKnownLocation().getLatitude(), provider.getLastKnownLocation().getLongitude());
                    mapController.setZoom(20);
                    mapController.animateTo(lastKnownLocation);
                    startNavigation.setVisibility(View.INVISIBLE);
                    tripTimeTotal.setVisibility(View.INVISIBLE);
                    rebuildTour.setVisibility(View.VISIBLE);
                    tspAlgo.navigation = true;
                    drawTour();
                    startTracking();
                    navigationInfo.setVisibility(View.VISIBLE);
                    completeInfo.setVisibility(View.VISIBLE);
                }
                else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Wait for location manager", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

    }

    public void startTracking() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)  == PackageManager.PERMISSION_GRANTED) {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0 , 0,this);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,this);
        }
        if (compass==null)
            compass = new InternalCompassOrientationProvider(ctx);
        compass.startOrientationProvider(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        gpsbearing = location.getBearing();
        gpsspeed = location.getSpeed();
        lat = (float) location.getLatitude();
        lon = (float) location.getLongitude();
        alt = (float) location.getAltitude(); //meters
        timeOfFix = location.getTime();

     //   Log.i("PathReady: ", "first leg duration  info " + road.getLengthDurationText(ctx, legIndex));
//        for (int i=0; i< 5; i++) {
//            timeInTheTourNodes += road.mNodes.get(i).mDuration;
//        }
        DecimalFormat formatter = new DecimalFormat("##.##");
        String navInfoSet = roadNodes.get(1).mInstructions +  " " + formatter.format(roadNodes.get(0).mLength) + " km";
        Log.i("Path Ready: ", "Nav info: " + navInfoSet);
        navigationInfo.setText(navInfoSet);

        double totalTimeLeft = road.mLegs.get(legIndex).mDuration - timeInTheTourNodes;
        String completeInfoText = timeToDest(TimeUnit.MINUTES.convert((long)totalTimeLeft, TimeUnit.SECONDS)) + " to " + chosenAttractions.get(legIndex).name;
        completeInfo.setText(completeInfoText);
        //  Log.i("PathReady: ", "after for loop stuf: " + completeInfoText);

        if (gpsspeed >= 0.02 && followme) {
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
        if ((gpsspeed >=0.01) ) {
            GeoPoint loc = new GeoPoint(location.getLatitude(), location.getLongitude(), location.getAltitude());
            prevToCurrPath.add(loc);
            userMoves.setPoints(prevToCurrPath);
            map.getOverlays().add(userMoves);
            double differencLong = Math.abs(location.getLongitude() - roadNodes.get(0).mLocation.getLongitude());
            double differenceLatt = Math.abs(location.getLatitude() - roadNodes.get(0).mLocation.getLatitude());

            Log.i("start Point", "Location Changed has been called");
            Log.i("Path Ready", "Latitude of current location is " + location.getLatitude() + " Longtitude is " + location.getLongitude());
            Log.i("Path Ready", "First node's locations is " + roadNodes.get(0).mLocation);
            Log.i("Path Ready", "Different between our location longtitude and node's " + (location.getLongitude() - roadNodes.get(0).mLocation.getLongitude()) );

            Log.i("Path Ready", "All instructions are ");
            for (RoadNode rn : roadNodes) {
                Log.i("Path ready", rn.mInstructions);
            }

            /* proper distance check */
            //if (differencLong <= 0.0009 && differenceLatt <= 0.0009) {
            if (differencLong <= 0.009 && differenceLatt <= 0.009) {
                Log.i("Path Ready", "Got into if statement");
                navigationInfo.setText(roadNodes.get(1).mInstructions);
                timeInTheTourNodes += roadNodes.get(0).mDuration;
                Log.i("Pth Ready", "size of road nodes before removing " + roadNodes.size());
                roadNodes.remove(0);
                Log.i("Pth Ready", "size of road nodes after removing " + roadNodes.size());
                Log.i("Pth Ready", "road noad current " + roadNodes.get(0).toString());
                Log.i("Pth Ready", "Road node end this leg " + road.mNodes.get(road.mLegs.get(legIndex).mEndNodeIndex));
                if (road.mNodes.get(road.mLegs.get(legIndex).mEndNodeIndex) == roadNodes.get(0)) {
                    timeInTheTourNodes= 0;
                    legIndex ++;
                }
            }
        }

        float difference  = Math.abs(lastOrientation-t);
        if ((gpsspeed >= 0.02) && (difference > 20)) {
            lastOrientation = t;
            map.setMapOrientation(t);
        }

        //otherwise let the compass take over
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

    public void onResume() {
        super.onResume();
        Log.i("Path Ready", "On resume called");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        Configuration.getInstance().save(this, prefs);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        super.onResume();

            /* get all permissions */
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED &&
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

        for (int i = 0; i < chosenAttractions.size(); i++) {
            Marker startMarker = new Marker(map);
            startMarker.setTitle(chosenAttractions.get(i).name);
            startMarker.setPosition(new GeoPoint(chosenAttractions.get(i).latitude, chosenAttractions.get(i).longitude));
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            startMarker.setIcon(getResources().getDrawable(R.drawable.marker));
            startMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker, MapView mapView) {
                    marker.showInfoWindow();
                    return true;
                }
            });
            map.getOverlays().add(startMarker);
        }
    }

    public void drawTour () {
        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
        ArrayList<Attraction> sortedAttractions = new ArrayList<Attraction>();
        roadManager.addRequestOption(mydb.currentVehicleOption);
        roadManager.addRequestOption("instructions=true");
        Log.i("TSP", "Attractions we are passing are ");
        for (Attraction a : chosenAttractions) {
            Log.i("TSP Attraction: ", a.name);
        }
        tspAlgo = new TSP(roadManager, chosenAttractions, mydb);

        Log.i("Path Ready ", "first attraction in tour is " + mydb.firstAttractionInTour().name);
        waypoints = tspAlgo.findBestRoute(mydb.firstAttractionInTour(), lastKnownLocation);
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
        waypoints = tspAlgo.findBestRoute(mydb.firstAttractionInTour(), lastKnownLocation);
        totalTripTime = tspAlgo.finalTimeInTheTrip;
        Log.i("Start Point: ", "TSP ran twice supposedly");

        for (int i = 0; i < waypoints.size(); i++) {
            sortedAttractions.add(Attraction.getAttractionByGeo(chosenAttractions, waypoints.get(i)));
        }

        Log.i("TSP ", "Attractions received from algorithm are: ");
        for (Attraction a : sortedAttractions) {
            Log.i("TSP Attraction: ", a.name);
        }

        if (waypoints.size() != chosenAttractions.size()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(PathReady.this);
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

        /*TODO: navigation curernt locatin */
        chosenAttractions = sortedAttractions;
        attractionsToDisplayText = chosenAttractions;
        //attractionsToDisplayText.remove(0);
        if (provider.getLastKnownLocation() != null) {
            waypoints.add(0, lastKnownLocation);
        }

        road = roadManager.getRoad(waypoints);
        roadNodes = road.mNodes;

        Log.i("PathReady: ", "some node info " + road.mNodes.get(0).mInstructions);
        Log.i("PathReady: ", "road info " + road.getLengthDurationText(ctx, 0));

        /*testing stuff*/
        navigationInfo.setText(road.mNodes.get(0).mInstructions);

        Log.i("PathReady: ", "making sure we can get the name of the attraction " + mydb.firstAttractionInTour().name);
        String completeInfoText = road.getLengthDurationText(ctx, 0) + " to " + mydb.firstAttractionInTour().name;
        completeInfo.setText(completeInfoText);

        roadOverlay = RoadManager.buildRoadOverlay(road);
        roadOverlay.setColor(Color.parseColor("#FF04635B"));
        roadOverlay.setWidth(10);
        map.getOverlays().removeAll(overlays);
        overlays.add(roadOverlay);
        map.getOverlays().add(roadOverlay);
        map.invalidate();
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
    public String timeToDest(long minutes) {
        if (minutes < 60) {
            return minutes + " minutes";
        }
        else {
            int hours = (int) minutes / 60; //since both are ints, you get an int
            int min = (int) minutes % 60;
            return hours + " hr " + min + " min";
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

}

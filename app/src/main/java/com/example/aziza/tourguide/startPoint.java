package com.example.aziza.tourguide;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
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
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
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


/**
 * Created by aziza on 2018-01-18.
 */

public class startPoint extends AppCompatActivity implements LocationListener {

    MyLocationNewOverlay mLocationOverlay;
    CompassOverlay mCompassOverlay;
    Button addPoint;
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
    ArrayList<Attraction> chosenAttractions = new ArrayList<Attraction>();
    ArrayList<Polyline> overlays = new ArrayList<Polyline>();
    private Location currentLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context ctx = getApplicationContext();
        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.pick_point_activity);

        newPoint = (TextView) findViewById(R.id.place);
        chosenRoute = (TextView) findViewById(R.id.chosenSpots);
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

        addPoint.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View view) {
                String newAtt = newPoint.getText().toString();
                if (!Attraction.containsAttraction(chosenAttractions, newAtt)) {
                    count++;
                    chosenRoute.setVisibility(View.VISIBLE);
                    String text = chosenRoute.getText().toString();
                    text = text + "\r\n" + count + " " + newAtt;
                    chosenRoute.setText(text);
                    chosenAttractions.add(Attraction.getAttraction(attractions, newAtt));
                    ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
                    for (int i = 0; i < chosenAttractions.size(); i++) {
                        GeoPoint point = new GeoPoint(chosenAttractions.get(i).latitude, chosenAttractions.get(i).longitude);
                        waypoints.add(point);
                    }

                    if (count > 1) {
                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                        StrictMode.setThreadPolicy(policy);
                        RoadManager roadManager = new GraphHopperRoadManager("fae5bf0a-402a-48b2-96ac-324e138f53dc", true);
                        roadManager.addRequestOption("vehicle=foot");
                        roadManager.addRequestOption("optimize=true");
                        Road road = roadManager.getRoad(waypoints);
                        Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
                        overlays.add(roadOverlay);
                        map.getOverlays().removeAll(overlays);
                        map.getOverlays().add(roadOverlay);
                        map.invalidate();

                    }
                }
                else {
                    Toast toast = Toast.makeText(ctx, "Already Added!", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

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

}

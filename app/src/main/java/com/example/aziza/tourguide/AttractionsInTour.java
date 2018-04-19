package com.example.aziza.tourguide;

import android.app.usage.UsageEvents;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.graphhopper.directions.api.client.ApiException;
import com.graphhopper.directions.api.client.api.GeocodingApi;
import com.graphhopper.directions.api.client.model.GeocodingResponse;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.w3c.dom.Attr;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;

/**
 * Created by aziza on 2018-03-28.
 */

public class AttractionsInTour extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, MapEventsReceiver {
    MapView map;
    Button buildATour;
    Attraction attToAdd;
    ArrayList<Attraction> attractions = new ArrayList<Attraction>();
    ArrayList<Attraction> chosenAttractions = new ArrayList<Attraction>();
    ArrayList<Attraction> generatedAttractions = new ArrayList<Attraction>();
    Attraction firstAttraction = null;

    Context ctx;
    DBHelper mydb = new DBHelper(this);
    EditText spot1, spot2, spot3, spot4, spot5;
    ArrayList<EditText> attractionsPlaceHolders = new ArrayList<EditText>();
    HashMap<EditText,EditText> placeToTime = new HashMap<EditText, EditText>();
    HashMap<EditText, Integer> freeTextField = new HashMap<EditText, Integer>();
    EditText oneToModify = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attractions_on_map_activity);
        ctx = getApplicationContext();
        buildATour = (Button) findViewById(R.id.proceedToBuildAPath);
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        final IMapController mapController = map.getController();
        mapController.setZoom(13);
        Location def = new Location(LocationManager.GPS_PROVIDER);
        def.setLatitude(mydb.getLatitude());
        def.setLongitude(mydb.setLongitude());
        mapController.animateTo(new GeoPoint(def));

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        /*hook up all editTextFields to time spent  */
        for (int i = 1; i < 6; i++) {
            int resID = getResources().getIdentifier("spotToChoose" + i, "id", getPackageName());
            EditText chosenRoute = (EditText) findViewById(resID);
            int remAttId = getResources().getIdentifier("timeToSpendSpot" + i, "id", getPackageName());
            final EditText timeToSpend = (EditText) findViewById(remAttId);
            attractionsPlaceHolders.add(chosenRoute);
            placeToTime.put(chosenRoute, timeToSpend);
        }

        Log.i("Attractions in tour: ", "Size of the array is "  + attractionsPlaceHolders.size());
        oneToModify = attractionsPlaceHolders.get(0);

        /*set on click listener to indicate field we will set stuff in */
        for (final EditText e: placeToTime.keySet()) {
            e.setOnTouchListener(new View.OnTouchListener()
            {
                public boolean onTouch(View arg0, MotionEvent arg1)
                {
                    oneToModify = e;
                    int inType = e.getInputType(); // backup the input type
                    e.setInputType(InputType.TYPE_NULL); // disable soft input
                    e.onTouchEvent(arg1); // call native handler
                    e.setInputType(inType); // restore input type
                    return true;
                }
            });
        }

        buildATour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean indicationFirst = true;
                /*check that all fields are set */
                boolean allAreSet = false;
                chosenAttractions = new ArrayList<Attraction>();
                for (EditText e : attractionsPlaceHolders) {

                    if (!e.getText().toString().equalsIgnoreCase("")) {
                        allAreSet = true;
                        attToAdd = Attraction.getAttraction(attractions, e.getText().toString());
                        Log.i("Attractions in Tour ", "Names are:");
                        for (Attraction a : attractions) {
                            Log.i("Attractions in tour: ", "Attraction name : " + a.name);
                        }
                        if (attToAdd != null)
                            chosenAttractions.add(attToAdd);

                        else {
                            attToAdd = Attraction.getAttraction(generatedAttractions, e.getText().toString());
                            if (attToAdd != null) {
                                mydb.addAttToDB(attToAdd);
                                chosenAttractions.add(attToAdd);
                            }
                        }
                        if (indicationFirst) {
                            firstAttraction = attToAdd;
                            indicationFirst = false;
                        }
                        if (placeToTime.get(e).getText().toString().equalsIgnoreCase("")) {
                            /*there is an attraction that does not have a time definied for it */
                            allAreSet = false;
                            Toast.makeText(ctx, "Not All Fields Are Set!", Toast.LENGTH_SHORT).show();
                        }
                        if (allAreSet) {
                            for (Attraction a : chosenAttractions) {
                                if (a.name.equalsIgnoreCase(e.getText().toString())) {
                                    a.time_to_spend = Long.parseLong(placeToTime.get(e).getText().toString());
                                }
                            }
                        }
                    }
                }
                if (allAreSet) {
                    /* add visit time to attractions */
                    /*create a new tour in the database and open built tour page */
                    Log.i("Attractions in Tour", "first attraction is " +  chosenAttractions.get(0).name);
                    mydb.addTour(mydb.currentTourName, chosenAttractions, chosenAttractions.get(0));
                    for (Attraction a: chosenAttractions) {
                        Log.i("Attractions in tour", "Passing attractions to be saved name is " + a.name);
                    }
                    Intent pathReady = new Intent(AttractionsInTour.this, PathReady.class);
                    startActivityForResult(pathReady, 0);
                }
                else {
                    Toast.makeText(ctx, "Add at least two spots!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (!mydb.currentTourName.equalsIgnoreCase("-1")) {
            chosenAttractions = mydb.getAttractionsForCurrentTour();
            for (int i=0;i<chosenAttractions.size();i++) {
                attractionsPlaceHolders.get(i).setText(chosenAttractions.get(i).name);
                placeToTime.get(attractionsPlaceHolders.get(i)).setText(String.valueOf(chosenAttractions.get(i).time_to_spend));
                Log.i("Attractions in tour ", "loaded attraction name is " + chosenAttractions.get(i).name);
                if (!attractions.contains(chosenAttractions.get(i))) {
                    Log.i("Attractions in tour ", "loaded attraction adding to attractions name is " + chosenAttractions.get(i).name);
                    generatedAttractions.add(chosenAttractions.get(i));
                }
            }
        }
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this, this);
        map.getOverlays().add(mapEventsOverlay);
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

    public void onResume() {
        super.onResume();
        attractions = mydb.getAttractions();

        for (Attraction a : chosenAttractions) {
            if (!attractions.contains(a)) {
                Marker startMarker = new Marker(map);
                startMarker.setTitle(a.name);
                startMarker.setPosition(new GeoPoint(a.latitude, a.longitude));
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                startMarker.setIcon(getResources().getDrawable(R.drawable.marker));
                startMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker, MapView mapView) {
                        marker.showInfoWindow();
                        if (canSet(marker.getTitle())) {
                            Log.i("Attractions in tour ", "One to modify: " + oneToModify.getId());
                            oneToModify.setText(marker.getTitle());
                            resetOneToModify();
                        }
                        else {
                            Toast.makeText(ctx, "Attraction Already Added!", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                });
                map.getOverlays().add(startMarker);
            }
        }

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
                    if (canSet(marker.getTitle())) {
                        Log.i("Attractions in tour ", "One to modify: " + oneToModify.getId());
                        oneToModify.setText(marker.getTitle());
                        resetOneToModify();
                    }
                    else {
                        Toast.makeText(ctx, "Attraction Already Added!", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
            map.getOverlays().add(startMarker);
        }

    }

    @Override public boolean longPressHelper(GeoPoint p) {
        String spotInfo = "";
        GeocodingApi apiInstance = new GeocodingApi();
        String key = "eb3f3902-52ca-48e3-83b7-ec09e3deb945"; // String | Get your key at graphhopper.com
        String q = null;
        String locale = null;
        Integer limit = null;
        Boolean reverse = true;
        String point = p.getLatitude()+","+p.getLongitude();
        String provider = null;
        try {
            GeocodingResponse result = apiInstance.geocodeGet(key, q, locale, limit, reverse, point, provider);
            String street = result.getHits().get(0).getStreet();
            String houseNumber = result.getHits().get(0).getHousenumber();
            if (result.getHits().get(0).getName().equalsIgnoreCase(street))
                spotInfo = result.getHits().get(0).getCity() + ", " + street + " ";
            else
                spotInfo = result.getHits().get(0).getName() + " " + result.getHits().get(0).getCity();
            if (street != null) {
                spotInfo += ", " + street + " ";
            }
            if (houseNumber != null) {
                spotInfo += houseNumber;
            }

            Attraction newAttraction = new Attraction(result.getHits().get(0).getName(), spotInfo, result.getHits().get(0).getCity(),"", "",
                    "", "", result.getHits().get(0).getPoint().getLat(), result.getHits().get(0).getPoint().getLng(),0 );
            generatedAttractions.add(newAttraction);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling GeocodingApi#geocodeGet");
            e.printStackTrace();
        }
        Log.i("Attractions In Tour: ", "long press called");
        Marker newSpot = new Marker(map);
        newSpot.setPosition(new GeoPoint(p.getLatitude(), p.getLongitude()));
        newSpot.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        newSpot.setIcon(getResources().getDrawable(R.drawable.tagc));
        newSpot.setTitle(spotInfo);
        newSpot.showInfoWindow();
        newSpot.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                marker.showInfoWindow();
                if (canSet(marker.getTitle())) {
                    Log.i("Attractions in tour ", "One to modify: " + oneToModify.getId());
                    oneToModify.setText(marker.getTitle());
                    resetOneToModify();
                }
                else {
                    Toast.makeText(ctx, "Attraction Already Added!", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        oneToModify.setText(spotInfo);
        resetOneToModify();
        map.getOverlays().add(newSpot);
        return true;
    }

    public boolean canSet(String attName) {
        for (EditText e: attractionsPlaceHolders) {
            if (e.getText().toString().equalsIgnoreCase(attName)) {
                return false;
            }
        }
        return  true;
    }

    @Override public boolean singleTapConfirmedHelper(GeoPoint p) {
        return true;
    }


    public void resetOneToModify() {
        for(int i=0;i<(attractionsPlaceHolders.size()-1);i++) {
            if (attractionsPlaceHolders.get(i) == oneToModify) {
                oneToModify = attractionsPlaceHolders.get(i + 1);
                return;
            }
        }
    }
}

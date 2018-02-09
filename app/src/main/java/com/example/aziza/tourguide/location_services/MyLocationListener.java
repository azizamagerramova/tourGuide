package com.example.aziza.tourguide.location_services;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/**
 * Created by aziza on 2018-01-29.
 */

public class MyLocationListener implements LocationListener {


    public void onLocationChanged(Location location) {
       // currentLocation = new GeoPoint(location);
       // displayMyCurrentLocationOverlay();
    }

    public void onProviderDisabled(String provider) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
}

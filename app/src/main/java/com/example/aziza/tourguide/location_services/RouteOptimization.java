package com.example.aziza.tourguide.location_services;

import android.os.StrictMode;
import android.support.annotation.NonNull;

import org.osmdroid.bonuspack.routing.GraphHopperRoadManager;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import  java.util.*;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Map;
import java.util.Set;

/**
 * Created by aziza on 2018-02-04.
 */

public class RouteOptimization {
    ArrayList<Node> nodesList = new ArrayList<Node>();

    public RouteOptimization() {
    }

    public ArrayList<GeoPoint> sortGeoPoints(ArrayList<GeoPoint> waypoints) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        RoadManager roadManager = new GraphHopperRoadManager("fae5bf0a-402a-48b2-96ac-324e138f53dc", false);
        for (int i=0;i<waypoints.size();i++) {
            for (int j=0;j<waypoints.size();i++) {
                if (waypoints.get(i) != waypoints.get(j)) {
                    ArrayList<GeoPoint> twoPoints = new ArrayList<GeoPoint>();
                    twoPoints.add(waypoints.get(i));
                    twoPoints.add(waypoints.get(j));
                    Node newNode = new Node(waypoints.get(i),waypoints.get(j), roadManager.getRoad(waypoints).mLength);
                    nodesList.add(newNode);
                }
            }
        }
        return null;
    }
}

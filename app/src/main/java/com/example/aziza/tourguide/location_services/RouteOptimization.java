package com.example.aziza.tourguide.location_services;

import android.os.StrictMode;
import android.util.Log;

import com.example.aziza.tourguide.Attraction;

import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import  java.util.*;

/**
 * Created by aziza on 2018-02-04.
 */

public class RouteOptimization {
//    ArrayList<Vertex> nodesList = new ArrayList<Vertex>();
//
//    public RouteOptimization() {
//    }
//
//    public ArrayList<Attraction> sortGeoPoints(ArrayList<Attraction> attractions, RoadManager roadManager) {
//
//        for (int i=0;i<attractions.size();i++) {
//            GeoPoint point = new GeoPoint(attractions.get(i).latitude, attractions.get(i).longitude);
//            Vertex newNode = new Vertex(point, point, 5000000000.00);
//            nodesList.add(newNode);
//        }
//
//        return sortNodes(attractions, roadManager);
//    }
//
//    private ArrayList<Attraction> sortNodes(ArrayList<Attraction> attractions, RoadManager roadManager) {
//        ArrayList<Attraction> attractionsSorted = new ArrayList<>();
//        HashSet<Vertex> someNodes  = new HashSet<Vertex>();
//        nodesList.get(0).distance = 0.0;
//        attractionsSorted.add(Attraction.getAttractionByGeo(attractions, nodesList.get(0).point1));
//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy);
//
//        while (nodesList.size()!=0) {
//            Vertex u = nodesList.get(0).leastDistance(nodesList);
//            nodesList.remove(u);
//            someNodes.add(u);
//            for (int i =0; i<nodesList.size();i++) {
//                if (nodesList.get(i).point1 != u.point1) {
//                    ArrayList<GeoPoint> listPoints = new ArrayList<GeoPoint>();
//                    listPoints.add(nodesList.get(i).point1);
//                    listPoints.add(u.point1);
//                    double distancePtoP = u.distance + roadManager.getRoad(listPoints).mLength;
//                    Log.i("Route Optiomization", "Distance is " + distancePtoP);
//
//                    if (distancePtoP < nodesList.get(i).distance && nodesList.get(i).distance !=0) {
//                        nodesList.get(i).distance = distancePtoP;
//                        u.point2 = nodesList.get(i).point1;
//                        Log.i("Route Optimization", "a name from " + Attraction.getAttractionByGeo(attractions, u.point1).name);
//                        Log.i("Route Optimization", "a name to " + Attraction.getAttractionByGeo(attractions, nodesList.get(i).point1).name);
//                    }
////                    if (i == (nodesList.size() - 1)) {
////                        if (u.point2 != null) {
////                            if (!attractionsSorted.contains(Attraction.getAttractionByGeo(attractions, u.point2)))
////                                Log.i("Route Optimization ", "point 2 is :" + Attraction.getAttractionByGeo(attractions, u.point2).name);
////                                attractionsSorted.add(Attraction.getAttractionByGeo(attractions, u.point2));
////                        }
////                        else {
////                            attractionsSorted.add(Attraction.getAttractionByGeo(attractions, u.point1));
////                        }
////
////                    }
//                }
//            }
//        }
//        Log.i("Route Optimization", "Lets see what we got");
////        for (Attraction a : attractionsSorted ) {
////            Log.i("Rout Optimization", "attraction name: " + a.name);
////        }
//
//        for (Vertex a : someNodes) {
//            Log.i("Route Optimization", "a name from " + Attraction.getAttractionByGeo(attractions, a.point1).name);
//            Log.i("Route Optimization", "a name to " + Attraction.getAttractionByGeo(attractions, a.point2).name);
//            Log.i("Route Optimization", "a distance  " + a.distance);
//        }
//
//        return attractionsSorted;
//    }
//
////    private ArrayList<Attraction> sortNodesOther(ArrayList<Attraction> attractions) {
////         Set<Vertex>  unSettledNodes = new HashSet<Vertex>();
////         Set<Vertex> settledNodes = new HashSet<Vertex>();
////
////    }
}

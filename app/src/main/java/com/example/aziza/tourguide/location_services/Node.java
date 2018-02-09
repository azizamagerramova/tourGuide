package com.example.aziza.tourguide.location_services;

import org.osmdroid.util.GeoPoint;

/**
 * Created by aziza on 2018-02-04.
 */

public class Node {
    GeoPoint point1;
    GeoPoint point2;
    Double distance;
    public Node(GeoPoint point1, GeoPoint point2, Double distance) {
        this.point1 = point1;
        this.point2 = point2;
        this.distance = distance;
    }
}

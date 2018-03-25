package com.example.aziza.tourguide.dijkstra_impl;

import com.example.aziza.tourguide.Attraction;

import org.osmdroid.util.GeoPoint;

/**
 * Created by aziza on 2018-02-23.
 */

public class Edge {
    public final GeoPoint source;
    public final GeoPoint destination;
    public final double weight;

    public Edge(GeoPoint source, GeoPoint destination, double weight) {
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

    @Override
    public String toString() {
        return source + " " + destination;
    }

}

package com.example.aziza.tourguide.dijkstra_impl;

import android.os.StrictMode;
import android.util.Log;

import org.osmdroid.bonuspack.routing.GraphHopperRoadManager;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by aziza on 2018-02-22.
 */

public class DijkstraAlgorithm {
    public ArrayList<GeoPoint> attractions = new ArrayList<GeoPoint>();
    private Map<GeoPoint, GeoPoint> predecessors;
    private List<Edge> edges = new ArrayList<Edge>();
    private Map<GeoPoint, Double> distance;
    HashSet<GeoPoint>unSettledNodes;
    HashSet<GeoPoint>settledNodes;

    public DijkstraAlgorithm() {
        GeoPoint a = new GeoPoint(1.0, -75.7011);
        GeoPoint b =  new GeoPoint(2.0, -75.7079);
        GeoPoint c = new GeoPoint(3.0, -75.7009);
        attractions.add(a);
        attractions.add(b);
        attractions.add(c);
        /*
            B->C, C->A
            A->B, A->C

         */

        edges.add(new Edge(a,b,5.0));
        edges.add(new Edge(a,c,2.0));
        edges.add(new Edge(b,c,1.0));

    }

    public void  execute(GeoPoint source) {
        settledNodes = new HashSet<GeoPoint>();
        unSettledNodes = new HashSet<GeoPoint>();
        distance = new HashMap<GeoPoint, Double>();
        predecessors = new HashMap<GeoPoint, GeoPoint>();
        distance.put(source, 0.0);
        unSettledNodes.add(source);
        while (unSettledNodes.size() > 0) {
            GeoPoint node = getMinimum(unSettledNodes);
            System.out.println("Minimum node is " + node);
            settledNodes.add(node);
            unSettledNodes.remove(node);
            findMinimalDistances(node);
        }
    }

    public void printPredecessors() {
        System.out.print("Predecessors length is " + predecessors.size());
        for (Map.Entry<GeoPoint, GeoPoint> a : predecessors.entrySet()) {
            System.out.print("\nVertex 1 is " + a.getValue().getLatitude() +
                    " And is mapped to vertex 2 is " + a.getKey().getLatitude());
        }
    }

    private GeoPoint getMinimum(Set<GeoPoint> nodes) {
        GeoPoint minimum = null;
        for (GeoPoint n : nodes) {
            if (minimum == null) {
                minimum = n;
            } else {
                if (getShortestDistance(n) < getShortestDistance(minimum)) {
                    minimum = n;
                }
            }
        }
        return minimum;
    }

    private double getShortestDistance(GeoPoint destination) {
        Double d = distance.get(destination);
        if (d == null) {
            return Integer.MAX_VALUE;
        } else {
            return d;
        }
    }

    private void findMinimalDistances(GeoPoint node) {
        List<GeoPoint> adjacentNodes = getNeighbors(node);
        System.out.println("Size of neibours is " + adjacentNodes.size());
        for (GeoPoint target : adjacentNodes) {
            if (getShortestDistance(target) > getShortestDistance(node)
                    + getDistance(node, target)) {
                distance.put(target, getShortestDistance(node)
                        + getDistance(node, target));
                System.out.println("\n Source node is " + node);
                System.out.println("Target node is " + target);
                predecessors.put(target, node);
                unSettledNodes.add(target);
            }
        }
    }

    private List<GeoPoint> getNeighbors(GeoPoint node) {
        List<GeoPoint> neighbors = new ArrayList<GeoPoint>();
        for (Edge edge : edges) {
            if ((edge.source.getLatitude()== node.getLatitude()
            && edge.source.getLongitude()== node.getLongitude()) && (!settledNodes.contains(edge.destination))) {
                neighbors.add(edge.destination);
            }
        }
        return neighbors;
    }

    private double getDistance(GeoPoint node, GeoPoint target) {
        for (Edge edge : edges) {
            if (edge.source.equals(node)
                    && edge.destination.equals(target)) {
                return edge.weight;
            }
        }
        throw new RuntimeException("Should not happen");
    }


    public LinkedList<GeoPoint> getPath(GeoPoint target) {
        LinkedList<GeoPoint> path = new LinkedList<GeoPoint>();
        GeoPoint step = target;
        // check if a path exists
        if (predecessors.get(step) == null) {
            return null;
        }
        path.add(step);
        while (predecessors.get(step) != null) {
            step = predecessors.get(step);
            path.add(step);
        }
        // Put it into the correct order
        Collections.reverse(path);
        return path;
    }

}

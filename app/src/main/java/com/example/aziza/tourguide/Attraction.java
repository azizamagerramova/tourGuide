package com.example.aziza.tourguide;

import android.net.wifi.aware.AttachCallback;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by aziza on 2018-01-31.
 */

public class Attraction {
    String name;
    String id;
    String city;
    String from_time_week;
    String from_time_wed;
    String to_time_week;
    String to_time_wed;
    double latitude;
    double longitude;

    public Attraction( String id, String name, String city, String from_time_week, String from_time_wed, String to_time_week, String to_time_wed, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.from_time_week = from_time_week;
        this.from_time_wed = from_time_wed;
        this.to_time_week = to_time_week;
        this.to_time_wed = to_time_wed;
        this.latitude = latitude;
        this.longitude = longitude;

    }

    public static boolean containsAttraction(ArrayList<Attraction> attractions, String name) {
        for (Attraction a: attractions) {
            if (a.name.equalsIgnoreCase(name))
                return true;
        }
        return false;
    }

    public static boolean removeAttraction(ArrayList<Attraction> attractions, String name) {
        Attraction att = null;
        for (Attraction a: attractions) {
            if (a.name.equalsIgnoreCase(name))
                att = a;
        }
        return attractions.remove(att);
    }

    public static Attraction getAttraction (HashSet<Attraction> attractions, String name) {
        for (Attraction a: attractions) {
            if (a.name.equalsIgnoreCase(name))
                return a;
        }
        return null;
    }

    public static Attraction getAttraction (ArrayList<Attraction> attractions, String name) {
        for (Attraction a: attractions) {
            if (a.name.equalsIgnoreCase(name))
                return a;
        }
        return null;
    }

}

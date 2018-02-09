package com.example.aziza.tourguide;

import java.util.ArrayList;

/**
 * Created by aziza on 2018-01-31.
 */

public class Attraction {
    String name;
    String city;
    String from_time_week;
    String from_time_wed;
    String to_time_week;
    String to_time_wed;
    double latitude;
    double longitude;

    public Attraction(String name, String city, String from_time_week, String from_time_wed, String to_time_week, String to_time_wed, double latitude, double longitude) {
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

    public static Attraction getAttraction (ArrayList<Attraction> attractions, String name) {
        for (Attraction a: attractions) {
            if (a.name.equalsIgnoreCase(name))
                return a;
        }
        return null;
    }

}

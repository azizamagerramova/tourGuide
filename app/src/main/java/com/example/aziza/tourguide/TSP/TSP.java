package com.example.aziza.tourguide.TSP;

import android.util.Log;

import com.example.aziza.tourguide.Attraction;
import com.example.aziza.tourguide.DBHelper;

import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;
import org.w3c.dom.Attr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by aziza on 2018-03-01.
 */

public class TSP{
    GeoPoint startPoint;
    boolean firstRun = true;
    public boolean navigation = false;
    boolean firstAtt = true;
    Date currentTime = null;
    Date endTime;
    GeoPoint previousPlace = null;
    RoadManager roadManager;
    public double finalTimeInTheTrip = 0;
    public ArrayList<GeoPoint> fastestRoute = new ArrayList<GeoPoint>();
    ArrayList<GeoPoint> currentSublist = new ArrayList<GeoPoint>();
    ArrayList<GeoPoint> visitedPlaces = new ArrayList<GeoPoint>();
    ArrayList<Attraction> passedAttractions = new ArrayList<Attraction>();
    public HashMap<Double, ArrayList<GeoPoint>> possibleRoutes = new HashMap<Double, ArrayList<GeoPoint>>();
    public ArrayList<List<GeoPoint>> triedCombos = new ArrayList<List<GeoPoint>>();
    public ArrayList<Double> durationKeys = new ArrayList<Double>();
    double fastest = Double.MAX_VALUE;
    int sizeOfPlacesToGo = 0;
    DBHelper mydb;


    public TSP (RoadManager roadManager, ArrayList<Attraction> attractions, DBHelper mydb) {
        this.mydb = mydb;
        passedAttractions = attractions;
        this.roadManager = roadManager;
        sizeOfPlacesToGo = passedAttractions.size();
    }

    public ArrayList<GeoPoint> findBestRoute(Attraction start, GeoPoint currentLoc) {
        if (!firstRun) {
            if (possibleRoutes.get(fastest).size()==2) {
                return possibleRoutes.get(fastest);
            }
            Log.i("TSP", "Recursive algorithm should run now");
            recursiveTRAlgorithm(possibleRoutes.get(fastest), null);
            Log.i("TSP", "Final time in the trip " + finalTimeInTheTrip);
        }

        else {
            ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
            for (int i = 0; i < passedAttractions.size(); i++) {
                GeoPoint point = new GeoPoint(passedAttractions.get(i).latitude, passedAttractions.get(i).longitude);
                waypoints.add(point);
            }

            if (waypoints.size()==2) {
                double roadLength = roadManager.getRoad(waypoints).mDuration;
                possibleRoutes.put(roadLength, waypoints);
                durationKeys.add(roadLength);
                fastest = roadLength;
                Log.i("TSP", "fastest road time is " + fastest);
                finalTimeInTheTrip =  TimeUnit.MINUTES.convert((long)fastest, TimeUnit.SECONDS) + Attraction.getAttractionByGeo(passedAttractions, waypoints.get(0)).time_to_spend;
                finalTimeInTheTrip = finalTimeInTheTrip + Attraction.getAttractionByGeo(passedAttractions, waypoints.get(1)).time_to_spend;
                return  waypoints;
            }

            /* if we are just building a path, remove first point and then add it back */
                startPoint = new GeoPoint(start.latitude, start.longitude);
                waypoints.remove(startPoint);
                ArrayList<ArrayList<GeoPoint>> permutations = listPermutations(waypoints);
                for (ArrayList<GeoPoint> al : permutations) {
                    al.add(0, startPoint);
                    double roadLength = roadManager.getRoad(al).mDuration;
                    // double roadLength = ThreadLocalRandom.current().nextInt(0, 10 + 1);
                    possibleRoutes.put(roadLength, al);
                    durationKeys.add(roadLength);
                    if (roadLength < fastest) {
                        fastest = roadLength;
                    }
                }

            Collections.sort(durationKeys);
            firstRun = false;
            return possibleRoutes.get(fastest);
        }
        //return possibleRoutes.get(fastest);
        return visitedPlaces;
    }


    /*  Check if they have enough time to visit all places if best case scenario
     */
    public boolean enoughTime() {
        long minutesAvailable = mydb.timeDifference(mydb.parseTime(mydb.currentTimeTo), mydb.parseTime(mydb.currentTimeFrom), TimeUnit.MINUTES);
        double totalTime = TimeUnit.MINUTES.convert((long)fastest, TimeUnit.MILLISECONDS);;
        for (GeoPoint g : possibleRoutes.get(fastest)) {
            totalTime += Attraction.getAttractionByGeo(passedAttractions, g).time_to_spend;
        }
        if (minutesAvailable > totalTime) {
            return true;
        }
        return false;
    }

    /*TODO: final time in the trip should work for 2 points too */
    public void recursiveTRAlgorithm(List<GeoPoint> points, Date currentTime) {
        if (currentTime == null) {
            currentTime = mydb.parseTime(mydb.currentTimeFrom);
        }
        Log.i("TSP ", "current time before starting algo " + currentTime.toString());
        Log.i("TSP: ", "size of the points is " + points.size());
        for (int d=0;d<points.size();d++) {
            long summedTime = 0;

            /* if we have visited all the places, exit */
            if (visitedPlaces.size() == sizeOfPlacesToGo)
                return;

            /* calculate walking time from previous spot to current */
            if (previousPlace != null) {
                ArrayList<GeoPoint> distancebetweenTwo = new ArrayList<GeoPoint>();
                distancebetweenTwo.add(previousPlace);
                distancebetweenTwo.add(points.get(d));
                summedTime = summedTime + (long) roadManager.getRoad(distancebetweenTwo).mDuration;
                Log.i("TSP ", "summed walking time from " + Attraction.getAttractionByGeo(passedAttractions, previousPlace).name + " to " +
                        Attraction.getAttractionByGeo(passedAttractions, points.get(d)).name +  " is " + TimeUnit.MINUTES.convert(summedTime, TimeUnit.SECONDS));
            }

            /* now see if attraction is open when we will get there */
            Date attStartTime = mydb.parseTime(Attraction.getAttractionByGeo(passedAttractions, points.get(d)).from_time_week);
            Date attEndTime = mydb.parseTime(Attraction.getAttractionByGeo(passedAttractions, points.get(d)).to_time_week);
            summedTime = TimeUnit.MILLISECONDS.convert(summedTime, TimeUnit.SECONDS) + currentTime.getTime();
            long differenceSeconds = summedTime - attStartTime.getTime();
            Log.i("TSP: ", "Summed time is " + new Date(summedTime).toString() + ". Attraction start time is "  + attStartTime.toString());
            Log.i("TSP: ", "the difference between summed time and start time is " + TimeUnit.MINUTES.convert(differenceSeconds, TimeUnit.MILLISECONDS));
            long differenceMinutes = TimeUnit.MINUTES.convert(differenceSeconds, TimeUnit.MILLISECONDS);

            /*if start time is fine with us, go here*/
            if (differenceMinutes > 0 || firstAtt || (attEndTime.getTime() == attStartTime.getTime())) {
                Log.i("TSP ", "Attraction visit time " + Attraction.getAttractionByGeo(passedAttractions, points.get(d)).time_to_spend);

                /*Time we are gonna spend at the place in milliseconds*/
                summedTime = summedTime + TimeUnit.MILLISECONDS.convert(Attraction.getAttractionByGeo(passedAttractions, points.get(d)).time_to_spend, TimeUnit.MINUTES);
                Log.i("TSP: ", "attraction end time is " + (mydb.parseTime(Attraction.getAttractionByGeo(passedAttractions, points.get(d)).to_time_week)).toString());

                long makeBeforeOver = attEndTime.getTime() - summedTime;

                if ((makeBeforeOver > 0) || (attEndTime.getTime() == attStartTime.getTime())) {
                    Log.i("TSP", "The place is open for the whole duration of the trip ");
                    /*update current time  and add place to visitedPlaces */
                    visitedPlaces.add(points.get(d));
                    firstAtt = false;
                    previousPlace = points.get(d);
                    currentTime = new Date(summedTime);
                    currentTime.getTime();
                    finalTimeInTheTrip = summedTime - mydb.parseTime(mydb.getFromTime()).getTime();
                    finalTimeInTheTrip = TimeUnit.MINUTES.convert((long)finalTimeInTheTrip, TimeUnit.MILLISECONDS);
                    Log.i("TSP:", "New updated time is " + currentTime.toString());

                } else {
                    Log.i("TSP: ", "cannot visit this attraction. It closes before we can make it there");
                    /* we need to go to one place less */
                    sizeOfPlacesToGo --;
                }
            }

            /* if start time does not work with us, go here */
            else if (!firstAtt && (differenceMinutes < 0)){
                for (int i = 1; i < durationKeys.size(); i++) {
                    Log.i("TSP", "could not build the best path, got into else statement");
                    List<GeoPoint> sublistOfPoints = possibleRoutes.get(durationKeys.get(i)).subList(0, (visitedPlaces.size()));
                    for (GeoPoint g : sublistOfPoints) {
                        Log.i("TSP ", "point is " + Attraction.getAttractionByGeo(passedAttractions, g).name);
                    }
                    if (sublistOfPoints.containsAll(visitedPlaces)) {
                        List<GeoPoint> sublistToKeepGoing = possibleRoutes.get(durationKeys.get(i)).subList(visitedPlaces.size(), possibleRoutes.get(durationKeys.get(i)).size());

                        /* only pass the ones we have not tried before in the recursion tree */
                        if (!triedCombos.contains(sublistToKeepGoing)) {
                            previousPlace = sublistOfPoints.get(visitedPlaces.size() - 1);
                            Log.i("TSP", "Lets see what we are passing in recursion tree");
                            for (GeoPoint g : sublistToKeepGoing) {
                                Log.i("TSP thing ", "Attraction passed is " + Attraction.getAttractionByGeo(passedAttractions, g).name);
                            }
                            triedCombos.add(sublistToKeepGoing);
                            recursiveTRAlgorithm(sublistToKeepGoing, currentTime);
                        }
                    }
                }
            }
        }
    }


    /*Used for test purposes only */
    public void setUp() {
        fastestRoute.add(new GeoPoint(1.0, 1.0));
        fastestRoute.add(new GeoPoint(2.0, 2.0));
        fastestRoute.add(new GeoPoint(3.0, 3.0));

        /* create permutation list for test purposes */
        ArrayList<GeoPoint> visitedPlaces = new ArrayList<GeoPoint>();
        ArrayList<GeoPoint> arrayToCopy = new ArrayList<GeoPoint>();
        arrayToCopy.addAll(fastestRoute);
        arrayToCopy.remove(arrayToCopy.get(0));
        ArrayList<ArrayList<GeoPoint>> allPermutations = listPermutations(arrayToCopy);
        durationKeys.add(2.0);
        durationKeys.add(3.0);
        allPermutations.get(0).add(0, (new GeoPoint(1.0, 1.0)));
        allPermutations.get(1).add(0, (new GeoPoint(1.0, 1.0)));
        possibleRoutes.put(2.0, allPermutations.get(0));
        possibleRoutes.put(3.0, allPermutations.get(1));

    }

    public ArrayList<ArrayList<GeoPoint>> listPermutations(ArrayList<GeoPoint> list) {
        if (list.size() == 0) {
            ArrayList<ArrayList<GeoPoint>> result = new ArrayList<ArrayList<GeoPoint>>();
            result.add(new ArrayList<GeoPoint>());
            return result;
        }

        ArrayList<ArrayList<GeoPoint>> returnMe = new ArrayList<ArrayList<GeoPoint>>();
        GeoPoint firstElement = list.remove(0);
        ArrayList<ArrayList<GeoPoint>> recursiveReturn = listPermutations(list);

        for (List<GeoPoint> li : recursiveReturn) {
            for (int index = 0; index <= li.size(); index++) {
                ArrayList<GeoPoint> temp = new ArrayList<GeoPoint>(li);
                temp.add(index, firstElement);
                returnMe.add(temp);
            }
        }
        return returnMe;
    }

}

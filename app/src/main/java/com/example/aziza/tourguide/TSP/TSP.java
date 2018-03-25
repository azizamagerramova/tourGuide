package com.example.aziza.tourguide.TSP;

import android.os.StrictMode;
import android.util.Log;

import com.example.aziza.tourguide.Attraction;
import com.example.aziza.tourguide.DBHelper;

import org.osmdroid.bonuspack.routing.GraphHopperRoadManager;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;
import org.w3c.dom.Attr;

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Created by aziza on 2018-03-01.
 */

public class TSP{
    GeoPoint startPoint;
    boolean firstRun = true;
    boolean firstAtt = true;
    Date currentTime = null;
    Date endTime;
    GeoPoint previousPlace = null;
    RoadManager roadManager;
    public ArrayList<GeoPoint> fastestRoute = new ArrayList<GeoPoint>();
    ArrayList<GeoPoint> currentSublist = new ArrayList<GeoPoint>();
    ArrayList<GeoPoint> visitedPlaces = new ArrayList<GeoPoint>();
    ArrayList<Attraction> passedAttractions = new ArrayList<Attraction>();
    public HashMap<Double, ArrayList<GeoPoint>> possibleRoutes = new HashMap<Double, ArrayList<GeoPoint>>();
    public ArrayList<List<GeoPoint>> triedCombos = new ArrayList<List<GeoPoint>>();
    public ArrayList<Double> distanceKeys = new ArrayList<Double>();
    double fastest = Double.MAX_VALUE;
    int sizeOfPlacesToGo = 0;
    DBHelper mydb;


    public TSP (RoadManager roadManager, ArrayList<Attraction> attractions, DBHelper mydb) {
        this.mydb = mydb;
        passedAttractions = attractions;
        this.roadManager = roadManager;
        sizeOfPlacesToGo = passedAttractions.size();
    }

    /* TODO: open hours support for 2 points */
    public ArrayList<GeoPoint> findBestRoute(Attraction start) {
        if (!firstRun) {
            if (possibleRoutes.get(fastest).size()==2) {
                return possibleRoutes.get(fastest);
            }
            Log.i("TSP", "Recursive algorithm should run now");
            recursiveTRAlgorithm(possibleRoutes.get(fastest), null);
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
                distanceKeys.add(roadLength);
                fastest = roadLength;
                return  waypoints;
            }

            startPoint = new GeoPoint(start.latitude, start.longitude);
            waypoints.remove(startPoint);
            ArrayList<ArrayList<GeoPoint>> permutations = listPermutations(waypoints);
            for (ArrayList<GeoPoint> al : permutations) {
                al.add(0, startPoint);
                double roadLength = roadManager.getRoad(al).mDuration;
                // double roadLength = ThreadLocalRandom.current().nextInt(0, 10 + 1);
                possibleRoutes.put(roadLength, al);
                distanceKeys.add(roadLength);
                if (roadLength < fastest) {
                    fastest = roadLength;
                }
            }
            Collections.sort(distanceKeys);
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

    /* TODO: FIX THIS SHIT */
    /* TODO: add end time support
              same logic as with start won't work since
              it would just grab next one and if it closes after current time, add it to the list,
              which won't cover other ones closing before
              At minimum: at least run that, and give a warning if something cannot be visited cause of the place closing
     *       determine infinite loop and prevent it  */
    /* TODO: check if that works for 4 points. Test if new solution works
     * if it doesnt possible solution: keep already tried combinations in arraylist and skip them when running algorithm again
      * same for endTime, keep tried combinations in array list, skip the ones tried
       * keep the count of times ran through the same size array list, maybe have a formula for that
       * if that many times ran through the same array list, you might be stack in infiinite loop, so just exit the function
       * then check the size of outputed places and compare to the size passed, if doesn't match output what we have and
       * give a warning, that due to discruption of opening hours and time for the trip, these places cannot be visited*/
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
            summedTime = TimeUnit.MILLISECONDS.convert(summedTime, TimeUnit.SECONDS) + currentTime.getTime();
            long differenceSeconds = summedTime - attStartTime.getTime();
            Log.i("TSP: ", "Summed time is " + new Date(summedTime).toString() + ". Attraction start time is "  + attStartTime.toString());
            Log.i("TSP: ", "the difference between summed time and start time is " + TimeUnit.MINUTES.convert(differenceSeconds, TimeUnit.MILLISECONDS));
            long differenceMinutes = TimeUnit.MINUTES.convert(differenceSeconds, TimeUnit.MILLISECONDS);

            /*if start time is fine with us, go here*/
            if (differenceMinutes > 0) {
                Log.i("TSP ", "Attraction visit time " + Attraction.getAttractionByGeo(passedAttractions, points.get(d)).time_to_spend);

                /*Time we are gonna spend at the place in milliseconds*/
                summedTime = summedTime + TimeUnit.MILLISECONDS.convert(Attraction.getAttractionByGeo(passedAttractions, points.get(d)).time_to_spend, TimeUnit.MINUTES);
                Log.i("TSP: ", "attraction end time is " + (mydb.parseTime(Attraction.getAttractionByGeo(passedAttractions, points.get(d)).to_time_week)).toString());
                Date attEndTime = mydb.parseTime(Attraction.getAttractionByGeo(passedAttractions, points.get(d)).to_time_week);
                Log.i("TSP:", "added this point to visited places " + Attraction.getAttractionByGeo(passedAttractions, points.get(d)).name);
                currentTime = new Date(summedTime);

                Log.i("TSP:", "New updated time is " + currentTime.toString());
                long makeBeforeOver = attEndTime.getTime() - summedTime;

                if ((makeBeforeOver > 0)) {
                    Log.i("TSP", "The place is open for the whole duration of the trip ");
                    /*update current time  and add place to visitedPlaces */
                    Log.i("TSP:", "New updated time is " + currentTime.toString());
                    visitedPlaces.add(points.get(d));
                    firstAtt = false;
                    previousPlace = points.get(d);
                } else {
                    Log.i("TSP: ", "cannot visit this attraction. It closes before we can make it there");
                    /* we need to go to one place less */
                    sizeOfPlacesToGo --;
                }



            }
            else if (firstAtt) {
                return;
            }

            /* if start time does not work with us, go here */
            else if (!firstAtt && (differenceMinutes < 0)){
                for (int i = 1; i < distanceKeys.size(); i++) {
                    Log.i("TSP", "could not build the best path, got into else statement");
                    List<GeoPoint> sublistOfPoints = possibleRoutes.get(distanceKeys.get(i)).subList(0, (visitedPlaces.size()));
                    for (GeoPoint g : sublistOfPoints) {
                        Log.i("TSP ", "point is " + Attraction.getAttractionByGeo(passedAttractions, g).name);
                    }
                    if (sublistOfPoints.containsAll(visitedPlaces)) {
                        List<GeoPoint> sublistToKeepGoing = possibleRoutes.get(distanceKeys.get(i)).subList(visitedPlaces.size(), possibleRoutes.get(distanceKeys.get(i)).size());

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
        distanceKeys.add(2.0);
        distanceKeys.add(3.0);
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

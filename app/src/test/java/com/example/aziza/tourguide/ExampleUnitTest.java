package com.example.aziza.tourguide;

import com.example.aziza.tourguide.TSP.TSP;
import com.example.aziza.tourguide.dijkstra_impl.DijkstraAlgorithm;

import org.junit.Test;
import org.osmdroid.bonuspack.routing.GraphHopperRoadManager;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testAlgorithm() {
        ArrayList<Attraction> attractions =  new ArrayList<Attraction>();
        Attraction a = new Attraction("wdefgh", "a","", "",
                "","","",1.0, -75.7011, 0.0);
        Attraction b = new Attraction("gffdsa", "b","", "",
                "","","",2.0, -75.7079, 0.0);
        Attraction c = new Attraction("llslas", "c","", "",
                "","","",3.0, -75.7009, 0.0);
        attractions.add(a);
        attractions.add(b);
        attractions.add(c);
        RoadManager roadManager = new GraphHopperRoadManager("fae5bf0a-402a-48b2-96ac-324e138f53dc", true);
        roadManager.addRequestOption("vehicle=foot");
        TSP newTsp =  new TSP();
    }

    @Test
    public void testTSP() {
        TSP newTsp = new TSP();
        newTsp.setUp();
        newTsp.recursiveTRAlgorithm(newTsp.fastestRoute, null);
    }

}
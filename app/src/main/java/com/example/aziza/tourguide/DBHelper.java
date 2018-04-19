package com.example.aziza.tourguide;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.w3c.dom.Attr;

import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by aziza on 2018-01-16.
 */

public final class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "TourGuide.db";
    public static final String CITY_TABLE_NAME = "cities";
    public static final String CITY_COLUMN_CITY = "city";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String CURRENT = "current";
    public static final String CITY_COLUMN_FROM_TIME = "from_time";
    public static final String CITY_COLUMN_TO_TIME = "to_time";
    public static final String CITY_COLUMN_FROM_TIME_WEEK = "from_time_week";
    public static final String CITY_COLUMN_TO_TIME_WEEK = "to_time_week";
    public static final String CITY_COLUMN_FROM_TIME_WEND = "from_time_wend";
    public static final String CITY_COLUMN_TO_TIME_WEND = "to_time_wend";
    public static String currentTourName ="-1";
    public static String currentTimeFrom = "";
    public static String currentTimeTo = "";
    public static String currentVehicleOption = "";

    public static final String ATTRACTIONS_TABLE = "attractions";
    public static final String ATTRACTION_NAME = "att_name";

    public static final String HOURS_TABLE = "hours_operation";
    ArrayList<Attraction> attractionsCity = new ArrayList<Attraction>();

    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table cities " +
                    //    "(id text primary key, city text, from_time text, to_time text, latitude double, longitude double, current integer)"
                    "(id text primary key, city text, latitude double, longitude double, current integer)"

        );

        /* create table with attractions */
        db.execSQL(
                "create table attractions " +
                        "(id text primary key, att_name text, att_description text, city text, from_time_week text, from_time_wend text, " +
                        "to_time_week text, to_time_wend text, latitude double, longitude double, custom integer)"
        );


        /*create table with tours */
        db.execSQL(
                "create table tours " +
                        "(tour_id text primary key, tour_name text, city_id text, from_time text, to_time text, vehicle text)"
        );
        db.execSQL(
                "create table toursToAtt " +
                        "(id text primary key, tour_id text, att_id text, time_to_spend double, first_attraction integer)"
        );

//         /* create table with attractions' hours of operation */
//        db.execSQL(
//                "create table hours_operation " +
//                        "(att_name text primary key, weekday text, weekend text)"
//        );
    }

    public String getCurrentVehicleOption() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from tours where tour_name = '" + currentTourName + "' " +
                "and city_id = '" + getCurrentCityId() + "'", null);
        if (res.getCount()>0) {
            res.moveToNext();
            return res.getString(res.getColumnIndex("vehicle"));
        }
        return  "";
    }

    public Attraction addAttToDB(Attraction att) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        Cursor res = db.rawQuery("select * from attractions where latitude = '" + att.latitude + "' " +
                "and longitude= '" +att.longitude + "'", null);

        if (res.getCount()<=0) {
            Log.i("DBHelper: ", "attraction is not in db");
            String uniqueID = UUID.randomUUID().toString();
            contentValues.put("id", uniqueID);
            contentValues.put(ATTRACTION_NAME, att.name);
            contentValues.put("att_description","" );
            contentValues.put(CITY_COLUMN_CITY, att.city);
            contentValues.put(CITY_COLUMN_FROM_TIME_WEEK, "00:00am");
            contentValues.put(CITY_COLUMN_FROM_TIME_WEND, "00:00am");
            contentValues.put(CITY_COLUMN_TO_TIME_WEEK, "00:00am");
            contentValues.put(CITY_COLUMN_TO_TIME_WEND, "00:00am");
            contentValues.put("custom", 1);
            contentValues.put(LATITUDE, att.latitude);
            contentValues.put(LONGITUDE, att.longitude);
            db.insert(ATTRACTIONS_TABLE, null, contentValues);
            att.id = uniqueID;
        }
        return att;

    }

    public boolean addTour(String tourName, ArrayList<Attraction> attractions, Attraction firstAttraction) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("tour_name", tourName);
        contentValues.put("city_id", this.getCurrentCityId());
        contentValues.put("from_time", currentTimeFrom);
        contentValues.put("to_time", currentTimeTo);
        contentValues.put("vehicle", currentVehicleOption);
        Cursor res = db.rawQuery("select * from tours where tour_name = '" + currentTourName + "' " +
                "and city_id = '" + getCurrentCityId() + "'", null);

        if (res.getCount()<=0) {
            Log.i("DBHelper: ", "creating new tour in the database");
            /* first create tour */
            String uniqueIDTour = UUID.randomUUID().toString();
            Log.i("DBHelper: ", "created unique ID is " + uniqueIDTour);
            contentValues.put("tour_id", uniqueIDTour);
            db.insert("tours", null, contentValues);

            for (Attraction a : attractions) {
                /*second create tour to attraction relation */
                int valueFirst = 0;
                if (a.longitude == firstAttraction.longitude && a.latitude == firstAttraction.latitude) {
                    valueFirst = 1;
                    Log.i("DB Helper", "this attraction goes first " + a.name);
                }

                contentValues = new ContentValues();
                String uniqueIDTA = UUID.randomUUID().toString();
                contentValues.put("first_attraction", valueFirst);
                contentValues.put("id", uniqueIDTA);
                contentValues.put("tour_id", uniqueIDTour);
                contentValues.put("att_id", a.id);
                contentValues.put("time_to_spend", a.time_to_spend);
                db.insert("toursToAtt", null, contentValues);
            }
            return true;
        }
        /* tour already exists
            update tour info in tour table
            replace info in tours to attraction table
         */
        else {
            Log.i("DBHelper: ", "Updating old tour");
            db.update("tours", contentValues, "tour_name = '" + currentTourName + "'", null );
            Log.i("DBHelper: ", "Tour name is " + currentTourName + "city id is " + this.getCurrentCityId());
            Log.i("DBHelper: ", "Tour ID is " + getTourIdByNameNCityId(currentTourName, this.getCurrentCityId()));
            db.execSQL("delete from toursToAtt where tour_id = '" + getTourIdByNameNCityId(currentTourName, this.getCurrentCityId()) + "'");
            for (Attraction a : attractions) {
                int valueFirst = 0;
                if (a.longitude == firstAttraction.longitude && a.latitude == firstAttraction.latitude) {
                    valueFirst = 1;
                    Log.i("DB Helper", "this attraction goes first " + a.name);
                }

                contentValues = new ContentValues();
                String uniqueIDTA = UUID.randomUUID().toString();
                contentValues.put("first_attraction", valueFirst);
                contentValues.put("id", uniqueIDTA);
                contentValues.put("tour_id", getTourIdByNameNCityId(currentTourName, this.getCurrentCityId()));
                contentValues.put("att_id", a.id);
                contentValues.put("time_to_spend", a.time_to_spend);
                db.insert("toursToAtt", null, contentValues);

            }
        }
        return true;
    }


    public void removeCurrentTour() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        Log.i("DBHelper: ", "current tour name " + currentTourName);
        Log.i("DBHelper: ", "current city is " + getCurrent());
        Log.i("DBHelper: ", "currentTourID " + getTourIdByNameNCityId(currentTourName, this.getCurrentCityId()));
        db.execSQL("delete from toursToAtt where tour_id = '" + getTourIdByNameNCityId(currentTourName, this.getCurrentCityId()) + "'");
        db.execSQL("delete from tours where tour_id = '" + getTourIdByNameNCityId(currentTourName, this.getCurrentCityId()) + "'");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS cities");
        db.execSQL("DROP TABLE IF EXISTS attractions");
        onCreate(db);
    }

    public Attraction firstAttractionInTour() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from toursToAtt where tour_id = '" + getTourIdByNameNCityId(currentTourName, getCurrentCityId()) + "' and " +
                "first_attraction = '" + 1 + "'", null);

        if(res!=null && res.getCount()>0) {
            Log.i("DBHelper", "If we got here we are able to get tour id ");
            res.moveToNext();
            String thisId = res.getString(res.getColumnIndex("att_id"));
            Cursor otherRes = db.rawQuery("select * from attractions where id = '" + thisId + "'", null);
            otherRes.moveToFirst();
            Attraction newAttraction = new Attraction(otherRes.getString(otherRes.getColumnIndex("id")),
                    otherRes.getString(otherRes.getColumnIndex(ATTRACTION_NAME)),
                    otherRes.getString(otherRes.getColumnIndex(CITY_COLUMN_CITY)), otherRes.getString(otherRes.getColumnIndex(CITY_COLUMN_FROM_TIME_WEEK)), otherRes.getString(otherRes.getColumnIndex(CITY_COLUMN_FROM_TIME_WEND)),
                    otherRes.getString(otherRes.getColumnIndex(CITY_COLUMN_TO_TIME_WEEK)), otherRes.getString(otherRes.getColumnIndex(CITY_COLUMN_TO_TIME_WEND)),
                    otherRes.getDouble(otherRes.getColumnIndex(LATITUDE)), otherRes.getDouble(otherRes.getColumnIndex(LONGITUDE)),
                    res.getLong(res.getColumnIndex("time_to_spend")));
            return  newAttraction;

        }
        return null;
    }

    public ArrayList<Attraction> getAttractionsForCurrentTour () {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Attraction> attractionsForCurrent = new ArrayList<Attraction>();
        Cursor res = db.rawQuery("select * from toursToAtt where tour_id = '" + getTourIdByNameNCityId(currentTourName, getCurrentCityId()) + "'", null);
        if(res!=null && res.getCount()>0) {
            for (int i=0;i<res.getCount();i++) {
                res.moveToNext();
                String thisId = res.getString(res.getColumnIndex("att_id"));
                Cursor otherRes = db.rawQuery("select * from attractions where id = '" + thisId + "'", null);
                otherRes.moveToFirst();
                 /*create attraction object */
                Attraction newAttraction = new Attraction(otherRes.getString(otherRes.getColumnIndex("id")),
                        otherRes.getString(otherRes.getColumnIndex(ATTRACTION_NAME)),
                        otherRes.getString(otherRes.getColumnIndex(CITY_COLUMN_CITY)), otherRes.getString(otherRes.getColumnIndex(CITY_COLUMN_FROM_TIME_WEEK)), otherRes.getString(otherRes.getColumnIndex(CITY_COLUMN_FROM_TIME_WEND)),
                        otherRes.getString(otherRes.getColumnIndex(CITY_COLUMN_TO_TIME_WEEK)), otherRes.getString(otherRes.getColumnIndex(CITY_COLUMN_TO_TIME_WEND)),
                        otherRes.getDouble(otherRes.getColumnIndex(LATITUDE)), otherRes.getDouble(otherRes.getColumnIndex(LONGITUDE)),
                        res.getLong(res.getColumnIndex("time_to_spend")));
                attractionsForCurrent.add(newAttraction);
                Log.i("DB Helper", "Retrieving attractions for current tour: "+newAttraction.name);
            }
        }
        return  attractionsForCurrent;

    }

    public boolean addHours (String cityName, String from_time, String to_time) {
        SQLiteDatabase db = this.getWritableDatabase();
        currentTimeFrom = from_time;
        currentTimeTo = to_time;
        Log.i("DB Helper: ", "from time is " + currentTimeFrom);
        Log.i("DB Helper: ", "to time is "+ currentTimeTo);
        return true;
    }


    public ArrayList<String> getTourNamesForCurrentCity() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> toursForCurrent = new ArrayList<String>();
        String cityId = getCurrentCityId();
        Cursor res = db.rawQuery("select * from tours where city_id = '" + cityId + "'", null);
        if(res!=null && res.getCount()>0) {
            for (int i=0;i<res.getCount();i++) {
                res.moveToNext();
                if (!toursForCurrent.contains(res.getString(res.getColumnIndex("tour_name"))))
                    toursForCurrent.add(res.getString(res.getColumnIndex("tour_name")));
                   // Log.i("Attraction name ", toursForCurrent.get(i));
            }
        }
        return toursForCurrent;
    }

    /*TODO: sort by city */
    public ArrayList<String> getAllTourNames() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> toursForCurrent = new ArrayList<String>();
        Cursor res = db.rawQuery("select * from tours ", null);
        if(res!=null && res.getCount()>0) {
            for (int i = 0; i < res.getCount(); i++) {
                res.moveToNext();
                if (!toursForCurrent.contains(res.getString(res.getColumnIndex("tour_name")))) {
                    String cityId = res.getString(res.getColumnIndex("city_id"));
                    Cursor cityRes = db.rawQuery("select * from cities where id = '" + cityId + "'", null);
                    cityRes.moveToNext();
                    String toAdd = res.getString(res.getColumnIndex("tour_name"));
                    toAdd = toAdd + " (" + cityRes.getString(cityRes.getColumnIndex("city")) + ")";
                    toursForCurrent.add(toAdd);
                }
            }
        }
        return toursForCurrent;
    }

    public void setCurrentTourName(String tourName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from tours where tour_name = '" + tourName + "'", null);
        currentTourName = tourName;
        if(res!=null && res.getCount()>0) {
            res.moveToFirst();
            currentTourName = res.getString(res.getColumnIndex("tour_name"));
            Log.i("DB Helper", "****************Current Tour Name is " + currentTourName);

        }
    }

    public boolean toursExist() {
        SQLiteDatabase db = this.getReadableDatabase();
        String cityId = getCurrentCityId();
        Cursor res = db.rawQuery("select * from tours", null);
        if (res.getCount()>0) {
            return true;
        }
        return false;
    }

    public boolean toursNameExistsCurrCity(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from tours where city_id = '" + getCurrentCityId() + "' and " +
                "tour_name = '" + name + "'", null);
        if (res.getCount() >0 )
            return true;
        return false;
    }

    public String getFromTime() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from tours where city_id = '" + getCurrentCityId() + "' and " +
                "tour_name = '" + currentTourName + "'", null);
        Log.i("DB Helper:From time", Integer.toString(res.getCount()));
        if(res!=null && res.getCount()>0) {
            res.moveToFirst();
            return res.getString(res.getColumnIndex("from_time"));
        }
        return "00:00am";
    }

    public String getToTime() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from tours where city_id = '" + getCurrentCityId() + "' and " +
                "tour_name = '" + currentTourName + "'", null);
        if(res!=null && res.getCount()>0) {
            res.moveToFirst();
            return res.getString(res.getColumnIndex("to_time"));
        }
        return "00:00am";
    }

    /*method to help us generate db data */
    public boolean generateContentDB () {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        String uniqueID = UUID.randomUUID().toString();
        contentValues.put("id", uniqueID);
        contentValues.put("city", "Ottawa");
        contentValues.put(LATITUDE, 45.425600);
        contentValues.put(LONGITUDE, -75.698965);
        db.insert("cities", null, contentValues);

        /*create few other cities and see how it goes */
        contentValues = new ContentValues();
        uniqueID = UUID.randomUUID().toString();
        contentValues.put("id", uniqueID);
        contentValues.put("city", "Montreal");
        contentValues.put(LATITUDE, 45.521509);
        contentValues.put(LONGITUDE, -73.616178);
        db.insert("cities", null, contentValues);

        contentValues = new ContentValues();
        uniqueID = UUID.randomUUID().toString();
        contentValues.put("id", uniqueID);
        contentValues.put("city", "Vancouver");
        contentValues.put(LATITUDE, 49.252049);
        contentValues.put(LONGITUDE, -123.07150);
        db.insert("cities", null, contentValues);

        contentValues = new ContentValues();
        uniqueID = UUID.randomUUID().toString();
        contentValues.put("id", uniqueID);
        contentValues.put("city", "Toronto");
        contentValues.put(LATITUDE, 43.714631);
        contentValues.put(LONGITUDE, -79.388859);
        db.insert("cities", null, contentValues);


        /* generate attractions, start with Ottawa */
        contentValues = new ContentValues();
        uniqueID = UUID.randomUUID().toString();
        contentValues.put("id", uniqueID);
        contentValues.put(ATTRACTION_NAME, "Parliament Hill");
        contentValues.put(CITY_COLUMN_CITY, "Ottawa");
        contentValues.put("att_description","Grand, neo-Gothic complex hosting Canada's legislature, with artworks, lush grounds & tours." );
        contentValues.put(CITY_COLUMN_FROM_TIME_WEEK, "08:30am");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEND, "08:30am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEEK, "06:00pm");
        contentValues.put(CITY_COLUMN_TO_TIME_WEND, "06:00pm");
        contentValues.put("custom", 0);
        contentValues.put(LATITUDE, 45.4236);
        contentValues.put(LONGITUDE, -75.7009);
        db.insert(ATTRACTIONS_TABLE, null, contentValues);

        contentValues = new ContentValues();
        uniqueID = UUID.randomUUID().toString();
        contentValues.put("id", uniqueID);
        contentValues.put(ATTRACTION_NAME, "Canadian Museum of History");
        contentValues.put("att_description","Museum chronicling Canadian culture & history, with an IMAX theater & separate children's museum." );
        contentValues.put(CITY_COLUMN_CITY, "Ottawa");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEEK, "09:30am");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEND, "09:30am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEEK, "05:00pm");
        contentValues.put(CITY_COLUMN_TO_TIME_WEND, "05:00pm");
        contentValues.put("custom", 0);
        contentValues.put(LATITUDE, 45.4302);
        contentValues.put(LONGITUDE, -75.7092);
        db.insert(ATTRACTIONS_TABLE, null, contentValues);

        contentValues = new ContentValues();
        uniqueID = UUID.randomUUID().toString();
        contentValues.put("id", uniqueID);
        contentValues.put(ATTRACTION_NAME, "National Gallery of Canada");
        contentValues.put("att_description","This spacious museum focusing on Canadian art also features some international art & a cafeteria." );
        contentValues.put(CITY_COLUMN_CITY, "Ottawa");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEEK, "10:00am");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEND, "10:00am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEEK, "05:00pm");
        contentValues.put(CITY_COLUMN_TO_TIME_WEND, "05:00pm");
        contentValues.put("custom", 0);
        contentValues.put(LATITUDE, 45.4295);
        contentValues.put(LONGITUDE, -75.6989);
        db.insert(ATTRACTIONS_TABLE, null, contentValues);

        contentValues = new ContentValues();
        uniqueID = UUID.randomUUID().toString();
        contentValues.put("id", uniqueID);
        contentValues.put(ATTRACTION_NAME, "Rideau Hall");
        contentValues.put(CITY_COLUMN_CITY, "Ottawa");
        contentValues.put("att_description","This historic 1830s structure with landscaped grounds is the official home of the Governor General." );
//        contentValues.put(CITY_COLUMN_FROM_TIME_WEEK, "10:00am");
//        contentValues.put(CITY_COLUMN_FROM_TIME_WEND, "11:00am");
//        contentValues.put(CITY_COLUMN_TO_TIME_WEEK, "04:30pm");
//        contentValues.put(CITY_COLUMN_TO_TIME_WEND, "04:00pm");

        contentValues.put(CITY_COLUMN_FROM_TIME_WEEK, "11:00am");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEND, "11:00am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEEK, "05:00pm");
        contentValues.put(CITY_COLUMN_TO_TIME_WEND, "05:00pm");
        contentValues.put("custom", 0);
        contentValues.put(LATITUDE, 45.4445);
        contentValues.put(LONGITUDE, -75.6858);
        db.insert(ATTRACTIONS_TABLE, null, contentValues);

        contentValues = new ContentValues();
        uniqueID = UUID.randomUUID().toString();
        contentValues.put("id", uniqueID);
        contentValues.put(ATTRACTION_NAME, "Jacques Cartier Park");
        contentValues.put("att_description","23-hectare park, site of the Winterlude festival (Feb), offering recreational paths & river views." );
        contentValues.put(CITY_COLUMN_CITY, "Ottawa");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEEK, "00:00am");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEND, "00:00am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEEK, "00:00am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEND, "00:00am");
        contentValues.put("custom", 0);
        contentValues.put(LATITUDE, 45.4340);
        contentValues.put(LONGITUDE, -75.7079);
        db.insert(ATTRACTIONS_TABLE, null, contentValues);

        contentValues = new ContentValues();
        uniqueID = UUID.randomUUID().toString();
        contentValues.put("id", uniqueID);
        contentValues.put(ATTRACTION_NAME, "Dow's Lake");
        contentValues.put(CITY_COLUMN_CITY, "Ottawa");
        contentValues.put("att_description","Dow's Lake in Ottawa, Ontario, Canada is a small man-made lake on the Rideau Canal, situated two kilometres north of Hog's Back Falls in the middle of Ottawa." );
        contentValues.put(CITY_COLUMN_FROM_TIME_WEEK, "00:00am");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEND, "00:00am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEEK, "00:00am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEND, "00:00am");
        contentValues.put("custom", 0);
        contentValues.put(LATITUDE, 45.3948);
        contentValues.put(LONGITUDE, -75.7011);
        db.insert(ATTRACTIONS_TABLE, null, contentValues);

        contentValues = new ContentValues();
        uniqueID = UUID.randomUUID().toString();
        contentValues.put("id", uniqueID);
        contentValues.put(ATTRACTION_NAME, "Canada Science and Technology Museum");
        contentValues.put("att_description","Striking, modern museum with exhibits & programs devoted to Canadian science & technology." );
        contentValues.put(CITY_COLUMN_CITY, "Ottawa");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEEK, "09:00am");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEND, "09:00am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEEK, "05:00pm");
        contentValues.put(CITY_COLUMN_TO_TIME_WEND, "05:00pm");
        contentValues.put("custom", 0);
        contentValues.put(LATITUDE, 45.4023);
        contentValues.put(LONGITUDE, -75.6248);
        db.insert(ATTRACTIONS_TABLE, null, contentValues);

        contentValues = new ContentValues();
        uniqueID = UUID.randomUUID().toString();
        contentValues.put("id", uniqueID);
        contentValues.put(ATTRACTION_NAME, "Byward Market");
        contentValues.put("att_description","ByWard Market is a buzzing hub of outdoor farmers’ market stalls and specialty food shops selling Canadian cheese and maple-infused chocolate. " +
                "It’s also known for its colorful street art and hip stores filled with crafts and clothes by local designers." );
        contentValues.put(CITY_COLUMN_CITY, "Ottawa");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEEK, "00:00am");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEND, "00:00am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEEK, "00:00am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEND, "00:00am");
        contentValues.put("custom", 0);
        contentValues.put(LATITUDE, 45.4289);
        contentValues.put(LONGITUDE, -75.6912);
        db.insert(ATTRACTIONS_TABLE, null, contentValues);

        contentValues = new ContentValues();
        uniqueID = UUID.randomUUID().toString();
        contentValues.put("id", uniqueID);
        contentValues.put(ATTRACTION_NAME, "Dominion Arboretum");
        contentValues.put("att_description","Visitors can explore many gardens at the country's oldest arboretum, home to 10,000 kinds of plants." );
        contentValues.put(CITY_COLUMN_CITY, "Ottawa");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEEK, "09:00am");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEND, "08:00am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEEK, "04:00pm");
        contentValues.put(CITY_COLUMN_TO_TIME_WEND, "04:00pm");
        contentValues.put("custom", 0);
        contentValues.put(LATITUDE, 45.3910);
        contentValues.put(LONGITUDE, -75.7039);
        db.insert(ATTRACTIONS_TABLE, null, contentValues);

        contentValues = new ContentValues();
        uniqueID = UUID.randomUUID().toString();
        contentValues.put("id", uniqueID);
        contentValues.put(ATTRACTION_NAME, "Notre-Dame Cathedral Basilica");
        contentValues.put("att_description","Dating to the 19th century, this church features a colourful interior & skyline-dominating spires." );
        contentValues.put(CITY_COLUMN_CITY, "Ottawa");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEEK, "00:00am");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEND, "00:00am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEEK, "00:00am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEND, "00:00am");
        contentValues.put("custom", 0);
        contentValues.put(LATITUDE, 45.4299);
        contentValues.put(LONGITUDE, -75.6962);
        db.insert(ATTRACTIONS_TABLE, null, contentValues);

        contentValues = new ContentValues();
        uniqueID = UUID.randomUUID().toString();
        contentValues.put("id", uniqueID);
        contentValues.put(ATTRACTION_NAME, "Mount Royal");
        contentValues.put("att_description","Mount Royal is a large volcanic-related hill or small mountain in the city of Montreal, immediately west of Downtown Montreal, Quebec, Canada. Mount Royal gave its name to Montreal" );
        contentValues.put(CITY_COLUMN_CITY, "Montreal");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEEK, "00:00am");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEND, "00:00am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEEK, "00:00am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEND, "00:00am");
        contentValues.put("custom", 0);
        contentValues.put(LATITUDE, 45.5071);
        contentValues.put(LONGITUDE, -73.5874);
        db.insert(ATTRACTIONS_TABLE, null, contentValues);

        contentValues = new ContentValues();
        uniqueID = UUID.randomUUID().toString();
        contentValues.put("id", uniqueID);
        contentValues.put(ATTRACTION_NAME, "Montreal Botanical Garden");
        contentValues.put("att_description","The Montreal Botanical Garden is a large botanical garden in Montreal, Quebec, Canada comprising 75 hectares of thematic gardens and greenhouses." );
        contentValues.put(CITY_COLUMN_CITY, "Montreal");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEEK, "09:00am");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEND, "09:00am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEEK, "05:00pm");
        contentValues.put(CITY_COLUMN_TO_TIME_WEND, "05:00pm");
        contentValues.put("custom", 0);
        contentValues.put(LATITUDE, 45.5600);
        contentValues.put(LONGITUDE, -73.5630);
        db.insert(ATTRACTIONS_TABLE, null, contentValues);

        contentValues = new ContentValues();
        uniqueID = UUID.randomUUID().toString();
        contentValues.put("id", uniqueID);
        contentValues.put(ATTRACTION_NAME, "Saint Joseph's Oratory");
        contentValues.put("att_description","Saint Joseph's Oratory of Mount Royal is a Roman Catholic minor basilica and national shrine on Mount Royal's Westmount Summit in Montreal, Quebec. It is Canada's largest church and claims to have one of the largest domes in the world." );
        contentValues.put(CITY_COLUMN_CITY, "Montreal");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEEK, "06:00am");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEND, "06:00am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEEK, "09:00pm");
        contentValues.put(CITY_COLUMN_TO_TIME_WEND, "09:00pm");
        contentValues.put("custom", 0);
        contentValues.put(LATITUDE, 45.4926);
        contentValues.put(LONGITUDE, -73.6183);
        db.insert(ATTRACTIONS_TABLE, null, contentValues);

        contentValues = new ContentValues();
        uniqueID = UUID.randomUUID().toString();
        contentValues.put("id", uniqueID);
        contentValues.put(ATTRACTION_NAME, "Notre-Dame Basilica");
        contentValues.put("att_description","Notre-Dame Basilica is a basilica in the historic district of Old Montreal, in Montreal, Quebec, Canada." );
        contentValues.put(CITY_COLUMN_CITY, "Montreal");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEEK, "00:00am");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEND, "00:00am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEEK, "00:00am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEND, "00:00am");
        contentValues.put("custom", 0);
        contentValues.put(LATITUDE, 45.5045);
        contentValues.put(LONGITUDE, -73.5561);
        db.insert(ATTRACTIONS_TABLE, null, contentValues);

        contentValues = new ContentValues();
        uniqueID = UUID.randomUUID().toString();
        contentValues.put("id", uniqueID);
        contentValues.put(ATTRACTION_NAME, "Old Montreal");
        contentValues.put("att_description","Old Montreal is the oldest area in the city of Montreal, Quebec, Canada, with a few remains dating back to New France." );
        contentValues.put(CITY_COLUMN_CITY, "Montreal");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEEK, "00:00am");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEND, "00:00am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEEK, "00:00am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEND, "00:00am");
        contentValues.put("custom", 0);
        contentValues.put(LATITUDE, 45.5075);
        contentValues.put(LONGITUDE, -73.5544);
        db.insert(ATTRACTIONS_TABLE, null, contentValues);

        contentValues = new ContentValues();
        uniqueID = UUID.randomUUID().toString();
        contentValues.put("id", uniqueID);
        contentValues.put(ATTRACTION_NAME, "Montreal Museum of Fine Arts");
        contentValues.put("att_description","Spacious museum showcasing Québec & Canadian visual works, plus international contemporary art." );
        contentValues.put(CITY_COLUMN_CITY, "Montreal");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEEK, "10:00am");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEND, "10:00am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEEK, "05:00pm");
        contentValues.put(CITY_COLUMN_TO_TIME_WEND, "05:00pm");
        contentValues.put("custom", 0);
        contentValues.put(LATITUDE, 45.4985);
        contentValues.put(LONGITUDE, -73.5794);
        db.insert(ATTRACTIONS_TABLE, null, contentValues);

        contentValues = new ContentValues();
        uniqueID = UUID.randomUUID().toString();
        contentValues.put("id", uniqueID);
        contentValues.put(ATTRACTION_NAME, "La Ronde");
        contentValues.put("att_description","Riverside amusement park with Six Flags rides, the International Fireworks Competition & concerts." );
        contentValues.put(CITY_COLUMN_CITY, "Montreal");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEEK, "10:00am");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEND, "10:00am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEEK, "05:00pm");
        contentValues.put(CITY_COLUMN_TO_TIME_WEND, "05:00pm");
        contentValues.put("custom", 0);
        contentValues.put(LATITUDE, 45.5227);
        contentValues.put(LONGITUDE, -73.5345);
        db.insert(ATTRACTIONS_TABLE, null, contentValues);

        contentValues = new ContentValues();
        uniqueID = UUID.randomUUID().toString();
        contentValues.put("id", uniqueID);
        contentValues.put(ATTRACTION_NAME, "Parc Jean-Drapeau");
        contentValues.put("att_description","Park made up of 2 islands that are home to multiple attractions, venues & museums." );
        contentValues.put(CITY_COLUMN_CITY, "Montreal");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEEK, "06:00am");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEND, "06:00am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEEK, "10:00pm");
        contentValues.put(CITY_COLUMN_TO_TIME_WEND, "10:00pm");
        contentValues.put("custom", 0);
        contentValues.put(LATITUDE, 45.5171);
        contentValues.put(LONGITUDE, -73.5336);
        db.insert(ATTRACTIONS_TABLE, null, contentValues);
        return true;
    }



     public ArrayList<Attraction> getAttractions() {
         SQLiteDatabase db = this.getReadableDatabase();
         attractionsCity = new ArrayList<Attraction>();
         String cityName = getCurrent();
         Cursor res = db.rawQuery("select * from attractions where city = '" + cityName + "'"  +
                 "and custom = '" + 0 + "'", null);
         if(res!=null && res.getCount()>0) {
             for (int i=0;i<res.getCount();i++) {
                 res.moveToNext();
                 /*create attraction object */
                 Attraction newAttraction = new Attraction(res.getString(res.getColumnIndex("id")),
                         res.getString(res.getColumnIndex(ATTRACTION_NAME)),
                         res.getString(res.getColumnIndex(CITY_COLUMN_CITY)), res.getString(res.getColumnIndex(CITY_COLUMN_FROM_TIME_WEEK)), res.getString(res.getColumnIndex(CITY_COLUMN_FROM_TIME_WEND)),
                         res.getString(res.getColumnIndex(CITY_COLUMN_TO_TIME_WEEK)), res.getString(res.getColumnIndex(CITY_COLUMN_TO_TIME_WEND)),
                         res.getDouble(res.getColumnIndex(LATITUDE)), res.getDouble(res.getColumnIndex(LONGITUDE)),
                         0);
                 newAttraction.description = res.getString(res.getColumnIndex("att_description"));
                 attractionsCity.add(newAttraction);
             }
         }

         return attractionsCity;
    }

    public double getLatitude() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from cities where current=1", null);
        if(res!=null && res.getCount()>0) {
            res.moveToFirst();
            return res.getDouble(res.getColumnIndex(LATITUDE));
        }
        return 0.0;
    }

    public double setLongitude() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from cities where current = 1", null);
        if (res!=null && res.getCount()>0) {
            res.moveToFirst();
            return res.getDouble(res.getColumnIndex(LONGITUDE));
        }
        return 0.0;
    }


    public void markCurrent(String cityName) {
        SQLiteDatabase db = this.getWritableDatabase();

        /* set any other possible current city to not be */
        ContentValues contentValues = new ContentValues();
        contentValues.put(CURRENT, 0);
        db.update("cities", contentValues, "current = 1", null );

        /*set passed city to be new current city */
        contentValues = new ContentValues();
        contentValues.put("city", cityName);
        contentValues.put(CURRENT, 1);
        db.update("cities", contentValues, "city = '" + cityName + "'", null );

    }

    public String getCurrent() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from cities where current = 1", null);
        if(res!=null && res.getCount()>0) {
            res.moveToFirst();
            return res.getString(res.getColumnIndex(CITY_COLUMN_CITY));
        }
        return "";
    }

    public String getCurrentCityId() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from cities where current = 1", null);
        if(res!=null && res.getCount()>0) {
            res.moveToFirst();
            return res.getString(res.getColumnIndex("id"));
        }
        return "";
    }

    public String getTourIdByNameNCityId(String tour_name, String city_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from tours where tour_name = '" + tour_name + "' " +
                "and city_id = '" + city_id + "'", null);
        if(res!=null && res.getCount()>0) {
            res.moveToFirst();
            return res.getString(res.getColumnIndex("tour_id"));
        }
        return "";
    }

    public double getPopularTimeByAttName(String att_name) {
        SQLiteDatabase db = this.getReadableDatabase();
        attractionsCity = new ArrayList<Attraction>();
        String cityName = getCurrent();
        Cursor res = db.rawQuery("select * from attractions where att_name = '" + att_name + "'", null);
        if(res!=null && res.getCount()>0) {
            res.moveToFirst();
            return res.getDouble(res.getColumnIndex("time_to_spend"));
        }
        return 0.0;
    }

    /*used to clean up when db changes */
    public void dropDB () {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("DROP TABLE IF EXISTS cities");
        db.execSQL("DROP TABLE IF EXISTS attractions");
        db.execSQL("DROP TABLE IF EXISTS tours");
        db.execSQL("DROP TABLE IF EXISTS toursToAtt");
       //db.execSQL("DROP TABLE IF EXISTS hours_operation");
        onCreate(db);
    }

//    public boolean tablesExist() {
//        SQLiteDatabase db = this.getReadableDatabase();
//        db.execSQL("IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES  WHERE TABLE_NAME='mytablename')");
//    }

    public Date parseTime(String time) {
        String timeToParse = time.replaceAll(" ", "");
        SimpleDateFormat inFormat = new SimpleDateFormat("hh:mmaa");
        try {
            return inFormat.parse(timeToParse);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public long timeDifference(Date endTime, Date startTime, TimeUnit timeUnit) {
         long difference = endTime.getTime() - startTime.getTime();
         return timeUnit.convert(difference, TimeUnit.MILLISECONDS);
    }
}

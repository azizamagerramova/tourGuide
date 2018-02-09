package com.example.aziza.tourguide;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.PreparedStatement;
import java.util.ArrayList;

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


    public static final String ATTRACTIONS_TABLE = "attractions";
    public static final String ATTRACTION_NAME = "att_name";

    public static final String HOURS_TABLE = "hours_operation";
    ArrayList<Attraction> attractionsCity = new ArrayList<Attraction>();

    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table cities " +
                        "(city text primary key, from_time text, to_time text, latitude double, longitude double, current integer)"
        );

        /* create table with attractions */
        db.execSQL(
                "create table attractions " +
                        "(att_name text primary key, city text, from_time_week text, from_time_wend text, " +
                        "to_time_week text, to_time_wend text, latitude double, longitude double)"
        );

//         /* create table with attractions' hours of operation */
//        db.execSQL(
//                "create table hours_operation " +
//                        "(att_name text primary key, weekday text, weekend text)"
//        );


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS cities");
        db.execSQL("DROP TABLE IF EXISTS attractions");
        onCreate(db);
    }

    public boolean addHours (String cityName, String from_time, String to_time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("city", cityName);
        contentValues.put("from_time", from_time);
        contentValues.put("to_time", to_time);
        Cursor res = db.rawQuery("select * from cities where city = '" + cityName + "'", null);
        Log.i("DB Helper", "number rows " + res.getCount());

        /*shoud not ever get here, but just in case */
        if (res.getCount()<=0) {
            db.insert("cities", null, contentValues);
            return true;
        }
        Log.i("DB Helper", "Got into the else clause");
        int id = 0;
        db.update("cities", contentValues, "city = '" + cityName + "'", null );

        return true;
    }

    public String getFromTime(String city) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from cities where city = '" + city + "'", null);
        Log.i("DB Helper", Integer.toString(res.getCount()));
        if(res!=null && res.getCount()>0) {
            res.moveToFirst();
            return res.getString(res.getColumnIndex(CITY_COLUMN_FROM_TIME));
        }
        return "00:00am";
    }

    public String getToTime(String city) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from cities where city = '" + city + "'", null);
        if(res!=null && res.getCount()>0) {
            res.moveToFirst();
            return res.getString(res.getColumnIndex(CITY_COLUMN_TO_TIME));
        }
        return "00:00am";
    }

    /*method to help us generate db data */
    public boolean generateContentDB () {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("city", "Ottawa");
        contentValues.put("from_time", "00:00am");
        contentValues.put("to_time", "00:00am");
        contentValues.put(LATITUDE, 45.425600);
        contentValues.put(LONGITUDE, -75.698965);
        db.insert("cities", null, contentValues);

        /*create few other cities and see how it goes */
        contentValues = new ContentValues();
        contentValues.put("city", "Montreal");
        contentValues.put("from_time", "00:00am");
        contentValues.put("to_time", "00:00am");
        contentValues.put(LATITUDE, 45.521509);
        contentValues.put(LONGITUDE, -73.616178);
        db.insert("cities", null, contentValues);

        contentValues = new ContentValues();
        contentValues.put("city", "Vancouver");
        contentValues.put("from_time", "00:00am");
        contentValues.put("to_time", "00:00am");
        contentValues.put(LATITUDE, 49.252049);
        contentValues.put(LONGITUDE, -123.07150);
        db.insert("cities", null, contentValues);

        contentValues = new ContentValues();
        contentValues.put("city", "Toronto");
        contentValues.put("from_time", "00:00am");
        contentValues.put("to_time", "00:00am");
        contentValues.put(LATITUDE, 43.714631);
        contentValues.put(LONGITUDE, -79.388859);
        db.insert("cities", null, contentValues);


        /* generate attractions, start with Ottawa */
        contentValues = new ContentValues();
        contentValues.put(ATTRACTION_NAME, "Parliament Hill");
        contentValues.put(CITY_COLUMN_CITY, "Ottawa");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEEK, "08:30am");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEND, "cl");
        contentValues.put(CITY_COLUMN_TO_TIME_WEEK, "06:00pm");
        contentValues.put(CITY_COLUMN_TO_TIME_WEND, "cl");
        contentValues.put(LATITUDE, 45.4236);
        contentValues.put(LONGITUDE, -75.7009);
        db.insert(ATTRACTIONS_TABLE, null, contentValues);

        contentValues = new ContentValues();
        contentValues.put(ATTRACTION_NAME, "Canadian Museum of History");
        contentValues.put(CITY_COLUMN_CITY, "Ottawa");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEEK, "09:30am");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEND, "09:30am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEEK, "05:00pm");
        contentValues.put(CITY_COLUMN_TO_TIME_WEND, "05:00pm");
        contentValues.put(LATITUDE, 45.4302);
        contentValues.put(LONGITUDE, -75.7092);
        db.insert(ATTRACTIONS_TABLE, null, contentValues);

        contentValues = new ContentValues();
        contentValues.put(ATTRACTION_NAME, "National Gallery of Canada");
        contentValues.put(CITY_COLUMN_CITY, "Ottawa");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEEK, "10:00am");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEND, "10:00am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEEK, "05:00pm");
        contentValues.put(CITY_COLUMN_TO_TIME_WEND, "05:00pm");
        contentValues.put(LATITUDE, 45.4295);
        contentValues.put(LONGITUDE, -75.6989);
        db.insert(ATTRACTIONS_TABLE, null, contentValues);

        contentValues = new ContentValues();
        contentValues.put(ATTRACTION_NAME, "Rideau Hall");
        contentValues.put(CITY_COLUMN_CITY, "Ottawa");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEEK, "11:00am");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEND, "11:00am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEEK, "04:00pm");
        contentValues.put(CITY_COLUMN_TO_TIME_WEND, "04:00pm");
        contentValues.put(LATITUDE, 45.4445);
        contentValues.put(LONGITUDE, -75.6858);
        db.insert(ATTRACTIONS_TABLE, null, contentValues);

        contentValues = new ContentValues();
        contentValues.put(ATTRACTION_NAME, "Jacques Cartier Park");
        contentValues.put(CITY_COLUMN_CITY, "Ottawa");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEEK, "00:00am");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEND, "00:00am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEEK, "00:00am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEND, "00:00am");
        contentValues.put(LATITUDE, 45.4340);
        contentValues.put(LONGITUDE, -75.7079);
        db.insert(ATTRACTIONS_TABLE, null, contentValues);

        contentValues = new ContentValues();
        contentValues.put(ATTRACTION_NAME, "Dow's Lake");
        contentValues.put(CITY_COLUMN_CITY, "Ottawa");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEEK, "00:00am");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEND, "00:00am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEEK, "00:00am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEND, "00:00am");
        contentValues.put(LATITUDE, 45.3948);
        contentValues.put(LONGITUDE, -75.7011);
        db.insert(ATTRACTIONS_TABLE, null, contentValues);

        contentValues = new ContentValues();
        contentValues.put(ATTRACTION_NAME, "Canada Science and Technology Museum");
        contentValues.put(CITY_COLUMN_CITY, "Ottawa");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEEK, "09:00am");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEND, "09:00am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEEK, "05:00pm");
        contentValues.put(CITY_COLUMN_TO_TIME_WEND, "05:00pm");
        contentValues.put(LATITUDE, 45.4023);
        contentValues.put(LONGITUDE, -75.6248);
        db.insert(ATTRACTIONS_TABLE, null, contentValues);

        contentValues = new ContentValues();
        contentValues.put(ATTRACTION_NAME, "Byward Market");
        contentValues.put(CITY_COLUMN_CITY, "Ottawa");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEEK, "00:00am");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEND, "00:00am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEEK, "00:00am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEND, "00:00am");
        contentValues.put(LATITUDE, 45.4289);
        contentValues.put(LONGITUDE, -75.6912);
        db.insert(ATTRACTIONS_TABLE, null, contentValues);

        contentValues = new ContentValues();
        contentValues.put(ATTRACTION_NAME, "Dominion Arboretum");
        contentValues.put(CITY_COLUMN_CITY, "Ottawa");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEEK, "09:00am");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEND, "08:00am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEEK, "04:00pm");
        contentValues.put(CITY_COLUMN_TO_TIME_WEND, "04:00pm");
        contentValues.put(LATITUDE, 45.4289);
        contentValues.put(LONGITUDE, -75.6912);
        db.insert(ATTRACTIONS_TABLE, null, contentValues);

        contentValues = new ContentValues();
        contentValues.put(ATTRACTION_NAME, "Notre-Dame Cathedral Basilica");
        contentValues.put(CITY_COLUMN_CITY, "Ottawa");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEEK, "00:00am");
        contentValues.put(CITY_COLUMN_FROM_TIME_WEND, "00:00am");
        contentValues.put(CITY_COLUMN_TO_TIME_WEEK, "00:00pm");
        contentValues.put(CITY_COLUMN_TO_TIME_WEND, "00:00pm");
        contentValues.put(LATITUDE, 45.4299);
        contentValues.put(LONGITUDE, -75.6962);
        db.insert(ATTRACTIONS_TABLE, null, contentValues);
        return true;
    }



     public ArrayList<Attraction> getAttractions() {
         SQLiteDatabase db = this.getReadableDatabase();
         attractionsCity = new ArrayList<Attraction>();
         String cityName = getCurrent();
         Cursor res = db.rawQuery("select * from attractions where city = '" + cityName + "'", null);
         if(res!=null && res.getCount()>0) {
             for (int i=0;i<res.getCount();i++) {
                 res.moveToNext();
                 /*create attraction object */
                 Attraction newAttraction = new Attraction(res.getString(res.getColumnIndex(ATTRACTION_NAME)),
                         res.getString(res.getColumnIndex(CITY_COLUMN_CITY)), res.getString(res.getColumnIndex(CITY_COLUMN_FROM_TIME_WEEK)), res.getString(res.getColumnIndex(CITY_COLUMN_FROM_TIME_WEND)),
                         res.getString(res.getColumnIndex(CITY_COLUMN_TO_TIME_WEEK)), res.getString(res.getColumnIndex(CITY_COLUMN_TO_TIME_WEND)),
                         res.getDouble(res.getColumnIndex(LATITUDE)), res.getDouble(res.getColumnIndex(LONGITUDE)));
                 attractionsCity.add(newAttraction);
             }
         }

         for (Attraction a: attractionsCity) {
             Log.i("DB Helper: ", a.name);
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
            Log.i("DB HELPER", res.getString(res.getColumnIndex(LONGITUDE)));
            return res.getDouble(res.getColumnIndex(LONGITUDE));
        }
        return 0.0;
    }


    /* to do: set others to be marked as none */
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
        Log.i("DB Helper", "There exists current");
        if(res!=null && res.getCount()>0) {
            Log.i("DB Helper", "There exists current");
            res.moveToFirst();
            return res.getString(res.getColumnIndex(CITY_COLUMN_CITY));
        }
        return "";
    }

    /*used to clean up when db changes */
    public void dropDB () {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("DROP TABLE IF EXISTS cities");
        db.execSQL("DROP TABLE IF EXISTS attractions");
    //    db.execSQL("DROP TABLE IF EXISTS hours_operation");
        onCreate(db);
    }
}

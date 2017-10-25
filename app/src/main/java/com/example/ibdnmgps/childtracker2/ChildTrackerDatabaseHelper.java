package com.example.ibdnmgps.childtracker2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.type;
import static android.provider.Contacts.SettingsColumns.KEY;

/**
 * Created by ibdnmgps on 4/15/2017.
 */

public class ChildTrackerDatabaseHelper extends SQLiteOpenHelper {

    // Logcat tag
     static final String LOG = "DatabaseHelper";

    // Database Version
     static final int DATABASE_VERSION = 1;

    // Database Name
     static final String DATABASE_NAME = "childtrackerdb";

    // Table Names
     static final String TABLE_CHILD = "child";
     static final String TABLE_PARENT = "parent";
     static final String TABLE_SAFEZONE = "safezone";
     static final String TABLE_CURFEW = "curfew";
     static final String TABLE_CURFEW_DAY = "curfew_day";
     static final String TABLE_LOCATION = "location";
     static final String TABLE_PARENT_CHILD = "parent_child";
     static final String TABLE_CHILDTRACKER_FILES = "files";

    // Common column names
     static final String KEY_ID = "_id";
     static final String KEY_PHONE = "phone_num";
     static final String KEY_CHILD_ID = "child_id";
     static final String KEY_LATITUDE = "latitude";
     static final String KEY_LONGITUDE = "longitude";

    // CHILD Table - column names
     static final String KEY_NAME = "name";
     static final String KEY_PARENT = "name";


    // PARENT Table - column names
     static final String KEY_PASSWORD = "password";


    // SAFEZONE Table - column names
     static final String KEY_RADIUS = "radius";
     static final String KEY_IS_HOME = "isHome";


    // CURFEW Table - column names
     static final String KEY_START = "start";
     static final String KEY_END = "end";

    // CURFEW_DAY Table - column names
     static final String KEY_DAY = "day";
     static final String KEY_CURFEW_ID = "curfew_id";

    //LOCATION tahle - column names
     static final String KEY_TIME_CREATED = "time_created";

    //PARENT_CHILD table - column names
     static final String KEY_PARENT_ID = "parent_id";

     static final String KEY_PIN_CODE = "pin_code";
     static final String KEY_FIRST = "is_first"; // 1 if yes, 0 if no
     static final String KEY_DEVICE = "device"; // parent_dev, child_dev
     static final String KEY_MODE = "mode"; // child_mod, parent_mod
     static final String KEY_CHILD_REF = "child_ref";
     static final String KEY_OFFLINE_SEND = "is_sms";
     static final String KEY_INT_OFF = "int_off";
     static final String KEY_INT_ON = "int_on";

    // Table Create Statements
    // ChiltTrackerFiles table create statement
    private static final String CREATE_TABLE_CHILDTRACKER_FILES = "CREATE TABLE "
            + TABLE_CHILDTRACKER_FILES + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_PIN_CODE
            + " TEXT," + KEY_FIRST + " INTEGER," + KEY_DEVICE + " TEXT, "  + KEY_MODE + " TEXT, "
            + KEY_OFFLINE_SEND + " INTEGER, " +  KEY_INT_ON + " INTEGER, "  + KEY_INT_OFF + " TEXT, " +
            KEY_CHILD_REF + " INTEGER)";


    private static final String CREATE_TABLE_CHILD = "CREATE TABLE "
            + TABLE_CHILD + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME
            + " TEXT," + KEY_PHONE + " TEXT UNIQUE," + KEY_PARENT + " INTEGER  )";

    // Parent table create statement
    private static final String CREATE_TABLE_PARENT = "CREATE TABLE " + TABLE_PARENT
            + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_PHONE + " TEXT UNIQUE,"
            + KEY_PASSWORD + " TEXT" + ")";

    // parent_child table create statement
    private static final String CREATE_TABLE_PARENT_CHILD = "CREATE TABLE " + TABLE_PARENT_CHILD
            + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_PARENT_ID + " INTEGER,"
            + KEY_CHILD_ID + " INTEGER" + ")";


    // Safezone table create statement
    private static final String CREATE_TABLE_SAFEZONE = "CREATE TABLE "
            + TABLE_SAFEZONE + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_LATITUDE + " DOUBLE," + KEY_LONGITUDE + " DOUBLE,"
            + KEY_RADIUS + " DOUBLE" + KEY_CHILD_ID + " INTEGER," + KEY_IS_HOME + " INT" + " )";

    // Curfew table create statement
    private static final String CREATE_TABLE_CURFEW = "CREATE TABLE "
            + TABLE_CURFEW + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_START + " TIMESTAMP," + KEY_END + " DATETIME,"
            + KEY_CHILD_ID + " INTEGER )";

    // Curfew_day table create statement
    private static final String CREATE_TABLE_CURFEW_DAY = "CREATE TABLE "
            + TABLE_CURFEW_DAY + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_DAY + " TEXT," + KEY_CURFEW_ID + " INTEGER )";

    // Safezone table create statement
    private static final String CREATE_TABLE_LOCATION = "CREATE TABLE "
            + TABLE_LOCATION + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_LATITUDE + " DOUBLE," + KEY_LONGITUDE + " DOUBLE,"
            + KEY_CHILD_ID + " INTEGER )";


    ChildTrackerDatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
     /*   db.execSQL(CREATE_TABLE_CHILD);
        db.execSQL(CREATE_TABLE_PARENT);
        db.execSQL(CREATE_TABLE_SAFEZONE);
        db.execSQL(CREATE_TABLE_CURFEW);
        db.execSQL(CREATE_TABLE_LOCATION); */
        db.execSQL(CREATE_TABLE_CHILDTRACKER_FILES);
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }

    /*
 * Creating a Child
 */
    public long createChild(Child child, long parent_id) {
        long child_id = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, child.getName());
        values.put(KEY_PHONE, child.getPhoneNum());

        // insert row restrict same child phone num
        if(getChild(child.getPhoneNum()) == null) {
            if(getParentChild(parent_id,child_id) == null)
                child_id = db.insert(TABLE_CHILD, null, values);
            ParentChild parent_child = new ParentChild(parent_id,child_id);
            createParentChild(parent_child);
        }

        db.close();

        return child_id;
    }

    public long updateChildTracker(ContentValues cv) {
        long id = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        id = db.update(TABLE_CHILDTRACKER_FILES, cv, "_id=1", null);
        db.close();
        return id;
    }

    public long initializeFiles(){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_FIRST, 1);
        values.put(KEY_MODE, "parent_mod");
        values.put(KEY_PIN_CODE, "");
        values.put(KEY_DEVICE, "");
        values.put(KEY_CHILD_REF, "");
        values.put(KEY_INT_OFF, 300000);
        values.put(KEY_INT_ON, 300000);
        values.put(KEY_OFFLINE_SEND, 0);
        long f = db.insert(TABLE_CHILDTRACKER_FILES, null, values);
        db.close();
        return  f;
    }

    public String getFiles(String type){
        SQLiteDatabase db = this.getReadableDatabase();


        String selectQuery = "SELECT  * FROM " + TABLE_CHILDTRACKER_FILES ;
        String key = "";
        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);
        while(c.getCount() <= 0){
            initializeFiles();
            c = db.rawQuery(selectQuery, null);
        }
        c.moveToFirst();
        switch(type){
            case "pin_code":
                key = c.getString(c.getColumnIndex(KEY_PIN_CODE));
                break;
            case "is_first":
                key = ""+c.getInt(c.getColumnIndex(KEY_FIRST));
                break;
            case "device":
                key = c.getString(c.getColumnIndex(KEY_DEVICE));
                break;
            case "mode":
                key = c.getString(c.getColumnIndex(KEY_MODE));
                break;
            case "child_ref":
                key =  c.getString(c.getColumnIndex(KEY_CHILD_REF));
                break;
            case "on":
                key = c.getString(c.getColumnIndex(KEY_INT_ON));
                break;
            case "off":
                key =  c.getString(c.getColumnIndex(KEY_INT_OFF));
                break;
            case "sms":
                key =  c.getString(c.getColumnIndex(KEY_OFFLINE_SEND));
                break;
        }
        db.close();
        return key;
    }

    public long createParent(Parent parent, long child_id) {
        long parent_id = -1;
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PASSWORD, parent.getPassword());
        values.put(KEY_PHONE, parent.getPhoneNum());

        // TO DO insert row restrict same parent phone num
        if(getParent(parent.getPhoneNum()) == null) {
            if(getParentChild(parent_id, child_id) == null)
                parent_id = db.insert(TABLE_PARENT, null, values);

            ParentChild parent_child = new ParentChild(parent_id, child_id);
            createParentChild(parent_child);
        }
        db.close();

        return parent_id;
    }

    public long createParentChild(ParentChild parent_child){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PARENT_ID, parent_child.getParent());
        values.put(KEY_CHILD_ID, parent_child.getChild());

        long parent_child_id = db.insert(TABLE_PARENT_CHILD, null, values);
        db.close();
        return parent_child_id;
    }

    public long createSafezone(Safezone safezone) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_LATITUDE, safezone.getCenter().getLatitude());
        values.put(KEY_LONGITUDE, safezone.getCenter().getLongitude());
        values.put(KEY_RADIUS, safezone.getRadius());
        values.put(KEY_IS_HOME, safezone.getIsHome());

        // insert row
        long safezone_id = db.insert(TABLE_SAFEZONE, null, values);
        db.close();
        return safezone_id;
    }

    public long createCurfew(Curfew curfew){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_START, curfew.getStart().toString());
        values.put(KEY_END, curfew.getEnd().toString());


        // insert row
        long curfew_id = db.insert(TABLE_CURFEW, null, values);

        // assigning tags to todo   --- IDK yet
        // for (String day : days) {
        //     createCurfewDay(day, curfew_id);
        // }
        db.close();
        return curfew_id;
    }

    public long createCurfewDay(String curfew_day, long curfew_id){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_DAY, curfew_day);
        values.put(KEY_CURFEW_ID, curfew_id);

        // insert row
        long curfew_day_id = db.insert(TABLE_CURFEW_DAY, null, values);
        db.close();
        return curfew_day_id;
    }

    public long createLocation(ChildLocation location) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_LATITUDE, location.getLocation().getLatitude());
        values.put(KEY_LONGITUDE, location.getLocation().getLongitude());
        // insert row
        long safezone_id = db.insert(TABLE_SAFEZONE, null, values);
        db.close();
        return safezone_id;
    }

    /*
 * get single Child with id
 */
    public Child getChild(long child_id) {
        String selectQuery = "SELECT  * FROM " + TABLE_CHILD + " WHERE "
                + KEY_ID + " = " + child_id;

        Log.e(LOG, selectQuery);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null){
            c.moveToFirst();

            Child cd = new Child();
            cd.setId(c.getString(c.getColumnIndex(KEY_ID)));
            cd.setName((c.getString(c.getColumnIndex(KEY_NAME))));
            cd.setPhoneNum(c.getString(c.getColumnIndex(KEY_PHONE)));
            db.close();
            return cd;
        }

        else {
            db.close();
            return null;
        }

    }
    /*
 * get single Child with phone_num
 */
    public Child getChild(String phone_num) {
        String selectQuery = "SELECT  * FROM " + TABLE_CHILD + " WHERE "
                + KEY_PHONE + " = " + phone_num;

        Log.e(LOG, selectQuery);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null){
            c.moveToFirst();

            Child cd = new Child();
            cd.setId(c.getString(c.getColumnIndex(KEY_ID)));
            cd.setName((c.getString(c.getColumnIndex(KEY_NAME))));
            cd.setPhoneNum(c.getString(c.getColumnIndex(KEY_PHONE)));
            db.close();
            return cd;
        }
        else {
            db.close();
            return null;
        }

    }

    /*
     * getting all Child of a parent
     * */
    public List<Child> getAllChild(int parent_id) {
        List<Child> child_list = new ArrayList<Child>();
        String selectQuery = "SELECT  * FROM " + TABLE_CHILD + " WHERE " + KEY_PARENT_ID +
                " = " + parent_id ;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                long child_id = c.getInt(c.getColumnIndex(KEY_CHILD_ID));
                Child cd = this.getChild(child_id);
                cd.setId(c.getString(c.getColumnIndex(KEY_ID)));
                cd.setName((c.getString(c.getColumnIndex(KEY_NAME))));
                cd.setPhoneNum(c.getString(c.getColumnIndex(KEY_PHONE)));
                // adding to child list
                child_list.add(cd);
            } while (c.moveToNext());
        }
        db.close();
        return child_list;
    }


    /*
* get single Parent with ID
*/
    public Parent getParent(long parent_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_PARENT + " WHERE "
                + KEY_ID + " = " + parent_id;

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        Parent parent = new Parent();
        parent.setId(c.getString(c.getColumnIndex(KEY_ID)));
        parent.setPassword((c.getString(c.getColumnIndex(KEY_PASSWORD))));
        parent.setPhoneNum(c.getString(c.getColumnIndex(KEY_PHONE)));
        db.close();
        return parent;
    }

    /*
* get single Parent with phone_num
*/
    public Parent getParent(String phone_num) {
        Parent parent = null;
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_PARENT + " WHERE "
                + KEY_PHONE + " = " + phone_num;

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null) {
            c.moveToFirst();
            parent = new Parent();
            parent.setId(c.getString(c.getColumnIndex(KEY_ID)));
            parent.setPassword((c.getString(c.getColumnIndex(KEY_PASSWORD))));
            parent.setPhoneNum(c.getString(c.getColumnIndex(KEY_PHONE)));

        }
        db.close();
        return parent;
    }

    public ParentChild getParentChild(long parent_id, long child_id){
        ParentChild parent_child = null;
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + CREATE_TABLE_PARENT_CHILD + " WHERE "
                + KEY_CHILD_ID + " = " + child_id + " AND " + KEY_PARENT_ID + " = "
                + parent_id;

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null) {
            c.moveToFirst();
            parent_child= new ParentChild(parent_id,child_id);
        }
        db.close();
        return parent_child;

    }

    /*
     * getting all Parent of a Child
     * */
    public List<Parent> getAllParent(int child_id) {
        List<Parent> parent_list = new ArrayList<Parent>();
        String selectQuery = "SELECT  * FROM " + TABLE_PARENT_CHILD + " WHERE "
                + KEY_CHILD_ID + " = " + child_id;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                long parent_id = c.getInt(c.getColumnIndex(KEY_PARENT_ID));
                Parent parent = this.getParent(parent_id);
                parent.setId(c.getString(c.getColumnIndex(KEY_ID)));
                parent.setPassword((c.getString(c.getColumnIndex(KEY_PASSWORD))));
                parent.setPhoneNum(c.getString(c.getColumnIndex(KEY_PHONE)));

                // adding to parent list
                parent_list.add(parent);
            } while (c.moveToNext());
        }
        db.close();
        return parent_list;
    }

    /*
* get single Safezone
*/
    public Safezone getSafezone(long safezone_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_SAFEZONE + " WHERE "
                + KEY_ID + " = " + safezone_id;

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        Safezone safezone = new Safezone();
        safezone.setId(c.getString(c.getColumnIndex(KEY_ID)));
        Location location = new Location("from_table");
        location.setLatitude(c.getDouble(c.getColumnIndex(KEY_LATITUDE)));
        location.setLongitude(c.getDouble(c.getColumnIndex(KEY_LONGITUDE)));
        safezone.setCenter(location);
        safezone.setRadius(c.getDouble(c.getColumnIndex(KEY_RADIUS)));
        safezone.setIsHome(c.getInt(c.getColumnIndex(KEY_IS_HOME)));
        db.close();
        return safezone;
    }

    /*
     * getting all Safezone of a child
     * */
    public List<Safezone> getChildSafezone(int child_id) {
        List<Safezone> safezone_list = new ArrayList<Safezone>();
        String selectQuery = "SELECT  * FROM " + TABLE_SAFEZONE + " WHERE " + KEY_CHILD_ID + " = " +
                child_id;


        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Safezone safezone = new Safezone();
                safezone.setId(c.getString(c.getColumnIndex(KEY_ID)));
                Location location = new Location("from_table");
                location.setLatitude(c.getDouble(c.getColumnIndex(KEY_LATITUDE)));
                location.setLongitude(c.getDouble(c.getColumnIndex(KEY_LONGITUDE)));
                safezone.setCenter(location);
                safezone.setRadius(c.getDouble(c.getColumnIndex(KEY_RADIUS)));
                safezone.setIsHome(c.getInt(c.getColumnIndex(KEY_IS_HOME)));

                // adding to safezone list
                safezone_list.add(safezone);
            } while (c.moveToNext());
        }
        db.close();
        return safezone_list;
    }

    /*
   * get days of the curfew*/
    public List<CurfewDay> getDays(int curfew_id) {
        List<CurfewDay> curfew_day_list = new ArrayList<CurfewDay>();
        String selectQuery = "SELECT  * FROM " + TABLE_CURFEW_DAY + " WHERE  " + KEY_CURFEW_ID + " = " + curfew_id;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                CurfewDay curfewDay = new CurfewDay();
                curfewDay.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                curfewDay.setDay(c.getString(c.getColumnIndex(KEY_DAY)));
                curfewDay.setCurfewId(c.getInt(c.getColumnIndex(KEY_CURFEW_ID)));

                // adding to curfew day list
                curfew_day_list.add(curfewDay);
            } while (c.moveToNext());
        }
        db.close();
        return curfew_day_list;
    }

    /*
    * get single Curfew
    */
    public Curfew getCurfew(long curfew_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_CURFEW + " WHERE "
                + KEY_ID + " = " + curfew_id;

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        Curfew curfew = new Curfew();
        curfew.setId(c.getString(c.getColumnIndex(KEY_ID)));
        String startString = c.getString(c.getColumnIndex(KEY_START));
        SimpleDateFormat s = new SimpleDateFormat("YYYY-MM-DD HH:MM:SS.SSS");
        curfew.setStart(s.parse(startString,new ParsePosition(0)).toString());
        String endString = c.getString(c.getColumnIndex(KEY_END));
        curfew.setEnd(s.parse(endString,new ParsePosition(0)).toString());
        db.close();
        return curfew;
    }

    /*
     * getting all Curfew
     * */
    public List<Curfew> getChildCurfew(int child_id) {
        List<Curfew> curfew_list = new ArrayList<Curfew>();
        String selectQuery = "SELECT  * FROM " + TABLE_CURFEW + " WHERE " + KEY_CHILD_ID + " = " +
                child_id;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Curfew curfew = new Curfew();
                curfew.setId(c.getString(c.getColumnIndex(KEY_ID)));
                String startString = c.getString(c.getColumnIndex(KEY_START));
                SimpleDateFormat s = new SimpleDateFormat("YYYY-MM-DD HH:MM:SS.SSS");
                curfew.setStart(s.parse(startString,new ParsePosition(0)).toString());
                String endString = c.getString(c.getColumnIndex(KEY_END));
                curfew.setEnd(s.parse(endString,new ParsePosition(0)).toString());


                // adding to safezone list
                curfew_list.add(curfew);
            } while (c.moveToNext());
        }
        db.close();
        return curfew_list;
    }

    /*
    * get a ChildLocation
    * */

    public ChildLocation getALocation(long child_location_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_LOCATION + " WHERE "
                + KEY_ID + " = " + child_location_id;

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        ChildLocation child_location = new ChildLocation();
        child_location.setId(c.getString(c.getColumnIndex(KEY_ID)));
        Location location = new Location("from_table");
        location.setLatitude(c.getDouble(c.getColumnIndex(KEY_LATITUDE)));
        location.setLongitude(c.getDouble(c.getColumnIndex(KEY_LONGITUDE)));
        child_location.setLocation(location);

        db.close();
        return child_location;
    }

    /*
    * getting all ChildLocation of a child
    * */
    public List<ChildLocation> getChildLocation(int child_id) {
        List<ChildLocation> child_location_list = new ArrayList<ChildLocation>();
        String selectQuery = "SELECT  * FROM " + TABLE_LOCATION + " WHERE " + KEY_CHILD_ID + " = " +
                child_id;


        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                ChildLocation child_location = new ChildLocation();
                child_location.setId(c.getString(c.getColumnIndex(KEY_ID)));
                Location location = new Location("from_table");
                location.setLatitude(c.getDouble(c.getColumnIndex(KEY_LATITUDE)));
                location.setLongitude(c.getDouble(c.getColumnIndex(KEY_LONGITUDE)));
                child_location.setLocation(location);
                // adding to safezone list
                child_location_list.add(child_location);
            } while (c.moveToNext());
        }
        db.close();
        return child_location_list;
    }

    //for empty SQLITE or change account
    public int retrieveAllFireBaseData(long id, String type){
        //type is either Parent or Child
        int result = -1;

        return result;
    }

    public void resetDB(){
        ContentValues values = new ContentValues();
        values.put(ChildTrackerDatabaseHelper.KEY_DEVICE, "");
        values.put(ChildTrackerDatabaseHelper.KEY_MODE, "");
        values.put(ChildTrackerDatabaseHelper.KEY_CHILD_REF, "");
        updateChildTracker(values);
    }



}


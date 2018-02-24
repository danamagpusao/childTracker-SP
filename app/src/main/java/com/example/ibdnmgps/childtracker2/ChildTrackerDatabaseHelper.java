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


     static final String TABLE_CHILDTRACKER_FILES = "files";

    // Common column names
     static final String KEY_ID = "_id";


     static final String KEY_PIN_CODE = "pin_code";
     static final String KEY_FIRST = "is_first"; // 1 if yes, 0 if no
     static final String KEY_DEVICE = "device"; // parent_dev, child_dev
     static final String KEY_MODE = "mode"; // child_mod, parent_mod
     static final String KEY_CHILD_REF = "child_ref";
     static final String KEY_OFFLINE_SEND = "is_sms";
     static final String KEY_INT_OFF = "int_off";
     static final String KEY_INT_ON = "int_on";
     static final String KEY_CHILD_NAME = "child_name";

    // Table Create Statements
    // ChiltTrackerFiles table create statement
    private static final String CREATE_TABLE_CHILDTRACKER_FILES = "CREATE TABLE "
            + TABLE_CHILDTRACKER_FILES + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_PIN_CODE
            + " TEXT," + KEY_FIRST + " INTEGER," + KEY_DEVICE + " TEXT, "  + KEY_MODE + " TEXT, "
            + KEY_OFFLINE_SEND + " INTEGER, " +  KEY_INT_ON + " INTEGER, "  + KEY_INT_OFF + " TEXT, " +
            KEY_CHILD_REF + " INTEGER," + KEY_CHILD_NAME + " TEXT)";


    ChildTrackerDatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CHILDTRACKER_FILES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        SQLiteDatabase db = this.getWritableDatabase();
        Log.w(ChildTrackerDatabaseHelper.class.getName(),
                "Upgrading database from version " + DATABASE_VERSION + " to "
                        + DATABASE_VERSION +1 + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHILDTRACKER_FILES);
        onCreate(db);
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
        values.put(KEY_MODE, "");
        values.put(KEY_PIN_CODE, "1234");
        values.put(KEY_DEVICE, "");
        values.put(KEY_CHILD_REF, "");
        values.put(KEY_INT_OFF, 300000);
        values.put(KEY_INT_ON, 300000);
        values.put(KEY_OFFLINE_SEND, 0);
        values.put(KEY_CHILD_NAME, "");
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
            case "name":
                key =  c.getString(c.getColumnIndex(KEY_CHILD_NAME));
                break;
        }
        db.close();
        return key;
    }


    public void resetDB(){
        ContentValues values = new ContentValues();
        values.put(KEY_FIRST, 1);
        values.put(KEY_MODE, "");
        values.put(KEY_PIN_CODE, "1234");
        values.put(KEY_DEVICE, "");
        values.put(KEY_CHILD_REF, "");
        values.put(KEY_INT_OFF, 300000);
        values.put(KEY_INT_ON, 300000);
        values.put(KEY_OFFLINE_SEND, 0);
        updateChildTracker(values);
    }



}


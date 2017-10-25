package com.example.ibdnmgps.childtracker2;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DatabaseSyncService extends Service{

    private DatabaseReference db;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
/*
* This service always runs and Syncs the firebase database to the local database
* it will only store the list of parent and child
*
* store 5 latest location of child
* curfew
* safezone
*

* */

    ChildTrackerDatabaseHelper cdh;

    public void onCreate() {
        cdh = new ChildTrackerDatabaseHelper(getApplicationContext());
        db = FirebaseDatabase.getInstance().getReference();

        //always clear db then resync
        //if child mode read parent of child
        //read child
            //curfew
            //safezone
            //locations (get only the latest 5)

        //else read children of parent



    }



}

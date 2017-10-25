package com.example.ibdnmgps.childtracker2;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import java.util.ArrayList;

/**
 * reference: Oclemy , posted 6/21/2016 for ProgrammingWizards Channel and http://www.camposha.com.
 */
abstract class FirebaseHelper {
    protected DatabaseReference db;
    protected Boolean saved=null;
    protected ArrayList objects =new ArrayList<>();

    public FirebaseHelper() {
    }
    public FirebaseHelper(DatabaseReference db) {
        this.db = db;
    }

    //READ
    abstract ArrayList retrieve();
}
package com.example.ibdnmgps.childtracker2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


public class SmsReceiver extends BroadcastReceiver {
    private DatabaseReference db;
    private ChildFirebaseHelper helper;
    private static SmsListener listener;
    private String parent_ref;
    private ArrayList<Child> child_list = new ArrayList<>();

    public SmsReceiver(String parent_ref){
        db= FirebaseDatabase.getInstance().getReference();
        helper=new ChildFirebaseHelper(db);
        this.parent_ref = parent_ref;


        db.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                retrieve(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                retrieve(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Database error occured!");
            }

        });

    }
    @Override
    public void onReceive(Context context, Intent intent) {



        Bundle data = intent.getExtras();
        if (Build.VERSION.SDK_INT >= 19) { //KITKAT
            SmsMessage[] msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            SmsMessage smsMessage = msgs[0];
            String sender = smsMessage.getDisplayOriginatingAddress();
            for(Child currchild : child_list) {
                if (sender.contentEquals(currchild.getPhoneNum())) {
                    String messageBody = smsMessage.getMessageBody();
                    //Pass the message text to interface
                    listener.messageReceived(messageBody);
                }
            }
        } else {
            Object[] pdus = (Object[]) data.get("pdus");

            for(int i = 0; i < pdus.length ; i++){
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);
                String sender = smsMessage.getDisplayOriginatingAddress();

                for(Child currchild : child_list) {
                    if (sender.contentEquals(currchild.getPhoneNum())) {
                        String messageBody = smsMessage.getMessageBody();
                        //Pass the message text to interface
                        listener.messageReceived(messageBody);
                    }
                }
            }
        }
    }

    public void retrieve(DataSnapshot dataSnapshot) {
        System.out.println("retrieving ...");
        for (DataSnapshot ds : dataSnapshot.getChildren())
        {
            if(dataSnapshot.getKey().equals("Child") && ds.child("parents/"+parent_ref).getValue() != null) {
                Child child = new Child();
                child.setId(ds.getKey());
                child.setPhoneNum(ds.child("phoneNum").getValue(String.class));
                child.setName(ds.child("name").getValue(String.class));
                if(!child_list.contains(child))
                    child_list.add(child);
                System.out.println(child.getName());
            }
        }


    }

    public static void bindListener(SmsListener mlistener) {
        listener = mlistener;
    }
}

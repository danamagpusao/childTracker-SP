package com.example.ibdnmgps.childtracker2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import static android.R.attr.data;

public class MenuList extends ListActivity {

    private String[] options = new String[] { "Curfew","Safezone", "Parent", "Settings", "Log Out"};
    private String child_ref;
    private ChildTrackerDatabaseHelper h;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_list);
        h = new ChildTrackerDatabaseHelper(getApplicationContext());
        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options);
        this.setListAdapter(itemsAdapter);
        child_ref = h.getFiles("child_ref");
        System.out.println(child_ref + "<<< Child reference");


    }

    @Override
    public void onListItemClick(ListView listView, View itemView, int position, long id){
        Intent intent = new Intent(MenuList.this, MenuList.class);
        switch (position){
            case 0:
                intent = new Intent(MenuList.this, AddCurfew.class);
                break;
            case 1:
                intent = new Intent(MenuList.this, SafezoneList.class);
                intent.putExtra("child_ref", child_ref);
                break;
            case 2:
                intent = new Intent(MenuList.this, ParentList.class);
                break;
            case 3:
                intent = new Intent(MenuList.this, Settings.class);
                break;
            case 4:
                new AlertDialog.Builder(this)
                        .setTitle("Log Out")
                        .setMessage("Do you really want to Log Out?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                stopService(new Intent(MenuList.this, ChildTrackerService.class));
                                FirebaseAuth.getInstance().signOut();
                                h.resetDB();
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    finishAndRemoveTask();
                                } else {
                                    finishAffinity();
                                }
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
                return;
            default:
        }

        startActivity(intent);




    }

}

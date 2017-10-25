package com.example.ibdnmgps.childtracker2;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
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
        child_ref = getIntent().getExtras().getString("child_ref");
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
                break;
            case 2:
                intent = new Intent(MenuList.this, ParentList.class);
                break;
            case 3:
                intent = new Intent(MenuList.this, Settings.class);
                break;
            case 4:
                intent = new Intent(MenuList.this,LogInActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                FirebaseAuth.getInstance().signOut();
                h.resetDB();
                break;
            default:

        }


        startActivity(intent);
        if(position == 4) {
           finish();
        }

    }

}

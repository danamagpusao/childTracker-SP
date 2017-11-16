package com.example.ibdnmgps.childtracker2;

        import android.content.Intent;
        import android.os.Handler;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;

        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.auth.FirebaseUser;
        import com.google.firebase.database.DatabaseReference;
        import com.google.firebase.database.FirebaseDatabase;

        import static android.R.attr.value;

public class StartUp extends AppCompatActivity {
    private ChildTrackerDatabaseHelper h;
    static Boolean isInitializedDB = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null)
            isInitializedDB = savedInstanceState.getBoolean("isInitializedDB");

        if (!isInitializedDB)
        {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            isInitializedDB = true;
        }
        setContentView (R.layout.activity_start_up);
        h = new ChildTrackerDatabaseHelper(getApplicationContext());

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent  = new Intent(StartUp.this, LogInActivity.class);
                String type = h.getFiles("device");
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    if(type.equals("child")){
                        intent  = new Intent(StartUp.this, ChildHome.class);
                    }
                    else if(type.equals("parent")){
                        intent  = new Intent(StartUp.this, childList_home.class);
                    }
                }
                startActivity(intent);
                finish();

            }
        }, 3000);

    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putBoolean("isInitializedDB", isInitializedDB);
    }

}


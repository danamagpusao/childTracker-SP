package com.example.ibdnmgps.childtracker2;

import android.content.ContentValues;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static android.R.attr.data;
import static android.R.attr.tag;
import static com.example.ibdnmgps.childtracker2.R.id.ccp;

public class LogInActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "LogInActivity";

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private boolean mVerificationInProgress = false;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private EditText liPhoneNum;
    private EditText liOTP;
    private Button liStart;
    private Button liNext;
    private Button liResend;
    private CountryCodePicker liCcp;
    private String phoneNumber;
    private  FirebaseUser user;

    private DatabaseReference db;
    private ChildTrackerDatabaseHelper h;
    private ArrayList<DataSnapshot> stored;

    //Code is based from the example code from GitHub @Firebase
    //https://github.com/firebase/quickstart-android/blob/master/auth/app/src/main/java/com/google/firebase/quickstart/auth/PhoneAuthActivity.java#L204-L209
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        //initializes Firebase Authentication
        mAuth = FirebaseAuth.getInstance();
        db= FirebaseDatabase.getInstance().getReference();
        h = new ChildTrackerDatabaseHelper(getApplicationContext());
        stored = new ArrayList<DataSnapshot>();
        //get reference on the UI components
        liPhoneNum = (EditText) findViewById(R.id.logIn_phoneNumber);
        liOTP = (EditText) findViewById(R.id.logIn_otp);
        liStart = (Button) findViewById(R.id.logIn_start);
        liNext = (Button) findViewById(R.id.logIn_next);
        liResend = (Button) findViewById(R.id.logIn_resendBtn);
        liCcp = (CountryCodePicker) findViewById(R.id.logIn_ccp);


        //assign onclick listeners
        liStart.setOnClickListener(this);
        liNext.setOnClickListener(this);
        liResend.setOnClickListener(this);



        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                Log.d(TAG,"onVerificationCompleted:" + phoneAuthCredential);
                mVerificationInProgress = false;
                Toast.makeText(LogInActivity.this, "Phone verification complete", Toast.LENGTH_SHORT).show();
                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);

                mVerificationInProgress=false;

                if( e instanceof FirebaseAuthInvalidCredentialsException) {
                    liPhoneNum.setError("Invalid Phone number");

                } else if ( e instanceof FirebaseTooManyRequestsException) {
                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                            Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:" + verificationId);

                mVerificationId = verificationId;
                mResendToken = token;

                liOTP.setVisibility(View.VISIBLE);
                liNext.setVisibility(View.VISIBLE);
                liResend.setVisibility(View.VISIBLE);

            }
        };

        db.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    stored.add(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                stored.add(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                stored.add(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.logIn_start:
               if (!validatePhoneNumber()) {
                    return;
                }
                startPhoneNumberVerification(phoneNumber);
                break;
            case R.id.logIn_next:
                String code = liOTP.getText().toString();
                if (TextUtils.isEmpty(code)) {
                    liOTP.setError("Cannot be empty.");
                    return;
                }

                verifyPhoneNumberWithCode(mVerificationId, code);
                break;
            case R.id.logIn_resendBtn:
                resendVerificationCode(phoneNumber, mResendToken);
                break;
        }

    }

    private boolean validatePhoneNumber() {
        phoneNumber =  liCcp.getSelectedCountryCodeWithPlus() + liPhoneNum.getText().toString().substring(1);
        if(TextUtils.isEmpty(phoneNumber))  {
            liPhoneNum.setError("Phone number cannot be empty!");
            return false;
        }
        return true;
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        // start of phone authentication
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                120,
                TimeUnit.SECONDS,
                this,
                mCallbacks
        );

        mVerificationInProgress = true;
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // start of verifying code
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void resendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                120,
                TimeUnit.SECONDS,
                this,
                mCallbacks,
                token
        );
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult> () {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            user = task.getResult().getUser();
                            if(user.getDisplayName()==null){
                                Intent intent = new Intent(LogInActivity.this,UserInfoActivity.class);
                                intent.putExtra("user_phone_num", user.getPhoneNumber());
                                startActivity(intent);
                                finish();
                            }
                            else {
                                System.out.println("retrieves ----");
                                Intent intent = new Intent(LogInActivity.this,UserInfoActivity.class);
                                intent.putExtra("user_phone_num", user.getPhoneNumber());

                                for(DataSnapshot data : stored ){
                                    String result = retrieve(data, user);
                                    if (result.equals("child")){
                                        intent = new Intent(LogInActivity.this,ChildHome.class);

                                        break;
                                    }
                                    else if(result.equals("parent")){
                                        intent = new Intent(LogInActivity.this,childList_home.class);
                                        break;
                                    }
                                }
                                startActivity(intent);
                                finish();
                            }

                        } else {
                            Log.w(TAG,"signInWithCredential:failure", task.getException());
                            if(task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                liOTP.setError("Invalid Code.");
                            }
                            Toast.makeText(LogInActivity.this,"Sign In Failed", Toast.LENGTH_LONG).show();

                        }
                    }
                });
    }



    public String retrieve(DataSnapshot dataSnapshot, FirebaseUser user) {
        System.out.println("retrieving (LogInActivity) ...");
        ContentValues values = new ContentValues();
        for (DataSnapshot ds : dataSnapshot.getChildren())
        {
            System.out.println("OYAAAHH :: " +dataSnapshot.getKey() + " >>>" + ds.getKey());
                if (dataSnapshot.getKey().equals("Child") && ds.getKey().equals(user.getUid())) {
                    values.put(ChildTrackerDatabaseHelper.KEY_DEVICE, "child");
                    values.put(ChildTrackerDatabaseHelper.KEY_MODE, "child");
                    values.put(ChildTrackerDatabaseHelper.KEY_CHILD_REF, user.getUid());
                    h.updateChildTracker(values);
                    return "child";
                } else if (dataSnapshot.getKey().equals("Parent") && ds.getKey().equals(user.getUid())) {
                    values.put(ChildTrackerDatabaseHelper.KEY_DEVICE, "parent");
                    values.put(ChildTrackerDatabaseHelper.KEY_MODE, "parent");
                    values.put(ChildTrackerDatabaseHelper.KEY_CHILD_REF, user.getUid());
                    h.updateChildTracker(values);
                    return "parent";
                }
        }

        return "na";
    }

}

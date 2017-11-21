package com.example.ibdnmgps.childtracker2;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
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
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static android.R.attr.childDivider;
import static android.R.attr.password;
import static android.R.id.list;
import static android.media.CamcorderProfile.get;

public class ParentList extends ListActivity {

    private static String TAG = "ParentListActivity";
    DatabaseReference db;
    ParentFirebaseHelper helper;
    ParentAdapter adapter;
    String currentChildId;
    ChildTrackerDatabaseHelper h;
    ArrayList<Parent> first = new ArrayList<>();

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private boolean mVerificationInProgress = false;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private Dialog d;

    private EditText dPhoneNum ;
    private EditText dOTP ;
    private Button dResend ;
    private Button saveBtn;
    private Button addBtn;
    private CountryCodePicker dCCp;

    private Button oDelete;
    private Switch oSwitch;

    private String phoneNum;
    private PhoneAuthCredential pac;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_list);
            //initialize

        //initializes Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        //initializes dialog components
        d  = new Dialog(this);
        d.setTitle("Add Parent");
        d.setContentView(R.layout.add_parent_dialog);
        dPhoneNum = (EditText) d.findViewById(R.id.parent_num);
        dOTP = (EditText) d.findViewById(R.id.addParent_otp);
        dResend = (Button) d.findViewById(R.id.addParent_resend);
        saveBtn = (Button) d.findViewById(R.id.saveParent);
        addBtn = (Button) d.findViewById(R.id.addParent_btn);
        dCCp = (CountryCodePicker) d.findViewById(R.id.addParent_ccp);

        h = new ChildTrackerDatabaseHelper(getApplicationContext());
        currentChildId = h.getFiles("child_ref");
        db = FirebaseDatabase.getInstance().getReference();
        helper = new ParentFirebaseHelper(db,currentChildId);

        adapter = new ParentAdapter(ParentList.this, R.layout.mylist, first);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayInputDialog();
            }
        });


        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                Log.d(TAG,"onVerificationCompleted:" + phoneAuthCredential);
                mVerificationInProgress = false;
                Toast.makeText(ParentList.this, "Phone verification complete", Toast.LENGTH_SHORT).show();
                pac = phoneAuthCredential;
                addBtn.setVisibility(View.VISIBLE);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);

                mVerificationInProgress=false;

                if( e instanceof FirebaseAuthInvalidCredentialsException) {
                    dPhoneNum.setError("Invalid Phone number");

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
                dOTP.setVisibility(View.VISIBLE);
                saveBtn.setVisibility(View.VISIBLE);
                dResend.setVisibility(View.VISIBLE);
                addBtn.setVisibility(View.VISIBLE);

            }
        };


        db= FirebaseDatabase.getInstance().getReference();
        db.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                retrieveParent(dataSnapshot);
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                first.clear();
                retrieveParent(dataSnapshot);
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                first.clear();
                retrieveParent(dataSnapshot);
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                first.clear();
                retrieveParent(dataSnapshot);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        this.setListAdapter(adapter);


        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = dOTP.getText().toString();
                if (TextUtils.isEmpty(code) && pac == null) {
                    dOTP.setError("Cannot be empty.");
                    return;
                }else if(pac != null) {
                    saveBtn.setVisibility(View.GONE);
                    signInWithPhoneAuthCredential(pac);
                } else verifyPhoneNumberWithCode(mVerificationId, code);
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                if (!validatePhoneNumber()) {
                    return;
                }

                startPhoneNumberVerification(phoneNum);
            }
        });
    }

    @Override
    public void onListItemClick(ListView listView, View itemView, final int position, long id){

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.layout_parent_options);
        dialog.setTitle("Options");
        final ParentList n = ParentList.this;


        oDelete = (Button) dialog.findViewById(R.id.parent_options_delete);
        oSwitch = (Switch)  dialog.findViewById(R.id.parent_options_switch);
        oSwitch.setChecked(first.get(position).getReceiveSMS());

        oDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helper.remove(first.get(position).getId(),h.getFiles("child_ref").toString());
                first.remove(position);
                adapter.notifyDataSetChanged();
            }
        });

        oSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Parent p = first.get(position);
                p.setReceiveSMS(compoundButton.isChecked());
                helper.update(p);

            }
        });

        View.OnClickListener l = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    dialog.dismiss();
                    n.finish();
                    n.startActivity(n.getIntent());

            }
        };
        dialog.show();
    }


    private void displayInputDialog() {
        adapter.notifyDataSetChanged();
        d.show();
    }

    private void retrieveParent(DataSnapshot dataSnapshot) {
        for (DataSnapshot ds : dataSnapshot.getChildren())
        {
                if(dataSnapshot.getKey().equals("Parent") && ds.child("children/"+currentChildId).getValue() != null) {
                    Parent parent = new Parent();
                    parent.setId(ds.getKey());
                    parent.setPhoneNum(ds.child("phoneNum").getValue(String.class));
                    parent.setName(ds.child("name").getValue(String.class));
                    parent.setReceiveSMS(ds.child("receiveSMS").getValue(Boolean.class));
                    boolean isDuplicate = false;
                    for(Parent p : first) {
                        if(parent.getPhoneNum().toString().equals( p.getPhoneNum().toString())) isDuplicate = true;
                    }
                    if(!isDuplicate) first.add(parent);
                    System.out.println(parent.getName());

                }
            adapter.notifyDataSetChanged();
        }
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // start of verifying code
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = task.getResult().getUser();
                            if(helper.isParent(user.getUid())){
                                addParentToDB(user);
                                Toast.makeText(ParentList.this,"Added Parent Successfully! " +
                                        user.getPhoneNumber(), Toast.LENGTH_LONG).show();
                            }else{
                                Toast.makeText(ParentList.this,"Phone Number provided already exists as child!" +
                                        user.getPhoneNumber(), Toast.LENGTH_LONG).show();
                            }

                            d.dismiss();


                        } else {
                            Log.w(TAG,"signInWithCredential:failure", task.getException());
                            if(task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                dOTP.setError("Invalid Code.");
                            }
                            Toast.makeText(ParentList.this,"Cannot Add Parent!", Toast.LENGTH_LONG).show();

                        }
                    }
                });
    }

    private void addParentToDB(FirebaseUser user) {
        String parent_name = (user.getDisplayName() == null) ? "Jane Doe":user.getDisplayName();
        String parent_number = user.getPhoneNumber();
        String parent_id = user.getUid();

        Parent object = new Parent();
        object.setName(parent_name);
        object.setPhoneNum(parent_number);
        object.setId(parent_id);
        object.setReceiveSMS(false);

        ParentFirebaseHelper helper = new ParentFirebaseHelper(db);
        if(!helper.save(object).equals("")){
            Boolean addChild =  helper.addChild(user.getUid(), currentChildId);
            if(addChild) Toast.makeText(this,"Parent added successfully",Toast.LENGTH_SHORT);
            else  Toast.makeText(this,"Cannot add Parent!",Toast.LENGTH_SHORT);
        } else{
            Log.d(TAG,"FirebaseDatabase_AddParent:Failed");
            Toast.makeText(this,"Firebase Database Error",Toast.LENGTH_SHORT);
        }
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

    private boolean validatePhoneNumber() {
        phoneNum =  dCCp.getSelectedCountryCodeWithPlus() + dPhoneNum.getText().toString().substring(1);
        if(TextUtils.isEmpty(phoneNum))  {
            dPhoneNum.setError("Phone number cannot be empty!");
            return false;
        }
        return true;
    }


}

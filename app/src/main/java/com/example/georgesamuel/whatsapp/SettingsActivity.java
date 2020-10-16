package com.example.georgesamuel.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = SettingsActivity.class.getSimpleName();
    private ImageView imgProfile;
    private EditText etName, etStatus;
    private AppCompatButton btnUpdate;
    private ProgressDialog loadingBar;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        rootRef = FirebaseDatabase.getInstance().getReference();

        initView();
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSettings();
            }
        });
        retrieveUserInfo();
    }

    private void retrieveUserInfo(){
        rootRef.child(Constants.ROOT_USERS).child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String name = "", status = "", profilePath;
                    if(dataSnapshot.child(Constants.USER_NAME).getValue() != null)
                        name = dataSnapshot.child(Constants.USER_NAME).getValue().toString();
                    if(dataSnapshot.child(Constants.USER_STATUS).getValue() != null)
                        status = dataSnapshot.child(Constants.USER_STATUS).getValue().toString();
                    if(dataSnapshot.child(Constants.USER_IMAGE_PATH).getValue() != null)
                        profilePath  = dataSnapshot.child(Constants.USER_IMAGE_PATH).getValue().toString();

                    etName.setText(name);
                    etStatus.setText(status);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initView() {
        imgProfile = findViewById(R.id.imgProfile);
        etName = findViewById(R.id.etName);
        etStatus = findViewById(R.id.etStatus);
        btnUpdate = findViewById(R.id.btnUpdate);
        loadingBar = new ProgressDialog(this);
    }

    private void updateSettings() {
        String name = etName.getText().toString();
        String status = etStatus.getText().toString();
        String userId = currentUser.getUid();

        loadingBar.setTitle("Creating new account");
        loadingBar.setMessage("Please wait, while we are creating new account for you...");
        loadingBar.setCancelable(false);
        loadingBar.show();

        HashMap<String, String> profileData = new HashMap<>();
        profileData.put(Constants.USER_ID, userId);
        profileData.put(Constants.USER_NAME, name);
        profileData.put(Constants.USER_STATUS, status);


        rootRef.child(Constants.ROOT_USERS).child(userId).setValue(profileData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        loadingBar.dismiss();
                        if(task.isSuccessful()){
                            Toast.makeText(SettingsActivity.this, "Profile updtaes successfully...", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
                        }
                        else {
                            Log.d(TAG, task.getException().getMessage());
                            Toast.makeText(SettingsActivity.this, "Exception", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
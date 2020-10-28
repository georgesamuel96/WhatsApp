package com.example.georgesamuel.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements UserAdapter.UserClickListener {

    private ArrayList<User> usersList = new ArrayList<>();
    private UserAdapter userAdapter;
    private RecyclerView rvUsers;
    private ProgressDialog loader;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference rootRef;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        rootRef = FirebaseDatabase.getInstance().getReference();

        initView();

        if(currentUser == null) {
            sendUserToLoginActivity();
        }
        else {
            verifyUserExistence();
        }
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.app_name));

        rvUsers = findViewById(R.id.rvUsers);
        userAdapter = new UserAdapter(MainActivity.this, usersList, this);
        rvUsers.setLayoutManager(new LinearLayoutManager(MainActivity.this, RecyclerView.VERTICAL, false));
        rvUsers.setHasFixedSize(true);
        rvUsers.setAdapter(userAdapter);

        loader = new ProgressDialog(this);
    }

    @Override
    public void onUserCLickedListener(User user) {
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_USER_RECEIVER_ID, user);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void verifyUserExistence() {
        String userId = currentUser.getUid();

        rootRef.child(Constants.ROOT_USERS).child(userId).addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(Constants.USER_NAME).exists()){
                    getAllUsers();
                }
                else {
                    sendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getAllUsers() {
        loader.setMessage("Loading...");
        loader.setCancelable(false);
        loader.show();
        rootRef.child(Constants.ROOT_USERS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot userSnapshot : dataSnapshot.getChildren()){
                        User user = new User();
                        HashMap<String, String> userMap = (HashMap<String, String>) userSnapshot.getValue();
                        if (userMap != null) {
                            String name = userMap.get(Constants.USER_NAME);
                            String status = userMap.get(Constants.USER_STATUS);
                            String uid = userMap.get(Constants.USER_ID);
                            user.setName(name);
                            user.setStatus(status);
                            user.setUserId(uid);
                            if(!uid.equals(currentUser.getUid()))
                                usersList.add(user);
                        }

                    }
                }
                else {
                    usersList.clear();
                }
                loader.dismiss();
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                loader.dismiss();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.item_logout){
            mAuth.signOut();
            sendUserToLoginActivity();
            return true;
        }
        else if(item.getItemId() == R.id.item_settings){
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendUserToLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void sendUserToSettingsActivity() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = RegisterActivity.class.getSimpleName();
    private AppCompatButton btnRegister, btnHaveAccount;
    private EditText etEmail, etPassword;
    private ProgressDialog loadingBar;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        initView();
        btnHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToLoginActivity();
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });
    }

    private void createNewAccount() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        loadingBar.setTitle("Creating new account");
        loadingBar.setMessage("Please wait, while we are creating new account for you...");
        loadingBar.setCancelable(false);
        loadingBar.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        loadingBar.dismiss();
                        if(task.isSuccessful()){
                            String userId = mAuth.getCurrentUser().getUid();
                            rootRef.child(Constants.ROOT_USERS).child(userId).setValue("");

                            Toast.makeText(RegisterActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                            sendUserToSettingsActivity();
                        }
                        else {
                            Log.d(TAG, task.getException().getMessage());
                            Toast.makeText(RegisterActivity.this, "Exception", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void initView() {
        btnRegister = findViewById(R.id.register_button);
        btnHaveAccount = findViewById(R.id.already_have_account_button);
        etEmail = findViewById(R.id.register_email);
        etPassword = findViewById(R.id.etPassword);
        loadingBar = new ProgressDialog(this);
    }

    private void sendUserToLoginActivity() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void sendUserToSettingsActivity() {
        Intent intent = new Intent(RegisterActivity.this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
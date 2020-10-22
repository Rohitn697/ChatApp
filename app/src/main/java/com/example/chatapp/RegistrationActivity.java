package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegistrationActivity extends AppCompatActivity {
    Button register;
    EditText username,password;
    TextView accountExist;
    private FirebaseUser currentuser;
    private FirebaseAuth mAuth;
    private DatabaseReference ref,userRef;
    LoadingDialog loadingDialog = new LoadingDialog(RegistrationActivity.this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        initialiseField();
        mAuth=FirebaseAuth.getInstance();
       currentuser= mAuth.getCurrentUser();
       ref = FirebaseDatabase.getInstance().getReference();
       userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp();
                loadingDialog.loadingAlertDialog();
                handler();
            }
        });
    }
    private void handler(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingDialog.dismissDialog();
            }
        },850);
    }
    private void signUp() {
        String email = username.getText().toString();
        String pass = password.getText().toString();
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please Enter email", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(pass)){
            Toast.makeText(this, "Please Enter password", Toast.LENGTH_SHORT).show();
        }
        else {
            mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                           String deviceId = FirebaseInstanceId.getInstance().getToken();
                           String CurrentUserId = mAuth.getCurrentUser().getUid();
                            ref.child("Users").child(CurrentUserId).setValue("");

                            ref.child("Users").child(CurrentUserId).child("device_token").setValue(deviceId);
                            mainActivity();
                            Toast.makeText(RegistrationActivity.this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();
                            loadingDialog.dismissDialog();


                    }
                    else {
                        String message = task.getException().getMessage().toString();
                        Toast.makeText(RegistrationActivity.this, message, Toast.LENGTH_SHORT).show();
                        loadingDialog.dismissDialog();
                    }
                }
            });
        }
    }


    private void mainActivity() {
        Intent i = new Intent(RegistrationActivity.this,MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void initialiseField() {
        register = (Button)findViewById(R.id.Register);
        username = (EditText)findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        accountExist = (TextView)findViewById(R.id.loginBack);
    }

    @Override
    protected void onStart() {
        if (currentuser!=null){
            mainActivity();
        }
        super.onStart();
    }

    // back to login screen
    public void newUser(View view){
        Intent intent = new Intent(RegistrationActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

public class profileActivity extends AppCompatActivity {
    private String receiverUserID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        receiverUserID = getIntent().getExtras().get("visited_uid").toString();
        Toast.makeText(this, receiverUserID, Toast.LENGTH_SHORT).show();
    }
}
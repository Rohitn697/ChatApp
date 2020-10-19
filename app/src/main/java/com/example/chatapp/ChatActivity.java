package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

public class ChatActivity extends AppCompatActivity {
    private String receiverID,receiverName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        receiverID = getIntent().getExtras().get("visited_userID").toString();
        receiverName = getIntent().getExtras().get("username").toString();

        Toast.makeText(this, receiverName, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, receiverID, Toast.LENGTH_SHORT).show();
    }
}
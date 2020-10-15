package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

public class FindFriendsActivity extends AppCompatActivity {
    private Toolbar mainTool;
    private RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        mainTool = (Toolbar) findViewById(R.id.findFriends_toolbar);
        setSupportActionBar(mainTool);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

        recyclerView = (RecyclerView) findViewById(R.id.findFriends_RecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
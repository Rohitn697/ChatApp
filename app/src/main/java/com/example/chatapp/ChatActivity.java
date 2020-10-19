package com.example.chatapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private String receiverID,receiverName,receiverImage;
    private TextView username,lastSeen;
    private CircleImageView userPic;
    androidx.appcompat.widget.Toolbar chatToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        chatToolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        chatToolbar.setContentInsetStartWithNavigation(0);
        setSupportActionBar(chatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);

        receiverID = getIntent().getExtras().get("visited_userID").toString();
        receiverName = getIntent().getExtras().get("username").toString();
        receiverImage = getIntent().getExtras().get("image").toString();

        username = (TextView) findViewById(R.id.customProfileName);
        lastSeen = (TextView) findViewById(R.id.customLastSeen);
        userPic = (CircleImageView) findViewById(R.id.customProfilePic);



        username.setText(receiverName);
        Picasso.get().load(receiverImage).into(userPic);
    }
}
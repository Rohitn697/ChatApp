package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class GroupChatActivity extends AppCompatActivity {
    private Toolbar mainTool;
    private Button send,sendFiles;
    private EditText type;
    private TextView messages;
    private String currentGroupName,currentUserId,currentUserName,currentDate,currentTime;
    private FirebaseAuth mAuth;
    private DatabaseReference ref,groupRef,GroupMessageRef;
    private final List<GroupMessages> groupMessagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private GroupMessagesAdapter groupMessagesAdapter;
    private RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        currentGroupName = getIntent().getExtras().getString("groupName").toString();
        ref = FirebaseDatabase.getInstance().getReference().child("Users");
        groupRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);
        initialise();
        retrieveUserInfo();
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveMessagesToDB();
                type.setText("");
            }
        });
    }


    private void saveMessagesToDB() {
        String newMessage = type.getText().toString();
        String messageKey = groupRef.push().getKey();
        if (TextUtils.isEmpty(newMessage)){
            // don't send any message and don't save in DB
        }
        else {
            Calendar calendarDate = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM,yyyy");
            currentDate = dateFormat.format(calendarDate.getTime());

            Calendar calendarTime = Calendar.getInstance();
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = timeFormat.format(calendarTime.getTime());
            HashMap<String,Object> groupMessageKey = new HashMap<>();
            groupRef.updateChildren(groupMessageKey);
            GroupMessageRef = groupRef.child(messageKey);
            HashMap<String,Object> groupMessageInfo = new HashMap<>();
                groupMessageInfo.put("name",currentUserName);
                groupMessageInfo.put("message", newMessage);
                groupMessageInfo.put("date",currentDate);
                groupMessageInfo.put("time",currentTime);
                groupMessageInfo.put("uid",currentUserId);
                GroupMessageRef.updateChildren(groupMessageInfo);
        }
    }

    private void retrieveUserInfo() {

        ref.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initialise() {
        mainTool = (Toolbar) findViewById(R.id.groupChat_bar);
        setSupportActionBar(mainTool);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(currentGroupName);
        send = (Button) findViewById(R.id.sendGroupMessage_button);
        type = (EditText) findViewById(R.id.sendGroupMessage);
        sendFiles = (Button) findViewById(R.id.sendFiles_button);

        groupMessagesAdapter = new GroupMessagesAdapter(groupMessagesList);
        recyclerView = (RecyclerView) findViewById(R.id.groupMessage_list);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(groupMessagesAdapter);

    }

}
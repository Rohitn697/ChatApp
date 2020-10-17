package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class profileActivity extends AppCompatActivity {
    private String receiverUserID,currentState,currentUserID,receiverName;
    private CircleImageView visitorProfilePic;
    private TextView username,status;
    private Button sendRequest;
    private DatabaseReference ref,chatReqRef;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        receiverUserID = getIntent().getExtras().get("visited_uid").toString();
        ref = FirebaseDatabase.getInstance().getReference().child("Users");
        chatReqRef=FirebaseDatabase.getInstance().getReference().child("Chat Request");
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        initialise();
        retrieveInfo();
    }

    private void initialise() {
        visitorProfilePic = (CircleImageView) findViewById(R.id.visited_ProfilePic);
        username = (TextView) findViewById(R.id.visited_username);
        status = (TextView) findViewById(R.id.visited_status);
        sendRequest = (Button) findViewById(R.id.sendRequest);
        currentState = "new";
    }
    private void retrieveInfo() {
    ref.child(receiverUserID).addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if ((snapshot.exists()) && snapshot.hasChild("image")){
                String userImage = snapshot.child("image").getValue().toString();
                String userName = snapshot.child("name").getValue().toString();
                String userStatus  = snapshot.child("status").getValue().toString();

                Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(visitorProfilePic);
                username.setText(userName);
                status.setText(userStatus);
                receiverName=userName;
                manageRequest();
            }
            else {

                String userName = snapshot.child("name").getValue().toString();
                String userStatus  = snapshot.child("status").getValue().toString();

                username.setText(userName);
                status.setText(userStatus);

                manageRequest();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    });
    }

    private void manageRequest() {
        chatReqRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.hasChild(receiverUserID)){
                 String request_type = snapshot.child(receiverUserID).child("request_type").getValue().toString();
                 if (request_type.equals("sent")){
                     currentState="request_sent";
                     sendRequest.setText("Cancel Message Request");
                 }
            }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if (!currentUserID.equals(receiverUserID)){
            sendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendRequest.setEnabled(false);
                    if (currentState.equals("new"))
                    {
                        Toast.makeText(profileActivity.this, "Message request has been sent to "+ receiverName , Toast.LENGTH_SHORT).show();
                        sendChatRequest();
                    }
                }
            });
        }
        else {
            sendRequest.setVisibility(View.INVISIBLE);
        }
    }

    private void sendChatRequest() {
        chatReqRef.child(currentUserID).child(receiverUserID).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            if (task.isSuccessful()){
                chatReqRef.child(receiverUserID).child(currentUserID).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        sendRequest.setEnabled(true);
                        currentState="request_sent";
                        sendRequest.setText("Cancel Message Request");
                    }
                });
            }
            }
        });
    }
}
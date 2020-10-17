package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class profileActivity extends AppCompatActivity {
    private String receiverUserID;
    private CircleImageView visitorProfilePic;
    private TextView username,status;
    private Button sendRequest;
    private DatabaseReference ref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        receiverUserID = getIntent().getExtras().get("visited_uid").toString();
        ref = FirebaseDatabase.getInstance().getReference().child("Users");
        initialise();
        retrieveInfo();
    }

    private void initialise() {
        visitorProfilePic = (CircleImageView) findViewById(R.id.visited_ProfilePic);
        username = (TextView) findViewById(R.id.visited_username);
        status = (TextView) findViewById(R.id.visited_status);
        sendRequest = (Button) findViewById(R.id.sendRequest);
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
            }
            else {

                String userName = snapshot.child("name").getValue().toString();
                String userStatus  = snapshot.child("status").getValue().toString();

                username.setText(userName);
                status.setText(userStatus);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    });
    }
}
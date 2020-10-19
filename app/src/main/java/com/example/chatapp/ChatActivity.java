package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private String receiverID,receiverName,receiverImage;
    private TextView username,lastSeen;
    private EditText message;
    private CircleImageView userPic;
    private Button sendDm;
    androidx.appcompat.widget.Toolbar chatToolbar;
    private String currentSenderID;
    private FirebaseAuth mAuth;
   private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        currentSenderID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();



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


        intialise();
        username.setText(receiverName);
        Picasso.get().load(receiverImage).into(userPic);

        sendDm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            sendMessage();
            }
        });
    }

    private void intialise() {
        sendDm = (Button) findViewById(R.id.sendDm_button);
        message = (EditText) findViewById(R.id.send_dm);
    }
    private void sendMessage(){
        String messageText = message.getText().toString();
        if(TextUtils.isEmpty(messageText)){
            //don't send anything
        }
        else {
            String messageSenderRef = "Messages/" + currentSenderID + "/" + receiverID;
            String messageReceiverRef = "Messages/" + receiverID + "/" + currentSenderID;

            DatabaseReference userMessageKey = rootRef.child("Messages").child(currentSenderID).child(receiverID).push();
            String messagePushID = userMessageKey.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("type","Text");
            messageTextBody.put("from",currentSenderID);

            Map messageBodyDetails  = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/"+ messagePushID , messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/"+ messagePushID , messageTextBody);

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()){
                    Toast.makeText(ChatActivity.this, "message sent!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(ChatActivity.this, "error", Toast.LENGTH_SHORT).show();
                }
                message.setText("");
                }
            });
        }

    }
}
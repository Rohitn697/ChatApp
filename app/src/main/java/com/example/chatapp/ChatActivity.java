package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private String receiverID,receiverName,receiverImage;
    private TextView username,lastSeen;
    private EditText message;
    private CircleImageView userPic;
    private Button sendDm,sendFiles;
    androidx.appcompat.widget.Toolbar chatToolbar;
    private String currentSenderID;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef,notifRef;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;
    private  String saveCurrentTime,saveCurrentDate;
    private String checker = "",URL="";
    private StorageTask uploadTask;
    private Uri fileUri;
    private ProgressDialog loadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        currentSenderID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        notifRef = FirebaseDatabase.getInstance().getReference().child("Notification");


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

        if (Build.VERSION.SDK_INT >= 11) {
            userMessagesList.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v,
                                           int left, int top, int right, int bottom,
                                           int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (bottom < oldBottom) {
                        userMessagesList.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                userMessagesList.smoothScrollToPosition(
                                        userMessagesList.getAdapter().getItemCount());
                            }
                        }, 100);
                    }
                }
            });
        }
        DisplayLastSeen();

        sendFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence options[] = new CharSequence[]{
                        "Images",
                        "PDF",
                        "Other Files"
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select The File Type");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i==0){
                        checker = "image";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent,"Select An Image"),438);
                        }
                         if (i==1){
                        checker = "pdf";
                             Intent intent = new Intent();
                             intent.setAction(Intent.ACTION_GET_CONTENT);
                             intent.setType("application/pdf");
                             startActivityForResult(intent.createChooser(intent,"Select a PDF file"),438);

                        }
                        if (i==2){
                        checker = "docx";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(Intent.createChooser(intent, "Select MS Word File"), 438);
                        }
                    }
                });
                builder.show();
            }
        });
        rootRef.child("Messages").child(currentSenderID).child(receiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s)
                    {
                        Messages messages = dataSnapshot.getValue(Messages.class);

                        messagesList.add(messages);

                        messageAdapter.notifyDataSetChanged();

                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
    private void DisplayLastSeen(){
        rootRef.child("Users").child(receiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("userState").hasChild("state")){
                    String state  = snapshot.child("userState").child("state").getValue().toString();
                    String date  = snapshot.child("userState").child("date").getValue().toString();
                    String time  = snapshot.child("userState").child("time").getValue().toString();

                    if (state.equals("online")){
                        lastSeen.setText("online");
                    }
                    else if (state.equals("offline")){
                       lastSeen.setText("Last Seen: "+date+ " "+time);
                    }
                    else{
                        lastSeen.setText("offline");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void intialise() {
        sendDm = (Button) findViewById(R.id.sendDm_button);
        message = (EditText) findViewById(R.id.send_dm);
        sendFiles =(Button) findViewById(R.id.sendFiles_button);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd MMM,yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        messageAdapter  = new MessageAdapter(messagesList);
        userMessagesList = (RecyclerView) findViewById(R.id.PmMessageList);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);
        loadingBar = new ProgressDialog(this);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==438 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            loadingBar.setTitle("Sending File");
            loadingBar.setMessage("Hold on for few seconds while we send ur file");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
        fileUri = data.getData();
        if (!checker.equals("image")){
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");
            final String messageSenderRef = "Messages/" + currentSenderID + "/" + receiverID;
            final String messageReceiverRef = "Messages/" + receiverID + "/" + currentSenderID;

            DatabaseReference userMessageKey = rootRef.child("Messages").child(currentSenderID).child(receiverID).push();
            final String messagePushID = userMessageKey.getKey();

            final StorageReference filePath = storageReference.child(messagePushID + "." +checker);

            filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadUrl = uri.toString();

                            Map messageImageBody = new HashMap();
                            messageImageBody.put("message",downloadUrl);
                            messageImageBody.put("name",fileUri.getLastPathSegment());
                            messageImageBody.put("type",checker);
                            messageImageBody.put("from",currentSenderID);
                            messageImageBody.put("to", receiverID);
                            messageImageBody.put("messageKey", messagePushID);
                            messageImageBody.put("time", saveCurrentTime);
                            messageImageBody.put("date", saveCurrentDate);


                            Map messageBodyDetail = new HashMap();
                            messageBodyDetail.put(messageSenderRef+ "/" + messagePushID, messageImageBody);
                            messageBodyDetail.put(messageReceiverRef+ "/" + messagePushID, messageImageBody);

                            rootRef.updateChildren(messageBodyDetail);
                            loadingBar.dismiss();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            loadingBar.dismiss();
                            Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double p = (100.0* taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    loadingBar.setMessage((int) p + " % Uploading...");
                }
            });
        }
        else if (checker.equals("image")){
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");
            final String messageSenderRef = "Messages/" + currentSenderID + "/" + receiverID;
            final String messageReceiverRef = "Messages/" + receiverID + "/" + currentSenderID;

            DatabaseReference userMessageKey = rootRef.child("Messages").child(currentSenderID).child(receiverID).push();
           final String messagePushID = userMessageKey.getKey();

            final StorageReference filePath = storageReference.child(messagePushID + ".jpg");
            uploadTask = filePath.putFile(fileUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>(){
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                Uri downloadUri = task.getResult();
                URL = downloadUri.toString();


                    Map messageTextBody = new HashMap();
                    messageTextBody.put("message",URL);
                    messageTextBody.put("name",fileUri.getLastPathSegment());
                    messageTextBody.put("type",checker);
                    messageTextBody.put("from",currentSenderID);
                    messageTextBody.put("to",receiverID);
                    messageTextBody.put("messageKey",messagePushID);
                    messageTextBody.put("date",saveCurrentDate);
                    messageTextBody.put("time",saveCurrentTime);

                    Map messageBodyDetails  = new HashMap();
                    messageBodyDetails.put(messageSenderRef + "/"+ messagePushID , messageTextBody);
                    messageBodyDetails.put(messageReceiverRef + "/"+ messagePushID , messageTextBody);

                    rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()){
                                HashMap<String,String> chatNotif = new HashMap<>();
                                chatNotif.put("from",currentSenderID);
                                chatNotif.put("type","chat");

                                notifRef.child(receiverID).push().setValue(chatNotif);
                                loadingBar.dismiss();
                            }
                            else {
                                loadingBar.dismiss();
                                Toast.makeText(ChatActivity.this, "some error occured try again later!", Toast.LENGTH_SHORT).show();
                            }
                            message.setText("");
                        }
                    });

                }
                }
            });
        }
        else {
            //nothing selected
            loadingBar.dismiss();
        }
        }
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
            messageTextBody.put("to",receiverID);
            messageTextBody.put("messageKey",messagePushID);
            messageTextBody.put("date",saveCurrentDate);
            messageTextBody.put("time",saveCurrentTime);

            Map messageBodyDetails  = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/"+ messagePushID , messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/"+ messagePushID , messageTextBody);

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()){
                    HashMap<String,String> chatNotif = new HashMap<>();
                    chatNotif.put("from",currentSenderID);
                    chatNotif.put("type","chat");

                    notifRef.child(receiverID).push().setValue(chatNotif);
                }
                else {
                    Toast.makeText(ChatActivity.this, "some error occured try again later!", Toast.LENGTH_SHORT).show();
                }
                message.setText("");
                }
            });
        }

    }
}
package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private Button saveButton;
    private EditText name,status;
    private CircleImageView profilePic;
    private String CurrentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference ref;
    private StorageReference profileImageRef;
    private static final int galleryPic = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initialiseFields();
        mAuth= FirebaseAuth.getInstance();
        CurrentUserID = mAuth.getCurrentUser().getUid();
        ref = FirebaseDatabase.getInstance().getReference();
        profileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        saveButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
             saveSettings();
         }
     });
        retrieveInfo();
        // add profile pic
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,galleryPic);
            }
        });
    }

    private void retrieveInfo() {
        ref.child("Users").child(CurrentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.exists())&&(dataSnapshot.hasChild("name"))&&(dataSnapshot.hasChild("image")) ){
                    String getName = dataSnapshot.child("name").getValue().toString();
                    String getStatus = dataSnapshot.child("status").getValue().toString();
                    String getImage = dataSnapshot.child("image").getValue().toString();

                    name.setText(getName);
                    status.setText(getStatus);
                }
                else if ((dataSnapshot.exists())&&(dataSnapshot.hasChild("name"))){
                    String getName = dataSnapshot.child("name").getValue().toString();
                    String getStatus = dataSnapshot.child("status").getValue().toString();

                    name.setText(getName);
                    status.setText(getStatus);
                }
                else {
                    //update userdata
                    Toast.makeText(SettingsActivity.this, "Invalid Input", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void initialiseFields() {
        saveButton = (Button) findViewById(R.id.save);
        name  = (EditText) findViewById(R.id.set_user_name);
        status = (EditText) findViewById(R.id.set_about);
        profilePic = (CircleImageView) findViewById(R.id.profile_image);
    }

    private void saveSettings() {
    String username = name.getText().toString();
    String about = status.getText().toString();

    if (TextUtils.isEmpty(username)){
        Toast.makeText(this, "Name is a mandatory field", Toast.LENGTH_SHORT).show();
    }
    if (TextUtils.isEmpty(about)){
        Toast.makeText(this, "Try updating your status", Toast.LENGTH_SHORT).show();
    }
    else {
      HashMap<String,String> profileMap = new HashMap<>();
          profileMap.put("uid",CurrentUserID);
          profileMap.put("name",username);
          profileMap.put("status",about);
        ref.child("Users").child(CurrentUserID).setValue(profileMap).
                addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(SettingsActivity.this, "Profile updated Successfully", Toast.LENGTH_SHORT).show();
                            startMainActivity();
                        }
                        else {
                            String message = task.getException().getMessage().toString();
                            Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    }
        //cropping selected image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode ==galleryPic && resultCode==RESULT_OK && data!=null){
            Uri imageUri =  data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start( this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode ==RESULT_OK){
                Uri uri = result.getUri();

                StorageReference filePath  = profileImageRef.child(CurrentUserID + ".jpg");
                filePath.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(SettingsActivity.this, "Profile Picture updated successfully", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            String message = task.getException().getMessage().toString();
                            Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(SettingsActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
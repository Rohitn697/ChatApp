package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity {
    private Toolbar mainTool;
    private TabLayout myTabLayout;
    private ViewPager myViewPager;
    private tabsAccessorAdapter myTabsAccessor;
    private FirebaseUser currentuser;
    private FirebaseAuth mAuth;
    private DatabaseReference ref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainTool = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mainTool);
        getSupportActionBar().setTitle("WhatsUp Messenger");
        myTabLayout=(TabLayout) findViewById(R.id.main_tab);
        myViewPager=(ViewPager) findViewById(R.id.main_tab_pager);
        myTabsAccessor = new tabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessor);
        myTabLayout.setupWithViewPager(myViewPager);
        mAuth=FirebaseAuth.getInstance();
        currentuser = mAuth.getCurrentUser();
        ref = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(currentuser == null){
            loginActivity();
        }
        else{
            verifyExistence();
        }
    }

    private void verifyExistence() {
        String CurrentUserId = mAuth.getCurrentUser().getUid();
        ref.child("Users").child(CurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if ((dataSnapshot.child("name").exists())){
            //user exists
            }
            else {
                //new user bring settings screen
                settingActivity();
            }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.logout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
       super.onOptionsItemSelected(item);
       if (item.getItemId()==R.id.logout_user){
           mAuth.signOut();
           loginActivity();
       }
       if (item.getItemId() == R.id.user_settings){
           settingActivity();
       }
       if (item.getItemId()==R.id.user_friends){

       }
        if (item.getItemId()==R.id.user_groups){
            createGroup();
        }
       return true;
    }

    private void createGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter Group Name");
        final EditText groupName = new EditText(MainActivity.this);
        groupName.setHint("Enter Group name");
        builder.setView(groupName);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String group = groupName.getText().toString();
                if(TextUtils.isEmpty(group)){
                    Toast.makeText(MainActivity.this, "Group name should not be empty", Toast.LENGTH_SHORT).show();
                }
                else{
                    newGroup(group);
                }
            }
        });
        builder.setNegativeButton("Cancel",null);
        builder.show();
    }

    private void newGroup(final String GroupName) {
        ref.child("Groups").child(GroupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            if (task.isSuccessful()){
                Toast.makeText(MainActivity.this, GroupName + " is created", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(MainActivity.this, "Unknown Error caused during group creation", Toast.LENGTH_SHORT).show();
            }
            }
        });
    }

    private void settingActivity(){
        Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void loginActivity() {
        Intent intent = new Intent(MainActivity.this,LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
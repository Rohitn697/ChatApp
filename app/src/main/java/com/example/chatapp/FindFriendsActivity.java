package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {
    private Toolbar mainTool;
    private RecyclerView recyclerView;
    private DatabaseReference userRef;
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
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<contacts> firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<contacts>()
                .setQuery(userRef,contacts.class)
                .build();
        FirebaseRecyclerAdapter<contacts,findFriendsViewHolder> recyclerAdapter =  new FirebaseRecyclerAdapter<contacts, findFriendsViewHolder>(firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull findFriendsViewHolder holder, final int position, @NonNull contacts model) {
                holder.username.setText(model.getName());
                holder.userStatus.setText(model.getStatus());
                Picasso.get().load(model.getImage()).into(holder.profilePic);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String visited_user_id = getRef(position).getKey();
                        Intent intent = new Intent(FindFriendsActivity.this,profileActivity.class);
                        intent.putExtra("visited_uid",visited_user_id);
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public findFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                findFriendsViewHolder viewHolder =new findFriendsViewHolder(view);
                return viewHolder;
            }
        };
        recyclerView.setAdapter(recyclerAdapter);
        recyclerAdapter.startListening();
    }
    public static class findFriendsViewHolder extends RecyclerView.ViewHolder
    {       TextView username,userStatus;
            CircleImageView profilePic;
        public findFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            username =   itemView.findViewById(R.id.user_profileName);
            profilePic = itemView.findViewById(R.id.users_profile_pic);
            userStatus = itemView.findViewById(R.id.user_currentStatus);
        }
    }
}
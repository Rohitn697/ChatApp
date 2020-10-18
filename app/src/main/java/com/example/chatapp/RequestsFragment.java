package com.example.chatapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RequestsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RequestsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RequestsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RequestsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RequestsFragment newInstance(String param1, String param2) {
        RequestsFragment fragment = new RequestsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    private View requestFragmentView;
    private RecyclerView recyclerView;
    private DatabaseReference chatReqRef,UserRef;
    private FirebaseAuth mAuth;
    private String CurrentUserID;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestFragmentView =  inflater.inflate(R.layout.fragment_requests, container, false);
        recyclerView = (RecyclerView) requestFragmentView.findViewById(R.id.requestsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatReqRef = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        mAuth = FirebaseAuth.getInstance();
        CurrentUserID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        return requestFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<contacts> options = new FirebaseRecyclerOptions.Builder<contacts>().setQuery(chatReqRef.child(CurrentUserID),contacts.class).build();
        FirebaseRecyclerAdapter<contacts,requestViewHolder> adapter = new FirebaseRecyclerAdapter<contacts, requestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final requestViewHolder holder, int position, @NonNull contacts model) {
                holder.itemView.findViewById(R.id.acceptButton).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.rejectButton).setVisibility(View.VISIBLE);
                final String userId_list = getRef(position).getKey();
                DatabaseReference requestRef = getRef(position).child("request_type").getRef();
                requestRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            String type = snapshot.getValue().toString();
                            if (type.equals("received")){
                                UserRef.child(userId_list).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.hasChild("image")){
                                            String profileName = snapshot.child("name").getValue().toString();
                                            String profileStatus = snapshot.child("status").getValue().toString();
                                            String profilePhoto = snapshot.child("image").getValue().toString();

                                            holder.username.setText(profileName);
                                            holder.userStatus.setText(profileStatus);
                                            Picasso.get().load(profilePhoto).into(holder.userPic);
                                        }
                                        else{
                                            String profileName = snapshot.child("name").getValue().toString();
                                            String profileStatus = snapshot.child("status").getValue().toString();

                                            holder.username.setText(profileName);
                                            holder.userStatus.setText(profileStatus);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                           else if (type.equals("sent")){
                               Button requestSent = holder.itemView.findViewById(R.id.acceptButton);
                               requestSent.setText("Request Sent");

                               holder.itemView.findViewById(R.id.rejectButton).setVisibility(View.INVISIBLE);
                                UserRef.child(userId_list).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.hasChild("image")){
                                            String profileName = snapshot.child("name").getValue().toString();
                                            String profileStatus = snapshot.child("status").getValue().toString();
                                            String profilePhoto = snapshot.child("image").getValue().toString();

                                            holder.username.setText(profileName);
                                            holder.userStatus.setText(profileStatus);
                                            Picasso.get().load(profilePhoto).into(holder.userPic);
                                        }
                                        else{
                                            String profileName = snapshot.child("name").getValue().toString();
                                            String profileStatus = snapshot.child("status").getValue().toString();

                                            holder.username.setText(profileName);
                                            holder.userStatus.setText(profileStatus);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @NonNull
            @Override
            public requestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                requestViewHolder requestViewHolder = new requestViewHolder(view);
                return requestViewHolder;
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
        }
        public static class requestViewHolder extends RecyclerView.ViewHolder{
            TextView username,userStatus;
            CircleImageView userPic;
            Button accept,reject;
            public requestViewHolder(@NonNull View itemView) {
                super(itemView);
                username = itemView.findViewById(R.id.user_profileName);
                userStatus = itemView.findViewById(R.id.user_currentStatus);
                userPic = itemView.findViewById(R.id.users_profile_pic);
                accept = itemView.findViewById(R.id.acceptButton);
                reject =itemView.findViewById(R.id.rejectButton);
            }

        }

        }

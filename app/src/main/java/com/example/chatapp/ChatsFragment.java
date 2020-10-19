package com.example.chatapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 * Use the {@link ChatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ChatsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatsFragment newInstance(String param1, String param2) {
        ChatsFragment fragment = new ChatsFragment();
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
    private View chatsView;
    private RecyclerView chatsList;
    private DatabaseReference chatsRef,usersRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        chatsView =  inflater.inflate(R.layout.fragment_chats, container, false);
        chatsList = (RecyclerView) chatsView.findViewById(R.id.chats);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuth= FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatsRef= FirebaseDatabase.getInstance().getReference().child("contacts").child(currentUserID);
        return chatsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<contacts> options = new FirebaseRecyclerOptions.Builder<contacts>().
                setQuery(chatsRef,contacts.class)
                .build();

        FirebaseRecyclerAdapter<contacts,chatsViewHolder> adapter = new FirebaseRecyclerAdapter<contacts, chatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final chatsViewHolder holder, int position, @NonNull contacts model) {
            final String usersIDs = getRef(position).getKey();
                final String[] profilePhoto = {"default"};
            usersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
               if (snapshot.exists()){
                   if (snapshot.hasChild("image")){
                       profilePhoto[0] = snapshot.child("image").getValue().toString();
                       Picasso.get().load(profilePhoto[0]).into(holder.profilePic);
                   }
                   final String profileName = snapshot.child("name").getValue().toString();
                   String profileStatus = snapshot.child("status").getValue().toString();

                   holder.userName.setText(profileName);
                   holder.userStatus.setText("Last seen: ");

                    // on click goto chat activity
                   holder.itemView.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View view) {
                           Intent intent = new Intent(getContext(),ChatActivity.class);
                           intent.putExtra("visited_userID",usersIDs);
                           intent.putExtra("username",profileName);
                           intent.putExtra("image", profilePhoto[0]);
                           startActivity(intent);
                       }
                   });
               }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            }

            @NonNull
            @Override
            public chatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                chatsViewHolder chatsViewHolder = new chatsViewHolder(view);
                return chatsViewHolder;
            }
        };
        chatsList.setAdapter(adapter);
        adapter.startListening();

    }


    public static class chatsViewHolder extends RecyclerView.ViewHolder{
        CircleImageView profilePic;
        TextView userName,userStatus;
        public chatsViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePic = itemView.findViewById(R.id.users_profile_pic);
            userName = itemView.findViewById(R.id.user_profileName);
            userStatus = itemView.findViewById(R.id.user_currentStatus);

        }
    }

}
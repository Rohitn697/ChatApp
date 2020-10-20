package com.example.chatapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupMessagesAdapter extends RecyclerView.Adapter<GroupMessagesAdapter.groupMessageViewHolder> {
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    String currentUserID;
    private List<GroupMessages> groupMessage;

    public GroupMessagesAdapter(List<GroupMessages> groupMessages){
        this.groupMessage = groupMessages;
    }
    public static class groupMessageViewHolder extends RecyclerView.ViewHolder {
        public static TextView senderMessage;
        public static TextView receiverMessage;
        public static CircleImageView receiverProfilePic;
        public groupMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverMessage =(TextView) itemView.findViewById(R.id.receiver_message_text);
            senderMessage = (TextView) itemView.findViewById(R.id.sender_messsage_text);
            receiverProfilePic =(CircleImageView) itemView.findViewById(R.id.message_profile_image);
        }
    }

    @NonNull
    @Override
    public groupMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_messages_layout,parent,false);
        groupMessageViewHolder groupMessageViewHolder=new groupMessageViewHolder(view);
        mAuth = FirebaseAuth.getInstance();
        return groupMessageViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final groupMessageViewHolder holder, int position) {
        currentUserID=mAuth.getCurrentUser().getUid();
        GroupMessages groupMessages = groupMessage.get(position);

        String fromUserID = groupMessages.getUserid();
        String Date = groupMessages.getDate();
        String time = groupMessages.getDate();
        String chatMessage = groupMessages.getMessage();
        String name = groupMessages.getName();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("image")){
                    String ProfileImage = snapshot.child("image").getValue().toString();

                    Picasso.get().load(ProfileImage).placeholder(R.drawable.profile_image).into(holder.receiverProfilePic);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        groupMessageViewHolder.receiverMessage.setVisibility(View.INVISIBLE);
        groupMessageViewHolder.receiverProfilePic.setVisibility(View.INVISIBLE);
        if (fromUserID.equals(currentUserID)){
            groupMessageViewHolder.senderMessage.setBackgroundResource(R.drawable.sender_messages_layout);
            groupMessageViewHolder.senderMessage.setText(chatMessage);
        }
        else {
            groupMessageViewHolder.senderMessage.setVisibility(View.INVISIBLE);
            groupMessageViewHolder.receiverMessage.setVisibility(View.VISIBLE);
            groupMessageViewHolder.receiverProfilePic.setVisibility(View.VISIBLE);

            groupMessageViewHolder.receiverMessage.setBackgroundResource(R.drawable.receiver_messages_layout);
            groupMessageViewHolder.receiverMessage.setText(chatMessage);
        }
    }

    @Override
    public int getItemCount() {
        return groupMessage.size();
    }


}

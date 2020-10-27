package com.example.chatapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView senderDocView,receiverDocView,senderImageView,receiverImageView;

        public groupMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverMessageText =(TextView) itemView.findViewById(R.id.receiver_message_text);
            senderMessageText = (TextView) itemView.findViewById(R.id.sender_messsage_text);
            receiverProfileImage =(CircleImageView) itemView.findViewById(R.id.message_profile_image);
            senderDocView = (ImageView) itemView.findViewById(R.id.sender_Doc_view);
            receiverDocView = (ImageView) itemView.findViewById(R.id.receiver_Doc_view);
            senderImageView = (ImageView) itemView.findViewById(R.id.sender_image_view);
            receiverImageView = (ImageView) itemView.findViewById(R.id.receiver_image_view);
        }
    }

    @NonNull
    @Override
    public groupMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_messages_layout, parent, false);

        mAuth = FirebaseAuth.getInstance();
        return new groupMessageViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final groupMessageViewHolder holder, final int position) {
        String messageSenderId=mAuth.getCurrentUser().getUid();
        GroupMessages groupMessages = groupMessage.get(position);

        String fromUserID = groupMessages.getUid();
        String Date = groupMessages.getDate();
        String time = groupMessages.getDate();
        String chatMessage = groupMessages.getMessage();
        String name = groupMessages.getName();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("image")){

                    String receiverImage = snapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(holder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        holder.receiverMessageText.setVisibility(View.GONE);
        holder.receiverProfileImage.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);
        holder.senderDocView.setVisibility(View.INVISIBLE);
        holder.receiverDocView.setVisibility(View.INVISIBLE);
        holder.receiverImageView.setVisibility(View.GONE);
        holder.senderImageView.setVisibility(View.GONE);

        if (fromUserID.equals(messageSenderId)){
            holder.senderMessageText.setVisibility(View.VISIBLE);
            holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
            holder.senderMessageText.setText(groupMessages.getMessage()+ "\n \n"+ groupMessages.getDate() +", at "+groupMessages.getTime());
        }
        else {
            holder.receiverProfileImage.setVisibility(View.VISIBLE);
            holder.receiverMessageText.setVisibility(View.VISIBLE);

            holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
            holder.receiverMessageText.setText(groupMessages.getMessage()+ "\n \n" + groupMessages.getDate() +", at "+groupMessages.getTime());
        }
    }

    @Override
    public int getItemCount() {
        return groupMessage.size();
    }


}

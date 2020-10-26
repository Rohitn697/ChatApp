package com.example.chatapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.net.URI;
import java.net.URL;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;


    public MessageAdapter (List<Messages> userMessagesList)
    {
        this.userMessagesList = userMessagesList;
    }



    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView senderImageView,receiverImageView,senderDocView,receiverDocView;


        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);

            senderMessageText = (TextView) itemView.findViewById(R.id.sender_messsage_text);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            senderImageView = (ImageView) itemView.findViewById(R.id.sender_image_view);
            receiverImageView = (ImageView) itemView.findViewById(R.id.receiver_image_view);
            senderDocView = (ImageView) itemView.findViewById(R.id.sender_Doc_view);
            receiverDocView = (ImageView) itemView.findViewById(R.id.receiver_Doc_view);
        }
    }




    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, final int i)
    {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(i);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChild("image"))
                {
                    String receiverImage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });





        messageViewHolder.receiverMessageText.setVisibility(View.GONE);
        messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
        messageViewHolder.senderMessageText.setVisibility(View.GONE);
        messageViewHolder.receiverImageView.setVisibility(View.GONE);
        messageViewHolder.senderImageView.setVisibility(View.GONE);
        messageViewHolder.receiverDocView.setVisibility(View.GONE);
        messageViewHolder.senderDocView.setVisibility(View.GONE);


        if (fromMessageType.equals("Text"))
        {
            if (fromUserID.equals(messageSenderId))
            {
                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);

                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                messageViewHolder.senderMessageText.setText(messages.getMessage()+ "\n \n"+ messages.getDate() +", at "+messages.getTime());
            }
            else
            {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);

                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                messageViewHolder.receiverMessageText.setText(messages.getMessage()+ "\n \n" + messages.getDate() +", at "+messages.getTime());
            }
        }
        else if (fromMessageType.equals("image"))
        {
            if (fromUserID.equals(messageSenderId)){
                messageViewHolder.senderImageView.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(messageViewHolder.senderImageView);
            }
            else {
                messageViewHolder.receiverImageView.setVisibility(View.VISIBLE);
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(messageViewHolder.receiverImageView);
            }
        }
        else if (fromMessageType.equals("pdf") || fromMessageType.equals("docx")){
            if (fromUserID.equals(messageSenderId)){
                messageViewHolder.senderDocView.setVisibility(View.VISIBLE);

            }
            else {
                messageViewHolder.receiverDocView.setVisibility(View.VISIBLE);
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);

            }
        }
        if (fromUserID.equals(messageSenderId)) {
            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (userMessagesList.get(i).getType().equals("pdf")|| userMessagesList.get(i).getType().equals("docx")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Download And View",
                                "Delete for everyone",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int position) {
                           if (position==0){
                               deleteSentMessages(i,messageViewHolder);
                               Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                               messageViewHolder.itemView.getContext().startActivity(intent);

                           }
                           else   if (position==1){
                               Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(i).getMessage()));
                               messageViewHolder.itemView.getContext().startActivity(intent);
                           }
                           else if (position==2){
                            deleteForEveryone(i,messageViewHolder);
                               Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                               messageViewHolder.itemView.getContext().startActivity(intent);
                           }
                            }
                        });
                        builder.show();
                    }
                    else if (userMessagesList.get(i).getType().equals("Text")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Delete for everyone",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int position) {
                                if (position==0){
                                    deleteSentMessages(i,messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (position==1){
                                    deleteForEveryone(i,messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if (userMessagesList.get(i).getType().equals("image")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "View the Image",
                                "Delete for everyone",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int position) {
                                if (position==0){
                                    deleteSentMessages(i,messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);

                                }
                                else if (position==1){
                                Intent intent = new Intent(messageViewHolder.itemView.getContext(),ImageViewerActivity.class);
                                intent.putExtra("url",userMessagesList.get(i).getMessage());
                                messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else   if (position==2){
                                    deleteForEveryone(i,messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }
        else {
            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (userMessagesList.get(i).getType().equals("pdf")|| userMessagesList.get(i).getType().equals("docx")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Download And View",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int position) {
                                if (position==0){
                                    deleteReceivedMessages(i,messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else   if (position==1){
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(i).getMessage()));
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if (userMessagesList.get(i).getType().equals("Text")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int position) {
                                if (position==0){
                                    deleteReceivedMessages(i,messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();
                    }
                    else if (userMessagesList.get(i).getType().equals("image")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "View the Image",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int position) {
                                if (position==0){
                                    deleteReceivedMessages(i,messageViewHolder);
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),MainActivity.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (position==1){
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(),ImageViewerActivity.class);
                                    intent.putExtra("url",userMessagesList.get(i).getMessage());
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
    }
    private void deleteSentMessages(final int i,final MessageViewHolder holder){
    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
            rootRef.child("Messages")
            .child(userMessagesList.get(i).getFrom())
            .child(userMessagesList.get(i).getTo())
                    .child(userMessagesList.get(i).getMessageKey())
                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(holder.itemView.getContext(), "Message Deleted Successfully!", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(holder.itemView.getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }
    private void deleteReceivedMessages(final int i,final MessageViewHolder holder){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(i).getTo())
                .child(userMessagesList.get(i).getFrom())
                .child(userMessagesList.get(i).getMessageKey())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), "Message Deleted Successfully!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteForEveryone(final int i,final MessageViewHolder holder){
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(i).getTo())
                .child(userMessagesList.get(i).getFrom())
                .child(userMessagesList.get(i).getMessageKey())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    rootRef.child("Messages")
                            .child(userMessagesList.get(i).getFrom())
                            .child(userMessagesList.get(i).getTo())
                            .child(userMessagesList.get(i).getMessageKey())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(holder.itemView.getContext(), "Message Deleted Successfully!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
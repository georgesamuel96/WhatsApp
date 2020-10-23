package com.example.georgesamuel.whatsapp;

import android.graphics.Color;
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

import static com.example.georgesamuel.whatsapp.Constants.*;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    public MessageAdapter(List<Message> userMessagesList) {
        this.userMessagesList = userMessagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mAuth = FirebaseAuth.getInstance();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_message_layout, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Message message = userMessagesList.get(position);

        String fromUserId = message.getFrom();
        String messageType = message.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child(ROOT_USERS).child(fromUserId);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(USER_IMAGE_PATH)){

                } else {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.receiverMessageText.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);
        holder.messageSenderPicture.setVisibility(View.GONE);
        holder.messageReceiverPicture.setVisibility(View.GONE);

        if(messageType.equals(BODY_MESSAGE_TYPE_TEXT)){

            if(fromUserId.equals(messageSenderId)) {
                holder.senderMessageText.setVisibility(View.VISIBLE);

                holder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
                holder.senderMessageText.setTextColor(Color.BLACK);
                holder.senderMessageText.setText(message.getMessage());
            }
            else {
                holder.receiverMessageText.setVisibility(View.VISIBLE);

                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_layout);
                holder.receiverMessageText.setTextColor(Color.BLACK);
                holder.receiverMessageText.setText(message.getMessage());
            }
        }
        else if(messageType.equals(BODY_MESSAGE_TYPE_IMAGE)) {

            if(fromUserId.equals(messageSenderId)) {
                holder.messageSenderPicture.setVisibility(View.VISIBLE);

                Picasso.get().load(message.getMessage()).fit().into(holder.messageSenderPicture);
            }
            else {
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);

                Picasso.get().load(message.getMessage()).fit().into(holder.messageReceiverPicture);
            }
        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        TextView senderMessageText, receiverMessageText;
        ImageView messageSenderPicture, messageReceiverPicture;

        public MessageViewHolder(View view) {
            super(view);

            senderMessageText = view.findViewById(R.id.tvSenderMessage);
            receiverMessageText = view.findViewById(R.id.tvReceiverMessage);
            messageSenderPicture = view.findViewById(R.id.image_message_sender);
            messageReceiverPicture = view.findViewById(R.id.image_message_receiver);
        }
    }
}

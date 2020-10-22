package com.example.georgesamuel.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.georgesamuel.whatsapp.Constants.*;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = ChatActivity.class.getSimpleName();
    public static final String EXTRA_USER_RECEIVER_ID  = "EXTRA_USER_RECEIVER_ID";
    private User userReceiver = new User();
    private Toolbar toolbar;
    private ImageButton sendMessageButton;
    private EditText messageInputText;
    private FirebaseAuth mAuth;
    private String receiverId, senderId;
    private DatabaseReference rootRef;
    private final List<Message> messageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView rvMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if(getIntent().hasExtra(EXTRA_USER_RECEIVER_ID)) {
            userReceiver = (User) getIntent().getSerializableExtra(EXTRA_USER_RECEIVER_ID);
            receiverId = userReceiver.getUserId();
        }

        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        senderId = mAuth.getCurrentUser().getUid();
        initView();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(userReceiver.getName());

        sendMessageButton = findViewById(R.id.sendMessageButton);
        messageInputText = findViewById(R.id.input_message);

        messageAdapter = new MessageAdapter(messageList);
        rvMessages = findViewById(R.id.private_message_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        rvMessages.setLayoutManager(linearLayoutManager);
        rvMessages.setHasFixedSize(true);
        rvMessages.setAdapter(messageAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        rootRef.child(ROOT_MESSAGES).child(senderId).child(receiverId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message message = dataSnapshot.getValue(Message.class);
                messageList.add(message);
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage() {
        String message = messageInputText.getText().toString().trim();
        if(message.equals(""))
            return;

        String messageSenderRef = ROOT_MESSAGES + "/" + senderId + "/" + receiverId;
        String messageReceiverRef = ROOT_MESSAGES + "/" + receiverId + "/" + senderId;

        DatabaseReference userMessageKeyRef = rootRef.child(ROOT_MESSAGES).child(senderId).child(receiverId).push();
        String messagePushId = userMessageKeyRef.getKey();
        Map messageTextBody = new HashMap();
        messageTextBody.put(BODY_MESSAGE_MESSAGE, message);
        messageTextBody.put(BODY_MESSAGE_TYPE, BODY_MESSAGE_TYPE_TEXT);
        messageTextBody.put(BODY_MESSAGE_FROM, senderId);

        Map messageBodyDetails = new HashMap();
        messageBodyDetails.put(messageSenderRef + "/" + messagePushId, messageTextBody);
        messageBodyDetails.put(messageReceiverRef + "/" + messagePushId, messageTextBody);
        rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()) {
                    Log.d(TAG, "Message Sent Successfully");
                }
                else {
                    Log.d(TAG, task.getException().getMessage());
                }
                messageInputText.setText("");
            }
        });
    }
}
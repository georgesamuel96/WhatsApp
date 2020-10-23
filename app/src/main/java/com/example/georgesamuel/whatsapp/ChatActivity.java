package com.example.georgesamuel.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

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
    private ImageButton sendMessageButton, sendFileButton;
    private EditText messageInputText;
    private FirebaseAuth mAuth;
    private String receiverId, senderId;
    private DatabaseReference rootRef;
    private final List<Message> messageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView rvMessages;
    private String checker = "", fileUrl = "";
    private StorageTask uploadTask;
    private Uri fileUri;
    private static final int IMAGE_REQUEST_CODE = 438;
    private ProgressDialog loadingBar;

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

        sendFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFile();
            }
        });

        getMessages();
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(userReceiver.getName());

        sendMessageButton = findViewById(R.id.sendMessageButton);
        sendFileButton = findViewById(R.id.sendFileButton);
        messageInputText = findViewById(R.id.input_message);

        messageAdapter = new MessageAdapter(messageList);
        rvMessages = findViewById(R.id.private_message_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        rvMessages.setLayoutManager(linearLayoutManager);
        rvMessages.setHasFixedSize(true);
        rvMessages.setAdapter(messageAdapter);

        loadingBar = new ProgressDialog(this);
    }

    private void getMessages() {
        rootRef.child(ROOT_MESSAGES).child(senderId).child(receiverId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message message = dataSnapshot.getValue(Message.class);
                messageList.add(message);
                messageAdapter.notifyDataSetChanged();
                rvMessages.smoothScrollToPosition(rvMessages.getAdapter().getItemCount());
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
        messageTextBody.put(BODY_MESSAGE_ID, messagePushId);

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

    private void uploadFile() {
        CharSequence options[] = new CharSequence[]
                {
                        "Images",
                        "PDF Files",
                        "Ms Word Files"
                };
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle("Select the file");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0: {
                        checker = BODY_MESSAGE_TYPE_IMAGE;
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(intent.createChooser(intent, "Select Image"), IMAGE_REQUEST_CODE);
                        break;
                    }
                    case 1: {
                        checker = BODY_MESSAGE_TYPE_PDF;
                        break;
                    }
                    case 2: {
                        checker = BODY_MESSAGE_TYPE_DOCS;
                        break;
                    }
                    default:{
                        break;
                    }
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {

            loadingBar.setTitle("Sending File");
            loadingBar.setMessage("Please wait, we are sending that file...");
            loadingBar.setCancelable(false);
            loadingBar.show();

            fileUri = data.getData();
            if(!checker.equals(BODY_MESSAGE_TYPE_IMAGE)) {
                loadingBar.dismiss();
            } else {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                        .child(STORE_IMAGES);

                final String messageSenderRef = ROOT_MESSAGES + "/" + senderId + "/" + receiverId;
                final String messageReceiverRef = ROOT_MESSAGES + "/" + receiverId + "/" + senderId;

                DatabaseReference userMessageKeyRef = rootRef.child(ROOT_MESSAGES).child(senderId).child(receiverId).push();
                final String messagePushId = userMessageKeyRef.getKey();
                final StorageReference filePath = storageReference.child(messagePushId + "." + "jpg");
                uploadTask = filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if(!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            Uri downloadUrl = task.getResult();
                            fileUrl = downloadUrl.toString();

                            Map messageTextBody = new HashMap();
                            messageTextBody.put(BODY_MESSAGE_MESSAGE, fileUrl);
                            messageTextBody.put(BODY_MESSAGE_NAME, fileUri.getLastPathSegment());
                            messageTextBody.put(BODY_MESSAGE_TYPE, checker);
                            messageTextBody.put(BODY_MESSAGE_FROM, senderId);
                            messageTextBody.put(BODY_MESSAGE_ID, messagePushId);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushId, messageTextBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushId, messageTextBody);
                            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    loadingBar.dismiss();
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
                });
            }
        }
    }
}
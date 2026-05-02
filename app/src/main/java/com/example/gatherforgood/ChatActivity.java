package com.example.gatherforgood;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class ChatActivity extends AppCompatActivity {

    TextView     tvChatName, tvChatSubtitle;
    RecyclerView rvMessages;
    EditText     etMessage;
    ImageButton  btnBack, btnSend;

    FirebaseDatabase  db;
    DatabaseReference messagesRef;

    String  currentUid;
    String  currentUserName;
    String  chatId;
    String  chatName;
    String  chatType;
    boolean isGroup;

    List<Message>  messageList;
    MessageAdapter adapter;
    ChildEventListener childEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatId   = getIntent().getStringExtra("chatId");
        chatName = getIntent().getStringExtra("chatName");
        chatType = getIntent().getStringExtra("chatType");
        isGroup  = "group".equals(chatType);

        db        = FirebaseDatabase.getInstance();
        currentUid  = FirebaseAuth.getInstance().getCurrentUser().getUid();

        messagesRef = db.getReference("chats").child(chatId).child("messages");

        init();
        fetchCurrentUserName();
    }

    private void init() {
        tvChatName     = findViewById(R.id.tvChatName);
        tvChatSubtitle = findViewById(R.id.tvChatSubtitle);
        rvMessages     = findViewById(R.id.rvMessages);
        etMessage      = findViewById(R.id.etMessage);
        btnBack        = findViewById(R.id.btnBack);
        btnSend        = findViewById(R.id.btnSend);

        tvChatName.setText(chatName);
        tvChatSubtitle.setText(isGroup ? "Group Chat" : "Direct Message");

        messageList = new ArrayList<>();
        adapter     = new MessageAdapter(messageList, currentUid, isGroup);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // newest messages at bottom
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> sendMessage());

        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void fetchCurrentUserName() {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUid)
                .get()
                .addOnSuccessListener(doc -> {
                    currentUserName = doc.getString("name");
                    if (currentUserName == null) currentUserName = "Unknown";
                    listenForMessages();
                })
                .addOnFailureListener(e -> {
                    currentUserName = "Unknown";
                    listenForMessages();
                });
    }

    private void listenForMessages() {
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                if (message != null) {
                    messageList.add(message);
                    adapter.notifyItemInserted(messageList.size() - 1);
                    rvMessages.scrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onCancelled(DatabaseError error) {}
        };

        messagesRef.orderByChild("timestamp")
                .addChildEventListener(childEventListener);
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        etMessage.setText("");

        String messageId = messagesRef.push().getKey();
        if (messageId == null) return;

        Message message = new Message(
                messageId,
                currentUid,
                currentUserName,
                text,
                System.currentTimeMillis()
        );

        messagesRef.child(messageId).setValue(message)
                .addOnFailureListener(e -> {
                    etMessage.setText(text);
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (childEventListener != null) {
            messagesRef.removeEventListener(childEventListener);
        }
    }
}
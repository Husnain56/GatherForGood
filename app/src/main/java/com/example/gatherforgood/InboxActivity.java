package com.example.gatherforgood;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InboxActivity extends AppCompatActivity {

    TextView     tvInboxTitle, tvInboxSubtitle;
    RecyclerView rvInbox;
    ProgressBar  progressBar;
    View         layoutEmpty;
    ImageButton  btnBack;

    FirebaseDatabase  db;
    String currentUid;
    String eventId;
    String eventTitle;

    List<InboxThread> threadList;
    InboxAdapter      adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        eventId    = getIntent().getStringExtra("eventId");
        eventTitle = getIntent().getStringExtra("eventTitle");
        db         = FirebaseDatabase.getInstance();

        init();
        loadDmThreads();
    }

    private void init() {
        tvInboxTitle    = findViewById(R.id.tvInboxTitle);
        tvInboxSubtitle = findViewById(R.id.tvInboxSubtitle);
        rvInbox         = findViewById(R.id.rvInbox);
        progressBar     = findViewById(R.id.progressBar);
        layoutEmpty     = findViewById(R.id.layoutEmpty);
        btnBack         = findViewById(R.id.btnBack);

        tvInboxTitle.setText("Messages");
        tvInboxSubtitle.setText(eventTitle != null ? eventTitle : "");

        threadList = new ArrayList<>();
        adapter    = new InboxAdapter(threadList, this::openThread);

        rvInbox.setLayoutManager(new LinearLayoutManager(this));
        rvInbox.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadDmThreads() {
        android.util.Log.d("INBOX", "eventId received: " + eventId);
        android.util.Log.d("INBOX", "currentUid: " + currentUid);
        progressBar.setVisibility(View.VISIBLE);

        String prefix    = eventId + "_dm_";
        String prefixEnd = eventId + "_dm_~";

        Query query = db.getReference("chats")
                .orderByKey()
                .startAt(prefix)
                .endAt(prefixEnd);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                android.util.Log.d("INBOX", "snapshot exists: " + snapshot.exists());
                android.util.Log.d("INBOX", "children count: " + snapshot.getChildrenCount());
                android.util.Log.d("INBOX", "prefix used: " + prefix);
                threadList.clear();

                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                    android.util.Log.d("INBOX", "found chat key: " + chatSnapshot.getKey());
                    String chatId = chatSnapshot.getKey();
                    if (chatId == null) continue;

                    String participantUid = chatId.replace(prefix, "");

                    String lastMessageText = "";
                    long   lastMessageTime = 0;
                    String participantName = "";

                    DataSnapshot messages = chatSnapshot.child("messages");
                    for (DataSnapshot msgSnapshot : messages.getChildren()) {
                        Message msg = msgSnapshot.getValue(Message.class);
                        if (msg == null) continue;

                        if (msg.getTimestamp() >= lastMessageTime) {
                            lastMessageTime = msg.getTimestamp();
                            lastMessageText = msg.getText();
                            if (!msg.getSenderId().equals(currentUid)) {
                                participantName = msg.getSenderName();
                            }
                        }
                    }

                    if (lastMessageTime == 0) continue;

                    if (participantName.isEmpty()) {
                        participantName = "Participant";
                    }

                    threadList.add(new InboxThread(
                            participantUid,
                            participantName,
                            lastMessageText,
                            lastMessageTime,
                            chatId
                    ));
                }

                Collections.sort(threadList,
                        (a, b) -> Long.compare(
                                b.getLastMessageTime(),
                                a.getLastMessageTime()));

                progressBar.setVisibility(View.GONE);

                if (threadList.isEmpty()) {
                    layoutEmpty.setVisibility(View.VISIBLE);
                    rvInbox.setVisibility(View.GONE);
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                    rvInbox.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void openThread(InboxThread thread) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("chatType", "direct");
        intent.putExtra("chatId",   thread.getChatId());
        intent.putExtra("chatName", thread.getParticipantName());
        startActivity(intent);
    }
}
package com.example.gatherforgood;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT     = 1;
    private static final int VIEW_TYPE_RECEIVED = 0;

    private final List<Message> messages;
    private final String        currentUid;
    private final boolean       isGroupChat;

    public MessageAdapter(List<Message> messages, String currentUid, boolean isGroupChat) {
        this.messages    = messages;
        this.currentUid  = currentUid;
        this.isGroupChat = isGroupChat;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getSenderId().equals(currentUid)
                ? VIEW_TYPE_SENT
                : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_SENT) {
            View view = inflater.inflate(R.layout.item_message_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_message_received, parent, false);
            return new ReceivedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (holder instanceof SentViewHolder) {
            ((SentViewHolder) holder).bind(message);
        } else {
            ((ReceivedViewHolder) holder).bind(message, isGroupChat);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class SentViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageText, tvTimestamp;

        SentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvTimestamp   = itemView.findViewById(R.id.tvTimestamp);
        }

        void bind(Message message) {
            tvMessageText.setText(message.getText());
            tvTimestamp.setText(formatTime(message.getTimestamp()));
        }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView tvSenderName, tvMessageText, tvTimestamp;

        ReceivedViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSenderName  = itemView.findViewById(R.id.tvSenderName);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvTimestamp   = itemView.findViewById(R.id.tvTimestamp);
        }

        void bind(Message message, boolean isGroupChat) {
            tvMessageText.setText(message.getText());
            tvTimestamp.setText(formatTime(message.getTimestamp()));

            if (isGroupChat) {
                tvSenderName.setVisibility(View.VISIBLE);
                String name = message.getSenderName();
                if ("host".equals(message.getRole())) {
                    tvSenderName.setText(name + " (Host)");
                    tvSenderName.setTextColor(
                            android.graphics.Color.parseColor("#D4AF37"));
                } else {
                    tvSenderName.setText(name);
                    tvSenderName.setTextColor(
                            android.graphics.Color.parseColor("#80FFFFFF"));
                }
            } else {
                tvSenderName.setVisibility(View.GONE);
            }
        }
    }

    private static String formatTime(long timestamp) {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault())
                .format(new Date(timestamp));
    }
}
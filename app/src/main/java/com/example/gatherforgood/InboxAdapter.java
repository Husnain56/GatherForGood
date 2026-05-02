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

public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.ViewHolder> {

    public interface OnThreadClickListener {
        void onThreadClick(InboxThread thread);
    }

    private final List<InboxThread>     threads;
    private final OnThreadClickListener listener;

    public InboxAdapter(List<InboxThread> threads, OnThreadClickListener listener) {
        this.threads  = threads;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inbox_thread, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(threads.get(position));
    }

    @Override
    public int getItemCount() { return threads.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvParticipantName, tvLastMessage, tvThreadTime;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvParticipantName = itemView.findViewById(R.id.tvParticipantName);
            tvLastMessage     = itemView.findViewById(R.id.tvLastMessage);
            tvThreadTime      = itemView.findViewById(R.id.tvThreadTime);
        }

        void bind(InboxThread thread) {
            tvParticipantName.setText(thread.getParticipantName());
            tvLastMessage.setText(thread.getLastMessage());
            tvThreadTime.setText(formatTime(thread.getLastMessageTime()));
            itemView.setOnClickListener(v -> listener.onThreadClick(thread));
        }
    }

    private String formatTime(long timestamp) {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault())
                .format(new Date(timestamp));
    }
}
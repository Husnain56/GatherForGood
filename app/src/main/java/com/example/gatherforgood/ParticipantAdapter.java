package com.example.gatherforgood;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ParticipantAdapter extends RecyclerView.Adapter<ParticipantAdapter.ViewHolder> {

    public interface OnParticipantClickListener {
        void onParticipantClick(ParticipantItem item);
    }

    private final Context                    context;
    private final ArrayList<ParticipantItem> list;
    private final OnParticipantClickListener listener;

    public ParticipantAdapter(Context context,
                              ArrayList<ParticipantItem> list,
                              OnParticipantClickListener listener) {
        this.context  = context;
        this.list     = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_participant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParticipantItem item = list.get(position);

        String name = item.name != null && !item.name.isEmpty() ? item.name : "?";
        holder.tvAvatar.setText(String.valueOf(name.charAt(0)).toUpperCase());
        holder.tvName.setText(name);

        String gender = item.gender != null && !item.gender.isEmpty() ? item.gender : "—";
        holder.tvGender.setText(gender);

        holder.tvJoinedAt.setText(formatJoinedAt(item.joinedAt));

        if ("host".equals(item.role)) {
            holder.tvRole.setVisibility(View.VISIBLE);
        } else {
            holder.tvRole.setVisibility(View.GONE);
        }

    }

    private String formatJoinedAt(long millis) {
        if (millis == 0) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());
        return "Joined " + sdf.format(new Date(millis));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvName, tvGender, tvJoinedAt, tvRole;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar   = itemView.findViewById(R.id.tvAvatar);
            tvName     = itemView.findViewById(R.id.tvParticipantName);
            tvGender   = itemView.findViewById(R.id.tvParticipantGender);
            tvJoinedAt = itemView.findViewById(R.id.tvJoinedAt);
            tvRole     = itemView.findViewById(R.id.tvRole);
        }
    }
}
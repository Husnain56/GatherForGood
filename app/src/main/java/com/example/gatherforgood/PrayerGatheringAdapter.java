package com.example.gatherforgood;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.format.TextStyle;
import java.util.ArrayList;

public class PrayerGatheringAdapter extends RecyclerView.Adapter<PrayerGatheringAdapter.PrayerViewHolder> {

    Context context;
    ArrayList<PrayerGathering> list;

    public PrayerGatheringAdapter(Context context, ArrayList<PrayerGathering> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public PrayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_prayer_card, parent, false);
        return new PrayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PrayerViewHolder holder, int position) {
        PrayerGathering gathering = list.get(position);

        holder.tvStatus.setText(gathering.getStatus());
        holder.tvPrayerName.setText(gathering.getPrayerType());
        holder.tvTime.setText(gathering.getTime());
        holder.tvHostName.setText("Hosted by " + gathering.getHostName());
        holder.tvLocation.setText(gathering.getLocation());
        holder.tvParticipantCount.setText(gathering.getParticipantCount() + " joined");


        holder.btnJoin.setOnClickListener(v -> {
            navigateToDetails(v.getContext(), gathering);
        });

        // Logic for clicking the WHOLE card
        holder.itemView.setOnClickListener(v -> {
            navigateToDetails(v.getContext(), gathering);
        });
    }

    private void navigateToDetails(Context context, PrayerGathering gathering) {
        Intent intent = new Intent(context, GatheringDetails.class);
        intent.putExtra("prayer_gathering",gathering);
        context.startActivity(intent);
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class PrayerViewHolder extends RecyclerView.ViewHolder {
        TextView tvStatus, tvPrayerName, tvTime, tvHostName, tvLocation, tvDistance, tvParticipantCount;
        Button btnJoin;

        View mainCard;

        public PrayerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPrayerName = itemView.findViewById(R.id.tvPrayerName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvHostName = itemView.findViewById(R.id.tvHostName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvParticipantCount = itemView.findViewById(R.id.tvParticipantCount);
            btnJoin = itemView.findViewById(R.id.btnJoin);
            mainCard = itemView.findViewById(R.id.main);
        }
    }
}

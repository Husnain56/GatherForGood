package com.example.gatherforgood;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {

    Context context;
    ArrayList<Event> list;

    public EventsAdapter(Context context, ArrayList<Event> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event_card, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = list.get(position);
        holder.tvEventTitle.setText(event.getTitle());
        holder.tvEventDate.setText(event.getDate());
        holder.tvEventLocation.setText(event.getLocation());
        holder.tvEventStatus.setText(event.getStatus());
        holder.tvSlotsFilled.setText(event.getVolunteersJoined() + "/" + event.getVolunteersRequired());
        holder.btnRequestJoin.setOnClickListener(v -> {
            navigateToDetails(context,event);
        });
        holder.mainCard.setOnClickListener(v -> {
            navigateToDetails(context,event);
        });
    }

    private void navigateToDetails(Context context,Event event) {
        Intent intent = new Intent(context, EventDetails.class);
        intent.putExtra("event", event);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public static class EventViewHolder extends RecyclerView.ViewHolder {

        TextView tvEventTitle, tvEventDate, tvEventLocation, tvSlotsFilled, tvEventStatus;
        Button btnRequestJoin;
        ConstraintLayout mainCard;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventLocation = itemView.findViewById(R.id.tvEventLocation);
            tvSlotsFilled = itemView.findViewById(R.id.tvSlotsFilled);
            btnRequestJoin = itemView.findViewById(R.id.btnRequestJoin);
            mainCard = itemView.findViewById(R.id.main);
            tvEventStatus = itemView.findViewById(R.id.tvEventStatus);
        }
    }
}

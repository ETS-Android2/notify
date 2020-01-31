package com.notify.myapplication.Adapters;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.notify.myapplication.ImageOperations.PicassoCircleTransformation;
import com.notify.myapplication.Listeners.OnRecycleItemClickedListener;
import com.notify.myapplication.Models.MyEvent;
import com.notify.myapplication.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;

public class AdapderEventCard extends RecyclerView.Adapter<AdapderEventCard.eventCardHolder> {

    private static final String TAG = "AdapterEventCard: ";

    //Def adapter variables
    private List<MyEvent> eventList;
    private Context context;
    private OnRecycleItemClickedListener listenerForEvent;
    private OnRecycleItemClickedListener listenerForOrganisator;

    //Constructor
    public AdapderEventCard(List<MyEvent> eventList, Context context, OnRecycleItemClickedListener listener, OnRecycleItemClickedListener listener2) {
        this.eventList = eventList;
        this.context = context;
        this.listenerForEvent = listener;
        this.listenerForOrganisator = listener2;
    }

    @NonNull
    @Override   //First creation
    public eventCardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.event_card,parent,false);
        return new eventCardHolder(v);
    }

    @Override   //Binding data to ui
    public void onBindViewHolder(@NonNull final eventCardHolder holder, int position) {
        final MyEvent tempEvent = eventList.get(position);
        try {
            //Get event image and set
            Picasso.get().load(tempEvent.getEventImageUri()).into(holder.eventImage, new Callback() {
                @Override
                public void onSuccess() {
                    BitmapDrawable drawable = (BitmapDrawable) holder.eventImage.getDrawable();
                    tempEvent.setEventImg(drawable.getBitmap());
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG,"Error: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            Log.w(TAG, e.getMessage());
        }
        holder.eventName.setText(tempEvent.getEventName());
        //Parse event timestamp
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        holder.eventDate.setText(dateFormat.format(tempEvent.getEventDateAndTime().toDate()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Notify adapter owner that an event is clicked
                listenerForEvent.onItemOfListClicked(tempEvent);
            }
        });

        if(tempEvent.getOrganisator()!=null){
            //If event has a user, set it to ui (first time data loads, there wont be any organisators, they will be added after events are loaded)
            holder.organisatorName.setText(tempEvent.getOrganisator().getName());
            Picasso.get().load(tempEvent.getOrganisator().getProfileImage()).transform(new PicassoCircleTransformation()).into(holder.organisatorImage);
            holder.organisatorLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Notify adapter owner an organisator is clicked
                    listenerForOrganisator.onItemOfListClicked(tempEvent.getOrganisator());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public void setMyEventList(List<MyEvent> myEventList) {
        this.eventList = myEventList;
    }


    public class eventCardHolder extends RecyclerView.ViewHolder{

        //Card view properties
        ImageView eventImage,organisatorImage;
        TextView eventName,eventDate, organisatorName;
        LinearLayout organisatorLayout;

        public eventCardHolder(@NonNull View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.event_card_img);
            eventName = itemView.findViewById(R.id.event_card_name);
            eventDate = itemView.findViewById(R.id.event_card_date);
            organisatorImage = itemView.findViewById(R.id.event_card_organisator_image);
            organisatorName = itemView.findViewById(R.id.event_card_organisator_name);
            organisatorLayout = itemView.findViewById(R.id.event_card_organisator);
        }
    }
}

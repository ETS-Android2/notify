package com.example.myapplication.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Listeners.OnRecycleItemClickedListener;
import com.example.myapplication.Models.MyImage;
import com.example.myapplication.myapplication.notify_beta.R;

import java.util.ArrayList;

public class AdapterSelectedImages extends RecyclerView.Adapter<AdapterSelectedImages.SelectedImageHolder> {

    private static final String TAG = "AdapterSelectedImages: ";

    //Def adapter variables
    private ArrayList<MyImage> selectedImageList;
    private Context mContext;
    private OnRecycleItemClickedListener listener;

    //Constructor
    public AdapterSelectedImages(ArrayList<MyImage> selectedImageList, Context mContext, OnRecycleItemClickedListener listener) {
        this.selectedImageList = selectedImageList;
        this.mContext = mContext;
        this.listener = listener;
    }

    @NonNull
    @Override   //First creation
    public SelectedImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.event_card_gallery, parent, false);
        return new SelectedImageHolder(v);
    }

    @Override   //Binding data to ui (Retrieved images from android gallery)
    public void onBindViewHolder(@NonNull final SelectedImageHolder holder, int position) {
        final MyImage uri = selectedImageList.get(position);
        holder.selectedImage.setImageBitmap(uri.getBitmap());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Notify adapter owner if on card clicked
                listener.onItemOfListClicked(uri);
            }
        });
        holder.checkBox.setVisibility(View.GONE);
        //Manage ui for upload process
        if(uri.isSelected()){
            holder.progress.setVisibility(View.VISIBLE);
            holder.selectedImage.setForeground(mContext.getResources().getDrawable(R.drawable.black_gradient,mContext.getTheme()));
            holder.progress.setText(uri.getProgress()+" %");
        }else{
            holder.progress.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return selectedImageList.size();
    }

    public void setSelectedImageList(ArrayList<MyImage> selectedImageList) {
        this.selectedImageList = selectedImageList;
    }

    public class SelectedImageHolder extends RecyclerView.ViewHolder {

        //Card view properties
        ImageView selectedImage;
        CheckBox checkBox;
        TextView progress;

        public SelectedImageHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.img_checkbox);
            selectedImage = itemView.findViewById(R.id.img_gallery);
            progress = itemView.findViewById(R.id.upload_progress);
        }
    }
}

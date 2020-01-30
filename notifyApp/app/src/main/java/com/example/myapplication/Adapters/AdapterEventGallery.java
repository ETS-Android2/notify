package com.example.myapplication.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Listeners.OnRecycleItemClickedListener;
import com.example.myapplication.Models.MyImage;
import com.example.myapplication.myapplication.notify_beta.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;

public class AdapterEventGallery extends RecyclerView.Adapter<AdapterEventGallery.MyImageHolder> {

    private static final String TAG = "AdapterEventGallery: ";

    //Def adapter variables
    private Context mContext;
    private ArrayList<MyImage> imgUriList;
    private OnRecycleItemClickedListener listener;

    //Constructor
    public AdapterEventGallery(Context context, ArrayList<MyImage> uriList, OnRecycleItemClickedListener listener){
        this.mContext = context;
        this.imgUriList = uriList;
        this.listener = listener;
    }

    @NonNull
    @Override   //First creation
    public MyImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.event_card_gallery,parent,false);
        return new MyImageHolder(v);
    }

    @Override   //Binding data to ui
    public void onBindViewHolder(@NonNull final MyImageHolder holder, int position) {
        final MyImage uri = imgUriList.get(position);
        //Get event gallery image and set
        try{
            //Retrieve and set gallery image thumbnails
            Picasso.get().load(uri.getImgUri()).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    holder.image.setImageBitmap(bitmap);
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    Log.e(TAG,"Error: " + e.getMessage());
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });
        }catch (Exception e){
            Log.e(TAG,"Error on image download: " + e.getMessage());
        }
        //Manage checkboxes
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.checkBox.isChecked()){
                    holder.checkBox.setChecked(false);
                    uri.setSelected(false);
                }else{
                    holder.checkBox.setChecked(true);
                    uri.setSelected(true);
                    holder.image.setForeground(mContext.getResources().getDrawable(R.drawable.black_gradient,mContext.getTheme()));
                }
                //Notify adapter owner if on card clicked
                listener.onItemOfListClicked(uri);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //Notify adapter owner if on card long clicked
                listener.onItemLongClicked(uri);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return imgUriList.size();
    }

    public ArrayList<MyImage> getImgUriList() {
        return imgUriList;
    }

    public void setImgUriList(ArrayList<MyImage> imgUriList) {
        this.imgUriList = imgUriList;
    }

    public class MyImageHolder extends RecyclerView.ViewHolder {

        //Card view properties
        private ImageView image;
        private CheckBox checkBox;

        public MyImageHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.img_checkbox);
            image = itemView.findViewById(R.id.img_gallery);
        }

    }
}

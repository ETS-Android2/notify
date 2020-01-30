package com.notify.myapplication.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.notify.myapplication.ImageOperations.PicassoCircleTransformation;
import com.notify.myapplication.Listeners.OnRecycleItemClickedListener;
import com.notify.myapplication.Models.MyUser;
import com.notify.myapplication.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyUserHolder> implements Filterable {

    private static final String TAG = "AdapterUsers Activity: ";

    //Def adapter variables
    Context context;
    List<MyUser> userList;
    List<MyUser> userListFull;
    OnRecycleItemClickedListener listener;

    //Constructor
    public AdapterUsers(Context context, List<MyUser> userList, OnRecycleItemClickedListener listener) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;
        userListFull = new ArrayList<>(this.userList);
    }

    @NonNull
    @Override   //First creation
    public MyUserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //Get(create) instance of a row_user layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_user, parent, false);

        return new MyUserHolder(view);
    }

    @Override   //Binding data to ui (users)
    public void onBindViewHolder(@NonNull final MyUserHolder holder, final int position) {

        final MyUser tempUser = userList.get(position);
        //Get user data to fill the row_user
        String userName = tempUser.getName();
        String userCompany = tempUser.getCompany();
        String userImage = tempUser.getProfileImage();

        //Set data and fill the row_user
        //Set username and company
        holder.mUsernameView.setText(userName);
        holder.mCompanyView.setText(userCompany);

        //Set profile pictures
        if (tempUser.getUserImage() == null) {
            //If user profile picture is not already retrieved
            try {
                Picasso.get().load(userImage).transform(new PicassoCircleTransformation()).
                        into(holder.mUserImageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                BitmapDrawable drawable = (BitmapDrawable) holder.mUserImageView.getDrawable();
                                Bitmap bitmap = drawable.getBitmap();
                                tempUser.setUserImage(bitmap);
                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });

            } catch (Exception e) {

                Log.w(TAG, "Error getting/setting profile image", e);

            }
        }else{
            //If user profile picture is already retrieved
            holder.mUserImageView.setImageBitmap(tempUser.getUserImage());
        }

        //When item is clicked, ...
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemOfListClicked(tempUser);
            }
        });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void setUserList(List<MyUser> userList) {
        this.userList = userList;
    }

    @Override
    public Filter getFilter() {
        return participantFilter;
    }

    private Filter participantFilter = new Filter() {
        //In list search (based on user name)
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<MyUser> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(userList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (MyUser user : userList) {
                    if (user.getName().toLowerCase().startsWith(filterPattern)) {
                        filteredList.add(user);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //Show search results
            userList.clear();
            userList.addAll((List<MyUser>) results.values);
            notifyDataSetChanged();
        }
    };


    //Single user row view holder subclass
    class MyUserHolder extends RecyclerView.ViewHolder {

        TextView mUsernameView;
        TextView mCompanyView;
        ImageView mUserImageView;

        public MyUserHolder(@NonNull View itemView) {
            super(itemView);

            mUsernameView = itemView.findViewById(R.id.user_row_name);
            mCompanyView = itemView.findViewById(R.id.user_row_company);
            mUserImageView = itemView.findViewById(R.id.user_row_img);
        }
    }


}

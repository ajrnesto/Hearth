package com.hearth.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hearth.R;
import com.hearth.methods.MyMethods;
import com.hearth.objects.ShopItem;

import java.util.ArrayList;

public class ProfilePictureAdapter extends RecyclerView.Adapter<ProfilePictureAdapter.profilePictureViewHolder>{

    Context context;
    ArrayList<String> profilePictureArrayList;
    private OnProfilePictureListener mOnProfilePictureListener;

    public ProfilePictureAdapter(Context context, ArrayList<String> profilePictureArrayList, OnProfilePictureListener onProfilePictureListener) {
        this.context = context;
        this.profilePictureArrayList = profilePictureArrayList;
        this.mOnProfilePictureListener = onProfilePictureListener;
    }

    @NonNull
    @Override
    public profilePictureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_profile_pictures, parent, false);
        return new profilePictureViewHolder(view, mOnProfilePictureListener);
    }

    @Override
    public void onBindViewHolder(@NonNull profilePictureViewHolder holder, int position) {
        MyMethods.Generate.image(profilePictureArrayList.get(position), holder.photo);
    }

    @Override
    public int getItemCount() {
        return profilePictureArrayList.size();
    }

    public static class profilePictureViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView photo;
        OnProfilePictureListener onProfilePictureListener;
        public profilePictureViewHolder(@NonNull View itemView, OnProfilePictureListener onProfilePictureListener) {
            super(itemView);
            photo = itemView.findViewById(R.id.imgPicture);
            this.onProfilePictureListener = onProfilePictureListener;

            itemView.setOnClickListener(this); // ("this" refers to the View.OnClickListener context)
        }

        @Override
        public void onClick(View view) {
            onProfilePictureListener.onProfilePictureClick(getAbsoluteAdapterPosition());
        }
    }

    public interface OnProfilePictureListener{
        void onProfilePictureClick(int position);
    }
}

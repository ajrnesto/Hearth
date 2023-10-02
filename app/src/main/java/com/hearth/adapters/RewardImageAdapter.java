package com.hearth.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hearth.methods.MyMethods;
import com.hearth.R;
import com.hearth.objects.RewardImage;

import java.util.ArrayList;

public class RewardImageAdapter extends RecyclerView.Adapter<RewardImageAdapter.rewardImageViewHolder>{

    Context context;
    ArrayList<RewardImage> rewardImageArrayList;
    private OnRewardImageListener mOnRewardImageListener;

    public RewardImageAdapter(Context context, ArrayList<RewardImage> rewardImageArrayList, OnRewardImageListener onRewardImageListener) {
        this.context = context;
        this.rewardImageArrayList = rewardImageArrayList;
        this.mOnRewardImageListener = onRewardImageListener;
    }

    @NonNull
    @Override
    public rewardImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_reward_image, parent, false);
        return new rewardImageViewHolder(view, mOnRewardImageListener);
    }

    @Override
    public void onBindViewHolder(@NonNull rewardImageViewHolder holder, int position) {
        String imageLink = rewardImageArrayList.get(position).getImagelink();
        MyMethods.Generate.image(imageLink, holder.imageView, 560);
    }

    @Override
    public int getItemCount() {
        return rewardImageArrayList.size();
    }

    public static class rewardImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView imageView;
        OnRewardImageListener onRewardImageListener;
        public rewardImageViewHolder(@NonNull View itemView, OnRewardImageListener onRewardImageListener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewRewardImage);
            this.onRewardImageListener = onRewardImageListener;

            itemView.setOnClickListener(this); // ("this" refers to the View.OnClickListener context)
        }

        @Override
        public void onClick(View view) {
            onRewardImageListener.onRewardImageClick(getAbsoluteAdapterPosition());
        }
    }

    public interface OnRewardImageListener{
        void onRewardImageClick(int position);
    }
}

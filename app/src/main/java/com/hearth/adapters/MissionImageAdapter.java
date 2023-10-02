package com.hearth.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hearth.objects.MissionImage;
import com.hearth.methods.MyMethods;
import com.hearth.R;

import java.util.ArrayList;

public class MissionImageAdapter extends RecyclerView.Adapter<MissionImageAdapter.missionImageViewHolder>{

    Context context;
    ArrayList<MissionImage> missionImageArrayList;
    private OnMissionImageListener mOnMissionImageListener;

    public MissionImageAdapter(Context context, ArrayList<MissionImage> missionImageArrayList, OnMissionImageListener onMissionImageListener) {
        this.context = context;
        this.missionImageArrayList = missionImageArrayList;
        this.mOnMissionImageListener = onMissionImageListener;
    }

    @NonNull
    @Override
    public missionImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_mission_image, parent, false);
        return new missionImageViewHolder(view, mOnMissionImageListener);
    }

    @Override
    public void onBindViewHolder(@NonNull missionImageViewHolder holder, int position) {
        String imageLink = missionImageArrayList.get(position).getImagelink();
        MyMethods.Generate.image(imageLink, holder.imageView, 560);
    }

    @Override
    public int getItemCount() {
        return missionImageArrayList.size();
    }

    public static class missionImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView imageView;
        OnMissionImageListener onMissionImageListener;
        public missionImageViewHolder(@NonNull View itemView, OnMissionImageListener onMissionImageListener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewRewardMission);
            this.onMissionImageListener = onMissionImageListener;

            itemView.setOnClickListener(this); // ("this" refers to the View.OnClickListener context)
        }

        @Override
        public void onClick(View view) {
            onMissionImageListener.onMissionImageClick(getAbsoluteAdapterPosition());
        }
    }

    public interface OnMissionImageListener{
        void onMissionImageClick(int position);
    }
}
package com.hearth.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.divider.MaterialDivider;
import com.hearth.methods.MyMethods;
import com.hearth.objects.Leaderboard;
import com.hearth.R;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.leaderboardViewHolder>{

    Context context;
    ArrayList<Leaderboard> leaderboardArrayList;
    private OnLeaderboardListener mOnLeaderboardListener;

    public LeaderboardAdapter(Context context, ArrayList<Leaderboard> leaderboardArrayList, OnLeaderboardListener onLeaderboardListener) {
        this.context = context;
        this.leaderboardArrayList = leaderboardArrayList;
        this.mOnLeaderboardListener = onLeaderboardListener;
    }

    @NonNull
    @Override
    public leaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_leaderboard, parent, false);
        return new leaderboardViewHolder(view, mOnLeaderboardListener);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull leaderboardViewHolder holder, int position) {
        Leaderboard leaderboard = leaderboardArrayList.get(position);
        holder.rank.setText(""+(position+1));

        renderMedalsForTopThree(holder);

        // display family name
        holder.familyname.setText(leaderboard.getFamilyname());

        // display family icon
        String photoUrl = leaderboard.getPhotourl();
        if (TextUtils.isEmpty(photoUrl)){
            photoUrl = "https://4.bp.blogspot.com/-Xd7h725VGgk/U9zsrHl7QxI/AAAAAAAAkeg/c3ecwYFH3Qs/s1600/hoka_05_question.png";
        }
        MyMethods.Generate.image(photoUrl, holder.familyIcon);

        // display score
        Integer score = leaderboard.getScore();
        holder.score.setText(String.valueOf(score));
    }

    private void renderMedalsForTopThree(@NonNull leaderboardViewHolder holder) {
        int pos = holder.getAbsoluteAdapterPosition();
        if (pos == 0){
            holder.rank.setTextColor(Color.parseColor("#FFFFFF"));
            holder.circle.setVisibility(View.VISIBLE);
            holder.circle.setColorFilter(Color.parseColor("#FFD700"));
        }
        else if (pos == 1){
            holder.rank.setTextColor(Color.parseColor("#FFFFFF"));
            holder.circle.setVisibility(View.VISIBLE);
            holder.circle.setColorFilter(Color.parseColor("#C0C0C0"));
        }
        else if (pos == 2){
            holder.rank.setTextColor(Color.parseColor("#FFFFFF"));
            holder.circle.setVisibility(View.VISIBLE);
            holder.circle.setColorFilter(Color.parseColor("#CD7F32"));
        }
    }

    @Override
    public int getItemCount() {
        return leaderboardArrayList.size();
    }

    public static class leaderboardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        MaterialCardView materialCardViewLeaderboard;
        TextView rank, familyname, score;
        ImageView circle;
        RoundedImageView familyIcon;
        MaterialDivider materialDividerLeaderboard;
        OnLeaderboardListener onLeaderboardListener;
        public leaderboardViewHolder(@NonNull View itemView, OnLeaderboardListener onLeaderboardListener) {
            super(itemView);
            rank = itemView.findViewById(R.id.textViewRank);
            familyname = itemView.findViewById(R.id.textViewFamilyName);
            score = itemView.findViewById(R.id.textViewScore);
            circle = itemView.findViewById(R.id.imageViewCircle);
            familyIcon = itemView.findViewById(R.id.imgFamilyIcon);
            materialCardViewLeaderboard = itemView.findViewById(R.id.materialCardViewLeaderboard);
            materialDividerLeaderboard = itemView.findViewById(R.id.materialDividerLeaderboard);
            this.onLeaderboardListener = onLeaderboardListener;

            itemView.setOnClickListener(this); // ("this" refers to the View.OnClickListener context)
        }

        @Override
        public void onClick(View view) {
            onLeaderboardListener.onLeaderboardClick(getAbsoluteAdapterPosition());
        }
    }

    public interface OnLeaderboardListener{
        void onLeaderboardClick(int position);
    }
}

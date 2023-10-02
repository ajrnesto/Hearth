package com.hearth.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hearth.R;
import com.hearth.methods.MyMethods;
import com.hearth.objects.MissionLog;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;

public class MissionLogAdapter extends RecyclerView.Adapter<MissionLogAdapter.missionLogViewHolder>{
    FirebaseDatabase hearthDB = FirebaseDatabase.getInstance();
    DatabaseReference userRef;
    Context context;
    ArrayList<MissionLog> missionLogArrayList;
    private OnMissionLogListener mOnMissionLogListener;

    public MissionLogAdapter(Context context, ArrayList<MissionLog> missionLogArrayList, OnMissionLogListener onMissionLogListener) {
        this.context = context;
        this.missionLogArrayList = missionLogArrayList;
        this.mOnMissionLogListener = onMissionLogListener;
    }

    @NonNull
    @Override
    public missionLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_mission_logs, parent, false);
        return new missionLogViewHolder(view, mOnMissionLogListener);
    }

    @Override
    public void onBindViewHolder(@NonNull missionLogViewHolder holder, int position) {
        MissionLog missionLog = missionLogArrayList.get(position);

        holder.tvTimestamp.setText(MyMethods.Time.toDate(context, missionLog.getTimestamp()) + " at " + MyMethods.Time.toClock(missionLog.getTimestamp()));
        loadUserData(missionLog, holder.imgProfilePicture, holder.tvUserName);
        holder.tvMissionTitle.setText(missionLog.getMissionTitle());
    }

    private void loadUserData(MissionLog missionLog, RoundedImageView imgProfilePicture, TextView tvUserName) {
        String userUid = missionLog.getMemberUid();

        userRef = hearthDB.getReference("user_"+userUid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String firstname = snapshot.child("firstname").getValue().toString();
                String familyname = snapshot.child("familyname").getValue().toString();
                String fullname = firstname + " " + familyname;
                String photoUrl = snapshot.child("photourl").getValue().toString();

                MyMethods.Generate.image(photoUrl, imgProfilePicture);
                tvUserName.setText(fullname + " finished their mission.");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return missionLogArrayList.size();
    }

    public static class missionLogViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tvUserName, tvMissionTitle, tvTimestamp;
        RoundedImageView imgProfilePicture;
        OnMissionLogListener onMissionLogListener;
        public missionLogViewHolder(@NonNull View itemView, OnMissionLogListener onMissionLogListener) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvMissionTitle = itemView.findViewById(R.id.tvMissionTitle);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            imgProfilePicture = itemView.findViewById(R.id.imgProfilePicture);
            this.onMissionLogListener = onMissionLogListener;

            itemView.setOnClickListener(this); // ("this" refers to the View.OnClickListener context)
        }

        @Override
        public void onClick(View view) {
            onMissionLogListener.onMissionLogClick(getAbsoluteAdapterPosition());
        }
    }

    public interface OnMissionLogListener{
        void onMissionLogClick(int position);
    }
}

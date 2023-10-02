package com.hearth.adapters;

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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.hearth.methods.MyMethods;
import com.hearth.R;
import com.hearth.objects.Mission;

import java.util.ArrayList;
import java.util.Calendar;

public class MissionAdapter extends RecyclerView.Adapter<MissionAdapter.missionViewHolder>{

    Context context;
    ArrayList<Mission> missionArrayList;
    private MissionAdapter.OnMissionListener mOnMissionListener;
    ArrayList<String> missionsCompletedArrayList;

    int completedMissionsVisibility;
    String currentUserId;
    String ALARM_MILITARY_DEFAULT_VALUE = "2401";

    public MissionAdapter(Context context, ArrayList<Mission> missionArrayList, MissionAdapter.OnMissionListener onMissionListener, ArrayList<String> missionsCompletedArrayList) {
        this.context = context;
        this.missionArrayList = missionArrayList;
        this.mOnMissionListener = onMissionListener;
        this.missionsCompletedArrayList = missionsCompletedArrayList;
    }

    @NonNull
    @Override
    public MissionAdapter.missionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_mission, parent, false);
        return new missionViewHolder(view, mOnMissionListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MissionAdapter.missionViewHolder holder, int position) {
        Mission mission = missionArrayList.get(position);
        currentUserId = MyMethods.Cache.getString(context, "uid");
        completedMissionsVisibility = MyMethods.Cache.getInt(context, currentUserId+"show_completed_missions_state");

        // load reward
        holder.reward.setText(MyMethods.DoubleMethods.formatDoubleToString(mission.getReward()));

        loadDescription(holder, mission);
        loadImage(holder, mission);
        loadAlarmText(holder, mission);
        checkIfMissionIsDone(holder, mission);
    }

    private void loadImage(missionViewHolder holder, Mission mission) {
        String imageLink = mission.getImagelink();
        if (TextUtils.isEmpty(imageLink)){
            holder.missionImage.setVisibility(View.GONE);
        }
        else {
            holder.missionImage.setVisibility(View.VISIBLE);
            MyMethods.Generate.image(imageLink, holder.missionImage);
        }
    }

    private void loadAlarmText(missionViewHolder holder, Mission mission) {
        long alarm = mission.getAlarm();
        if (mission.getAlarmmilitary() == null){
            return;
        }

        String alarmMilitary = mission.getAlarmmilitary();

        if (alarmMilitary.equals(ALARM_MILITARY_DEFAULT_VALUE)){
            holder.alarm.setVisibility(View.GONE);
        }
        else {
            holder.alarm.setVisibility(View.VISIBLE);
            holder.alarm.setText(MyMethods.Time.toClock(alarm));
        }
    }

    private void loadDescription(MissionAdapter.missionViewHolder holder, Mission mission) {
        if (mission.getDescription()==null){
            holder.description.setVisibility(View.GONE);
            return;
        }

        String desc = mission.getDescription();

        holder.description.setText(desc);
        if (!(TextUtils.isEmpty(desc)) || !(desc.equals(""))){
            holder.description.setVisibility(View.VISIBLE);
        }
    }

    private void checkIfMissionIsDone(missionViewHolder holder, Mission mission) {
        // check if mission is done
        String missionUid = mission.getUid();
        if (missionsCompletedArrayList.contains(missionUid)){
            // check hide completed missions state
            if (completedMissionsVisibility == 1){
                // default item visibility
                holder.itemView.setVisibility(View.VISIBLE);
                RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(26, 10,26,10);
                holder.itemView.setLayoutParams(layoutParams);

                holder.cvMission.setEnabled(false);
                holder.cvMission.setForeground(new ColorDrawable(Color.parseColor("#77E8E8E8")));
                // migrate title text to description
                holder.description.setVisibility(View.VISIBLE);
                holder.description.setText(mission.getTitle());
                // change title text to Completed!
                holder.title.setText("Completed mission!");
                holder.cvMission.setCardElevation(0);
            }
            else if (completedMissionsVisibility == 0) {
                holder.itemView.setVisibility(View.GONE);
                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            }
        }
        else {
            holder.cvMission.setEnabled(true);
            // default mission title and description
            holder.title.setText(mission.getTitle());
            loadDescription(holder, mission);
            holder.cvMission.setCardElevation(2);

            // highlight red if system time has passed the alarm time
            Calendar calendarMission = Calendar.getInstance();
            Calendar calendarSystem = Calendar.getInstance();
            calendarMission.setTimeInMillis(mission.getAlarm());
            calendarSystem.setTimeInMillis(System.currentTimeMillis());
            long hourMission = calendarMission.get(Calendar.HOUR_OF_DAY);
            long minuteMission = calendarMission.get(Calendar.MINUTE);
            long hourSystem = calendarSystem.get(Calendar.HOUR_OF_DAY);
            long minuteSystem = calendarSystem.get(Calendar.MINUTE);

            if ((mission.getAlarmmilitary() != ALARM_MILITARY_DEFAULT_VALUE) && (hourMission <= (hourSystem))){
                if (hourMission < hourSystem) {
                    holder.cvMission.setForeground(new ColorDrawable(Color.parseColor("#22ff0000")));
                }
                else if (hourMission == hourSystem) {
                    if (minuteMission <= (minuteSystem)){
                        holder.cvMission.setForeground(new ColorDrawable(Color.parseColor("#22ff0000")));
                    }
                }
            }
            else {
                holder.cvMission.setForeground(new ColorDrawable(Color.parseColor("#00fafafa")));
            }
        }
    }

    @Override
    public int getItemCount() {
        return missionArrayList.size();
    }

    public static class missionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView title, description, reward, alarm;
        ImageView missionImage;
        MaterialCardView cvMission;
        ConstraintLayout clMission;

        MissionAdapter.OnMissionListener onMissionListener;
        public missionViewHolder(@NonNull View itemView, MissionAdapter.OnMissionListener onMissionListener) {
            super(itemView);
            cvMission = itemView.findViewById(R.id.cvMission);
            clMission = itemView.findViewById(R.id.clMission);
            title = itemView.findViewById(R.id.textViewMissionTitle);
            description = itemView.findViewById(R.id.textViewMissionDescription);
            reward = itemView.findViewById(R.id.textViewMissionReward);
            missionImage = itemView.findViewById(R.id.imageViewMissionImage);
            alarm = itemView.findViewById(R.id.textViewMissionAlarm);
            this.onMissionListener = onMissionListener;

            itemView.setOnClickListener(this); // ("this" refers to the View.OnClickListener context)
        }

        @Override
        public void onClick(View view) {
            onMissionListener.onMissionClick(getAbsoluteAdapterPosition());
        }
    }

    public interface OnMissionListener{
        void onMissionClick(int position);
    }
}

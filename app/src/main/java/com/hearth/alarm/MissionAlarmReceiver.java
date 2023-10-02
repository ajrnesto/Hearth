package com.hearth.alarm;

import static android.media.AudioManager.STREAM_ALARM;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.hearth.AlarmActivity;
import com.hearth.R;
import com.hearth.activities.PomodoroActivity;
import com.hearth.authentication.LoginActivity;

public class MissionAlarmReceiver extends android.content.BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String missionTitle = intent.getExtras().getString("mission_title");
        String missionDescription = intent.getExtras().getString("mission_description");
        double missionReward = intent.getExtras().getDouble("mission_reward");
        String missionUid = intent.getExtras().getString("mission_uid");
        String missionImagelink = intent.getExtras().getString("mission_imagelink");

        Intent i = new Intent(context, PomodoroActivity.class);

        i.putExtra("mission_title", missionTitle);
        i.putExtra("mission_description", missionDescription);
        i.putExtra("mission_reward", missionReward);
        i.putExtra("mission_uid", missionUid);
        i.putExtra("mission_imagelink", missionImagelink);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "hearth_notifications")
                .setSmallIcon(R.drawable.ic_baseline_alarm_24)
                .setColor(Color.parseColor("#e17a5f"))
                .setContentTitle("Mission: "+missionTitle)
                .setContentText("Tap here to do this mission now.")
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(1, notificationBuilder.build());
    }
}

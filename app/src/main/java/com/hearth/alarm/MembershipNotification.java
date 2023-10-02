package com.hearth.alarm;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.FirebaseDatabase;
import com.hearth.R;
import com.hearth.activities.PomodoroActivity;
import com.hearth.authentication.LoginActivity;
import com.hearth.methods.MyMethods;

import java.util.Set;

public class MembershipNotification extends android.content.BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        int responseCode = intent.getIntExtra("response_code", 0);
        String familyName = intent.getStringExtra("family_name");

        Intent i = new Intent(context, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        // check response code ( 0 = join request denied, 1 = join request accepted, 2 = kicked from family)
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "hearth_order_notifications")
                .setSmallIcon(R.drawable.ic_family)
                .setColor(Color.parseColor("#e07a5f"))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        switch (responseCode) {
            case 0: notificationBuilder.setContentTitle("Join request denied");
                notificationBuilder.setContentText("We're sorry to inform that your request to join the "+familyName+" family has been denied.");
                break;
            case 1: notificationBuilder.setContentTitle("Join Request Accepted");
                notificationBuilder.setContentText("Your request to join the "+familyName+" family has been accepted!\nTap on Proceed to start your Hearth journey!");
                break;
            case 2: notificationBuilder.setContentTitle("You were kicked out from the family");
                notificationBuilder.setContentText("We're sorry to inform that you were kicked out from the family.\nAll gold, XP, missions, and pending orders will be lost.");
                break;
        }

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(0, notificationBuilder.build());
    }
}

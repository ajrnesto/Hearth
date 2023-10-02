package com.hearth.alarm;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.hearth.R;
import com.hearth.activities.ChatActivity;
import com.hearth.activities.OrdersActivity;
import com.hearth.methods.MyMethods;

public class ChatNotifications extends android.content.BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        String chatAuthor = intent.getExtras().getString("chat_author");
        String chatAuthorPhotoUrl = intent.getExtras().getString("chat_authorPhotoUrl");
        String chatMessage = intent.getExtras().getString("chat_message");

        Intent i = new Intent(context, ChatActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "hearth_chat_notifications")
                .setSmallIcon(R.drawable.baseline_chat_24)
                .setColor(Color.parseColor("#3E415B"))
                .setContentTitle(chatAuthor)
                .setContentText(chatMessage)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle());

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(0, notificationBuilder.build());
    }
}

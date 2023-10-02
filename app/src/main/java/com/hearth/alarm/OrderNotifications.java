package com.hearth.alarm;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.hearth.R;
import com.hearth.activities.OrdersActivity;
import com.hearth.activities.PomodoroActivity;
import com.hearth.methods.MyMethods;

public class OrderNotifications extends android.content.BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        String orderBuyerName = intent.getStringExtra("order_buyername");
        String orderItem = intent.getStringExtra("order_item");
        int orderId = MyMethods.Cache.getInt(context, "orderId");

        Intent i = new Intent(context, OrdersActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        if (orderBuyerName.equals(MyMethods.Cache.getString(context, "firstname") + " " + MyMethods.Cache.getString(context, "familyname"))) {
            return;
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "hearth_order_notifications")
                .setSmallIcon(R.drawable.ic__shopping_cart)
                .setColor(Color.parseColor("#FFD700"))
                .setContentTitle(orderBuyerName+" has placed an order for "+orderItem)
                .setContentText("Tap here to check all pending orders.")
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        if (orderBuyerName == null) {
            notificationBuilder.setContentTitle("A family member placed a new buy order");
        }

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(orderId++, notificationBuilder.build());
        MyMethods.Cache.setInt(context, "orderId", orderId);
    }
}

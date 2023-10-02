package com.hearth.alarm;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.FirebaseDatabase;
import com.hearth.R;
import com.hearth.activities.OrdersActivity;
import com.hearth.methods.MyMethods;

import java.util.Set;

public class OrderCompletedNotifications extends android.content.BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        String adminName = intent.getStringExtra("order_adminname");
        String itemName = intent.getStringExtra("order_itemName");
        String orderUid = intent.getStringExtra("order_uid");
        int orderCompletedId = MyMethods.Cache.getInt(context, "orderId");

        if (adminName == null || itemName == null) {
            return;
        }

        // check if order was already notified
        Set<String> setNotifiedCompletedOrders = MyMethods.Cache.getStringSet(context, "notified_completed_orders");
        if (setNotifiedCompletedOrders.contains(orderUid)) {
            return;
        }
        setNotifiedCompletedOrders.add(orderUid);
        MyMethods.Cache.setStringSet(context, "notified_completed_orders", setNotifiedCompletedOrders);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "hearth_order_notifications")
                .setSmallIcon(R.drawable.ic__shopping_cart)
                .setColor(Color.parseColor("#FFD700"))
                .setContentTitle("Order completed")
                .setContentText("Your order for "+itemName+" has been marked as completed by "+adminName)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle());

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(orderCompletedId++, notificationBuilder.build());
        MyMethods.Cache.setInt(context, "orderId", orderCompletedId);

        FirebaseDatabase.getInstance().getReference("user_"+MyMethods.Cache.getString(context, "uid")+"_completed_orders").child(orderUid).removeValue();
    }
}

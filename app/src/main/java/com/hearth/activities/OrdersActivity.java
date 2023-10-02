package com.hearth.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hearth.R;
import com.hearth.adapters.OrderAdapter;
import com.hearth.methods.MyMethods;
import com.hearth.objects.CompletedMission;
import com.hearth.objects.Order;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class OrdersActivity extends AppCompatActivity implements OrderAdapter.OnOrderListener{

    FirebaseDatabase hearthDB = FirebaseDatabase.getInstance();
    DatabaseReference ordersReference, orderRef, completedOrderRef;
    ValueEventListener ordersListener, orderListener, completedOrderListener;

    String familycode, currentUserUid;

    OrderAdapter orderAdapter;
    ArrayList<Order> orderArrayList;
    RecyclerView recyclerViewOrderList;
    OrderAdapter.OnOrderListener onOrderListener = this;
    Context context;
    MaterialButton materialButtonClose;

    ArrayList<String> arrayCompletedMissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        initializeVars();
        closeButtonHandler();
        ordersRecyclerviewHandler();
        notificationListener();
    }

    private void initializeVars() {
        context = this;
        familycode = MyMethods.Cache.getString(getApplicationContext(), "familycode");
        currentUserUid = MyMethods.Cache.getString(getApplicationContext(), "uid");
        materialButtonClose = findViewById(R.id.materialButtonClose);
    }

    private void closeButtonHandler() {
        materialButtonClose.setOnClickListener(view -> {
            finish();
        });
    }

    private void ordersRecyclerviewHandler() {
        //set up orders recyclerview and array list
        recyclerViewOrderList = findViewById(R.id.recyclerViewOrders);
        recyclerViewOrderList.setHasFixedSize(true);
        recyclerViewOrderList.setLayoutManager(new LinearLayoutManager(context));

        orderArrayList = new ArrayList<>();
        orderAdapter = new OrderAdapter(context, orderArrayList, onOrderListener);
        recyclerViewOrderList.setAdapter(orderAdapter);

        // finally, display all orders
        ordersReference = hearthDB.getReference("family_"+familycode+"_orders");

        ordersListener = ordersReference.addValueEventListener(new ValueEventListener() { // use retrieved familycode value to reference orders
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderArrayList.clear();

                long counter = 0;
                long ordersCount = snapshot.getChildrenCount();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Order order = dataSnapshot.getValue(Order.class);
                    orderArrayList.add(order);
                    counter++;
                }

                if (counter == ordersCount) {
                    for (int i=0; i<orderArrayList.size(); i++) {
                        if ((orderArrayList.get(i).getBuyername() == null) || (orderArrayList.get(i).getItemname() == null)) {
                            continue;
                        }
                        String name = orderArrayList.get(i).getBuyername();
                        if (name.equals(MyMethods.Cache.getString(getApplicationContext(), "firstname") + " " + MyMethods.Cache.getString(getApplicationContext(), "familyname"))) {
                            continue;
                        }
                        MyMethods.Notifications.orders(OrdersActivity.this, orderArrayList.get(i).getUid(), orderArrayList.get(i).getBuyername(), orderArrayList.get(i).getItemname(), orderArrayList.get(i).getDate());
                    }
                }
                orderAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public void onOrderClick(int position, int mode) {
        String orderUid = orderArrayList.get(position).uid;

        orderRef = hearthDB.getReference("family_"+familycode+"_orders").child(orderUid);
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String buyerUid = snapshot.child("buyeruid").getValue().toString();
                String itemName = snapshot.child("itemname").getValue().toString();
                // String buyerName = snapshot.child("buyername").getValue().toString();
                // String imageLink = snapshot.child("imagelink").getValue().toString();
                // long date = Long.parseLong(snapshot.child("date").getValue().toString());

                completedOrderRef = hearthDB.getReference("user_"+buyerUid+"_completed_orders").push();
                String completedOrderKey = completedOrderRef.getKey();
                completedOrderRef.child("uid").setValue(completedOrderKey);
                completedOrderRef.child("adminname").setValue(MyMethods.Cache.getString(getApplicationContext(), "firstname") + " " +MyMethods.Cache.getString(getApplicationContext(), "familyname"));
                completedOrderRef.child("itemname").setValue(itemName);

                orderRef.removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void notificationListener() {
        completedOrderRef = hearthDB.getReference("user_"+currentUserUid+"_completed_orders");
        completedOrderListener = completedOrderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    CompletedMission completedMission = dataSnapshot.getValue(CompletedMission.class);

                    assert completedMission != null;
                    String adminName = completedMission.getAdminname();
                    String itemName = completedMission.getItemname();
                    String uid = completedMission.getUid();

                    // push notification
                    MyMethods.Notifications.orderCompleted(context, adminName, itemName, uid);
                    // hearthDB.getReference("user_"+MyMethods.Cache.getString(context, "uid")+"_completed_orders").child(uid).removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.zoom_out_enter, R.anim.zoom_out_exit);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ordersReference.removeEventListener(ordersListener);
        completedOrderRef.removeEventListener(completedOrderListener);
    }
}
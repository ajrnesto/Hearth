package com.hearth.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.hearth.R;
import com.hearth.adapters.ShopItemAdapter;
import com.hearth.authentication.LoginActivity;
import com.hearth.methods.MyMethods;
import com.hearth.objects.CompletedMission;
import com.hearth.objects.ShopItem;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;

public class ShopActivity extends AppCompatActivity implements ShopItemAdapter.OnShopItemListener {

    FirebaseDatabase hearthDB = FirebaseDatabase.getInstance();
    String currentUserID = FirebaseAuth.getInstance().getUid();
    DatabaseReference shopRef, tempShopRef, orderRef, lastMessageRef, completedOrderRef;
    ValueEventListener lastMessageListener, completedOrderListener;

    TextView tvActivityTitle, tvGold;
    RoundedImageView ivShop;
    BottomNavigationView botnavShop;
    ImageView ivMenu, ivGold;
    ExtendedFloatingActionButton efabOrders, efabAddItem;
    BadgeDrawable badgeOrders;

    RecyclerView rvShop;
    ShopItemAdapter adapterShop;
    ArrayList<ShopItem> arrayShop;
    ShopItemAdapter.OnShopItemListener onShopItemListener = this;
    Context context;

    String familyCode;
    double goldBalance;
    String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        // Start Declare android widgets and views
        ivShop = findViewById(R.id.imageViewShopActivity);
        MyMethods.Generate.banner(MyMethods.Cache.getString(getApplicationContext(), "shop_banner_link"), ivShop);

        context = this;
        familyCode = MyMethods.Cache.getString(getApplicationContext(), "familycode");
        role = MyMethods.Cache.getString(getApplicationContext(), "role");
        tvActivityTitle = findViewById(R.id.textViewActivityTitle);
        botnavShop = findViewById(R.id.bottomNavigationMissions);
        ivMenu = findViewById(R.id.imageViewButton);
        efabAddItem = findViewById(R.id.extendedFloatingActionButtonAddItem);
        efabOrders = findViewById(R.id.extendedFloatingActionButtonOrders);
        badgeOrders = BadgeDrawable.create(ShopActivity.this);
        ivGold = findViewById(R.id.imageViewGoldIcon);
        tvGold = findViewById(R.id.textViewGoldBalance);
        rvShop = findViewById(R.id.recyclerViewShopItemsList);
        // End Declare android widgets and views

        // Start of top action bar and bottom nav bar initialization
        ivGold.setVisibility(View.VISIBLE);
        tvGold.setVisibility(View.VISIBLE);
        tvActivityTitle.setText("Shop");
        updateGoldBalanceDisplay();
        botnavShop.setSelectedItemId(R.id.shop);
        botnavShop.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.shop:
                    return true;
                case R.id.missions:
                    startActivity(new Intent(getApplicationContext(), MissionsActivity.class));
                    overridePendingTransition(R.anim.no_animation,R.anim.no_animation);
                    finish();
                    return true;
                case R.id.family:
                    startActivity(new Intent(getApplicationContext(), FamilyActivity.class));
                    overridePendingTransition(R.anim.no_animation,R.anim.no_animation);
                    finish();
                    return true;
            }
            return false;
        });
        generateOrderBadge();
        // End of top action bar and bottom nav bar initialization

        // Start - actionbar menu button handler
        ivMenu.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), MenuActivity.class));
            overridePendingTransition(R.anim.slide_in_left,R.anim.no_animation);
        });
        // End - actionbar menu button handler

        // start - check user role -> if admin, show and handle buttons, else hide buttons
        if (role.equals("Admin")){
            efabOrders.setVisibility(View.VISIBLE);

            // start - orders efab click listener
            efabOrders.setOnClickListener(view -> {
                Intent intentOrders = new Intent(ShopActivity.this, OrdersActivity.class);
                startActivity(intentOrders);
                overridePendingTransition(R.anim.zoom_in_enter, R.anim.zoom_in_exit);
            });
            // end - orders efab click listener

            // start - add new item efab click listener
            efabAddItem.setOnClickListener(view -> {
                // get temporary shop item reference and get firebase-generated uid
                tempShopRef = hearthDB.getReference("family_"+familyCode+"_items_temp").push();
                String itemUid = tempShopRef.getKey();
                tempShopRef.child("uid").setValue(itemUid);

                // start manage item activity and push item uid
                Intent intentAddItem = new Intent(getApplicationContext(), ManageShopActivity.class);
                intentAddItem.putExtra("manage_mode", 0);
                intentAddItem.putExtra("item_uid", itemUid);
                startActivity(intentAddItem);
                overridePendingTransition(R.anim.zoom_in_enter, R.anim.zoom_in_exit);
            });
            // end - add new item efab click listener
        }
        else{
            efabOrders.setVisibility(View.GONE);
            efabAddItem.setVisibility(View.GONE);
        }
        // end - check user role

        // start - display shop items using family
        rvShop = findViewById(R.id.recyclerViewShopItemsList);
        rvShop.setHasFixedSize(true);
        rvShop.setLayoutManager(new LinearLayoutManager(context));

        arrayShop = new ArrayList<>();
        adapterShop = new ShopItemAdapter(context, arrayShop, onShopItemListener);
        rvShop.setAdapter(adapterShop);

        shopRef = hearthDB.getReference("family_"+familyCode+"_items");
        shopRef.addValueEventListener(new ValueEventListener() { // use retrieved familycode value to reference family shop items
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayShop.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    ShopItem shopItem = dataSnapshot.getValue(ShopItem.class);
                    arrayShop.add(shopItem);
                }
                adapterShop.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        if (role.equals("Admin")){
            rvShop.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (dy > 0){
                        efabAddItem.shrink();
                        efabAddItem.hide();
                        efabOrders.hide();
                    }
                    else {
                        efabAddItem.show();
                        efabOrders.show();
                        efabAddItem.extend();
                    }
                }
            });
        }
        // end - display shop items using family code

        onKickedListener();
        chatListener();
        completedOrdersListener();
    }

    private void completedOrdersListener() {
        completedOrderRef = hearthDB.getReference("user_"+currentUserID+"_completed_orders");
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

    private void generateOrderBadge() {
        hearthDB.getReference("family_"+familyCode+"_orders").addValueEventListener(new ValueEventListener() {
            @SuppressLint("UnsafeOptInUsageError")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long numberOfOrders = snapshot.getChildrenCount();

                if (numberOfOrders > 0){
                    badgeOrders.setNumber((int) numberOfOrders);
                    badgeOrders.setVisible(true);
                    BadgeUtils.attachBadgeDrawable(badgeOrders, efabOrders);
                }
                else{
                    badgeOrders.setVisible(false);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNavigationMissions = findViewById(R.id.bottomNavigationMissions);
        bottomNavigationMissions.setSelectedItemId(R.id.shop);
    }

    @Override
    public void onShopItemClick(int position) {
        // get item selected details
        String itemName = arrayShop.get(position).getName();
        double itemPrice = arrayShop.get(position).getPrice();
        String itemUid = arrayShop.get(position).getUid();
        String imageLink = arrayShop.get(position).getImagelink();
        //TextView textViewGoldBalance = findViewById(R.id.textViewGoldBalance);

        // switch user role
        switch (role){
            case "Admin": {
                // display dialog, admin can edit the item, members can only purchase item
                MaterialAlertDialogBuilder alertAdmin = new MaterialAlertDialogBuilder(ShopActivity.this);
                alertAdmin.setTitle("Edit or Buy "+itemName);
                alertAdmin.setMessage("As a family Admin, you may Edit "+itemName+" item details or Purchase it for "+MyMethods.DoubleMethods.formatDoubleToString(itemPrice)+" gold coins.");
                alertAdmin.setPositiveButton("Buy", (dialogInterface, i) -> {
                    // get user's gold balance
                    purchaseItem(itemName, itemPrice, imageLink);
                });
                alertAdmin.setNegativeButton("Edit", (dialogInterface, i) -> {
                    Intent intentEditItemActivity = new Intent(getApplicationContext(), ManageShopActivity.class);
                    intentEditItemActivity.putExtra("manage_mode", 1);
                    MyMethods.Cache.getInt(getApplicationContext(), itemUid+"has_image");
                    intentEditItemActivity.putExtra("item_name", itemName);
                    intentEditItemActivity.putExtra("item_price", MyMethods.DoubleMethods.formatDoubleToString(itemPrice));
                    intentEditItemActivity.putExtra("item_uid", itemUid);
                    startActivity(intentEditItemActivity);
                    overridePendingTransition(R.anim.zoom_in_enter, R.anim.zoom_in_exit);
                });
                alertAdmin.setNeutralButton("Cancel", (dialogInterface, i) -> {
                });
                alertAdmin.show();
                break;
            }
            case "Member":
                MaterialAlertDialogBuilder alertMember = new MaterialAlertDialogBuilder(ShopActivity.this);
                alertMember.setTitle("Confirm purchase");
                alertMember.setMessage("Would you like to buy "+itemName+" for "+MyMethods.DoubleMethods.formatDoubleToString(itemPrice)+" gold?");
                alertMember.setPositiveButton("Buy", (dialogInterface, i) -> {
                    purchaseItem(itemName, itemPrice, imageLink);
                });
                alertMember.setNeutralButton("Cancel", (dialogInterface, i) -> {
                });
                alertMember.show();
                break;
        }
    }

    public void purchaseItem(String itemName, double itemPrice, String imageLink){
        //TextView textViewGoldBalance = findViewById(R.id.textViewGoldBalance);
        goldBalance = MyMethods.Cache.getDouble(getApplicationContext(), "gold");

        // subtract gold balance by the item price
        if ((goldBalance-itemPrice) < 0){
            // if balance drops below 0, cancel purchase
            MyMethods.Generate.warningDialog(ShopActivity.this, "Insufficient Gold", "You don't have enough gold to buy "+itemName);
        }
        else{
            // if balance is sufficient, then subtract price from gold balance
            String strNewGoldBalance = MyMethods.DoubleMethods.formatDoubleToString(goldBalance-itemPrice);
            double newGoldBalance = Double.parseDouble(strNewGoldBalance);
            hearthDB.getReference("user_"+MyMethods.Cache.getString(getApplicationContext(), "uid")+"_gold").setValue(newGoldBalance);
            MyMethods.Cache.setDouble(getApplicationContext(), "gold", newGoldBalance);
            updateGoldBalanceDisplay();
            Toast.makeText(getApplicationContext(), "Bought item for "+itemPrice+" gold coins", Toast.LENGTH_SHORT).show();

            // then send an order
            orderRef = hearthDB.getReference("family_"+familyCode+"_orders").push();
            // get order id
            String orderUid = orderRef.getKey();
            orderRef.child("uid").setValue(orderUid);

            // full name
            String firstname = MyMethods.Cache.getString(getApplicationContext(), "firstname");
            String familyname = MyMethods.Cache.getString(getApplicationContext(), "familyname");
            orderRef.child("buyername").setValue(firstname + " " + familyname);
            orderRef.child("buyeruid").setValue(currentUserID);
            // date
            orderRef.child("date").setValue(ServerValue.TIMESTAMP);
            // item image
            orderRef.child("imagelink").setValue(imageLink);
            // item name
            orderRef.child("itemname").setValue(itemName);
        }
    }

    public void updateGoldBalanceDisplay(){
        double goldbalance = MyMethods.Cache.getDouble(getApplicationContext(), "gold");
        tvGold.setText(MyMethods.DoubleMethods.formatDoubleToString(goldbalance));
    }

    private void onKickedListener() {
        hearthDB.getReference("user_"+currentUserID).child("familycode").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    MyMethods.Notifications.membership(ShopActivity.this, 2, "");
                    MaterialAlertDialogBuilder kickedDialog = new MaterialAlertDialogBuilder(ShopActivity.this);
                    kickedDialog.setTitle("You were kicked out from this family");
                    kickedDialog.setMessage("We're sorry to inform that you were kicked out from this family.\nAll gold, XP, missions, and pending orders will be lost.");
                    kickedDialog.setPositiveButton("Back to login", (dialogInterface, i) -> {
                        Intent loginIntent = new Intent(ShopActivity.this, LoginActivity.class);
                        startActivity(loginIntent);
                        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        finish();
                    });
                    kickedDialog.setOnCancelListener(dialogInterface -> {
                        Intent loginIntent = new Intent(ShopActivity.this, LoginActivity.class);
                        startActivity(loginIntent);
                        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        finish();
                    });
                    if (!ShopActivity.this.isFinishing()) {
                        kickedDialog.show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void chatListener() {
        lastMessageRef = hearthDB.getReference("family_"+familyCode+"_chat_lastmessage");
        lastMessageListener = lastMessageRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    return;
                }

                if (snapshot.child("authorUid").getValue().toString() == null) {
                    return;
                }

                String authorUid = snapshot.child("authorUid").getValue().toString();
                String message = snapshot.child("message").getValue().toString();
                long timestamp = Long.parseLong(snapshot.child("timestamp").getValue().toString());

                String currentUserId = MyMethods.Cache.getString(getApplicationContext(), "uid");
                if (authorUid.equals(currentUserId)){
                    return;
                }

                hearthDB.getReference("user_"+authorUid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String photoUrl = snapshot.child("photourl").getValue().toString();
                        String firstName = snapshot.child("firstname").getValue().toString();
                        String familyName = snapshot.child("familyname").getValue().toString();
                        String fullName = firstName + " " + familyName;

                        MyMethods.Notifications.chat(ShopActivity.this, photoUrl, fullName, message, timestamp);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        lastMessageRef.removeEventListener(lastMessageListener);
        completedOrderRef.removeEventListener(completedOrderListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
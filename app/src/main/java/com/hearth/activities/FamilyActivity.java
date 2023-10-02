package com.hearth.activities;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hearth.R;
import com.hearth.adapters.FamilyMemberAdapter;
import com.hearth.adapters.JoinRequestAdapter;
import com.hearth.adapters.ShopItemAdapter;
import com.hearth.authentication.LoginActivity;
import com.hearth.methods.MyMethods;
import com.hearth.objects.CompletedMission;
import com.hearth.objects.FamilyMember;
import com.hearth.objects.JoinRequest;
import com.hearth.objects.Order;
import com.hearth.objects.ShopItem;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;

public class FamilyActivity extends AppCompatActivity implements FamilyMemberAdapter.OnFamilyMemberListener, JoinRequestAdapter.OnJoinRequestListener{

    private FirebaseDatabase hearthDB = FirebaseDatabase.getInstance();
    String currentUserID;
    private DatabaseReference currentUserRef, newMemberRef, currentFamilyRef, roleRef, ordersReference, userFamilyCodeRef, lastMessageRef, completedOrderRef;
    private ValueEventListener roleListener, ordersListener, userFamilyCodeListener, memberListener, lastMessageListener, completedOrderListener;
    private BottomNavigationView botnavFamily;

    RecyclerView rvFamilyMember, rvJoinRequests;
    ArrayList<FamilyMember> arrayFamilyMember;
    ArrayList<JoinRequest> arrayJoinRequests;
    ArrayList<Order> orderArrayList;
    DatabaseReference memberRef, familyScoreRef;
    DatabaseReference memberRoleReference;
    FamilyMemberAdapter adapterFamilyMember;
    JoinRequestAdapter joinRequestAdapter;
    FamilyMemberAdapter.OnFamilyMemberListener onFamilyMemberListener = this;
    JoinRequestAdapter.OnJoinRequestListener onJoinRequestListener = this;

    TextView tvActivityTitle, tvFamilyName, tvCode, tvScore, tvJoinRequests;
    ImageView imgMenu, imageViewButtonMore;
    BadgeDrawable badgeOrders;
    MaterialButton btnMembers, btnLeaderboards, btnChat, btnEditDetails, btnAdminToggle, btnJoinRequests, btnToggleCodeVisibility, btnCopyCode;
    RoundedImageView imgFamilyIcon;

    String familyName;
    String familyCode;
    String currentUserRole;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family);

        initializeVars();
        initializeTopAndBottomBars();

        btnToggleCodeVisibility.setOnClickListener(view -> {
            if (tvCode.getVisibility() == View.GONE) {
                tvCode.setVisibility(View.VISIBLE);
                btnToggleCodeVisibility.setText("Hide");
                btnToggleCodeVisibility.setIconResource(R.drawable.baseline_visibility_off_24);
                btnCopyCode.setVisibility(View.VISIBLE);
                btnCopyCode.setOnClickListener(view1 -> {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("copy code", tvCode.getText().toString());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(context, "Copied family code to clipboard", Toast.LENGTH_SHORT).show();
                });
            }
            else {
                tvCode.setVisibility(View.GONE);
                btnToggleCodeVisibility.setText("Show Family Code");
                btnToggleCodeVisibility.setIconResource(R.drawable.ic_eye);
                btnCopyCode.setVisibility(View.GONE);
            }
        });

        btnEditDetails.setOnClickListener(view -> {
            startActivity(new Intent(FamilyActivity.this, EditFamilyDetailsActivity.class));
            overridePendingTransition(R.anim.zoom_in_enter, R.anim.zoom_in_exit);
        });
        btnLeaderboards.setOnClickListener(v -> {
            startActivity(new Intent(FamilyActivity.this, LeaderboardsActivity.class));
            overridePendingTransition(R.anim.zoom_in_enter, R.anim.zoom_in_exit);
        });
        btnChat.setOnClickListener(v -> {
            chatButtonHandler();
        });
        imgMenu.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), MenuActivity.class));
            overridePendingTransition(R.anim.slide_in_left,R.anim.no_animation);
        });
        btnMembers.setOnClickListener(view -> {
            membersButtonHandler();
        });

        chatListener();
        completedOrdersListener();
    }

    private void membersButtonHandler() {
        if (rvFamilyMember.getVisibility() == View.GONE){
            rvFamilyMember.setVisibility(View.VISIBLE);
            btnMembers.setIcon(getDrawable(R.drawable.ic_baseline_keyboard_arrow_up_24));
            btnMembers.setIconGravity(MaterialButton.ICON_GRAVITY_END);
            btnMembers.setIconSize(40);
        }
        else{
            rvFamilyMember.setVisibility(View.GONE);
            btnMembers.setIcon(getDrawable(R.drawable.ic_baseline_keyboard_arrow_down_24));
            btnMembers.setIconGravity(MaterialButton.ICON_GRAVITY_END);
            btnMembers.setIconSize(40);
        }
    }

    private void completedOrdersListener() {
        completedOrderRef = hearthDB.getReference("user_"+MyMethods.Cache.getString(context, "uid")+"_completed_orders");
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

    private void chatButtonHandler() {
        hearthDB.getReference("family_"+familyCode+"_members").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long childrenCount = snapshot.getChildrenCount();
                long counter = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    String uid = dataSnapshot.child("uid").getValue().toString();
                    String fullname = dataSnapshot.child("fullname").getValue().toString();
                    String photourl = dataSnapshot.child("photourl").getValue().toString();

                    MyMethods.Cache.setString(getApplicationContext(), "familymember_"+uid+"_fullname", fullname);
                    MyMethods.Cache.setString(getApplicationContext(), "familymember_"+uid+"_photourl", photourl);

                    counter++;
                    if (counter == childrenCount){
                        startActivity(new Intent(FamilyActivity.this, ChatActivity.class));
                        overridePendingTransition(R.anim.zoom_in_enter, R.anim.zoom_in_exit);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initializeTopAndBottomBars() {
        // Start of top action bar and bottom nav bar initialization
        tvActivityTitle.setText("Family");
        // update score
        updateFamilyScore();
        updateFamilyDetails();
        roleChangedListener();
        onKickedListener();
        botnavFamily.setSelectedItemId(R.id.family);
        botnavFamily.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.shop:
                    startActivity(new Intent(getApplicationContext(), ShopActivity.class));
                    finish();
                    overridePendingTransition(R.anim.no_animation,R.anim.no_animation);
                    return true;
                case R.id.missions:
                    startActivity(new Intent(getApplicationContext(), MissionsActivity.class));
                    finish();
                    overridePendingTransition(R.anim.no_animation,R.anim.no_animation);
                    return true;
                case R.id.family:
                    return true;
            }
            return false;
        });
    }

    private void roleChangedListener() {
        roleRef = hearthDB.getReference("family_"+familyCode+"_members").child("user_"+currentUserID).child("role");
        roleListener = roleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    arrayFamilyMember.clear();
                    currentUserRole = snapshot.getValue().toString();
                    MyMethods.Cache.setString(getApplicationContext(), "role", currentUserRole);

                    updateRvFamilyMembers();
                    toggleEditDetailsButtonVisibility();
                    updateRvJoinRequests();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateRvJoinRequests() {
        rvJoinRequests = findViewById(R.id.recyclerViewJoinRequests);

        hearthDB.getReference("family_"+familyCode+"_joinrequests").addValueEventListener(new ValueEventListener() {
            @SuppressLint("UnsafeOptInUsageError")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long joinRequestsCount = snapshot.getChildrenCount();
                BadgeDrawable joinRequestsBadge = BadgeDrawable.create(FamilyActivity.this);

                if (joinRequestsCount == 0){
                    btnJoinRequests.setVisibility(View.GONE);
                    tvJoinRequests.setVisibility(View.GONE);
                    joinRequestsBadge.setVisible(false);
                    return;
                }
                else {
                    btnJoinRequests.setVisibility(View.VISIBLE);
                    tvJoinRequests.setVisibility(View.VISIBLE);
                }

                generateJoinRequestBadge(joinRequestsBadge, joinRequestsCount);

                btnJoinRequests.setOnClickListener(view -> {
                    rvJoinRequestsToggleVisibility();
                });

                displayJoinRequests();
            }

            private void displayJoinRequests() {
                rvJoinRequests.setHasFixedSize(true);
                rvJoinRequests.setLayoutManager(new LinearLayoutManager(context));

                arrayJoinRequests = new ArrayList<>();
                joinRequestAdapter = new JoinRequestAdapter(context, arrayJoinRequests, onJoinRequestListener);
                rvJoinRequests.setAdapter(joinRequestAdapter);

                hearthDB.getReference("family_"+familyCode+"_joinrequests").addValueEventListener(new ValueEventListener() { // use retrieved familycode value to reference family shop items
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        arrayJoinRequests.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            JoinRequest joinRequest = dataSnapshot.getValue(JoinRequest.class);
                            arrayJoinRequests.add(joinRequest);
                        }
                        joinRequestAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }

            private void rvJoinRequestsToggleVisibility() {
                if (rvJoinRequests.getVisibility() == View.GONE){
                    rvJoinRequests.setVisibility(View.VISIBLE);
                    btnJoinRequests.setIcon(getDrawable(R.drawable.ic_baseline_keyboard_arrow_up_24));
                    btnJoinRequests.setIconGravity(MaterialButton.ICON_GRAVITY_END);
                    btnJoinRequests.setIconSize(40);
                }
                else{
                    rvJoinRequests.setVisibility(View.GONE);
                    btnJoinRequests.setIcon(getDrawable(R.drawable.ic_baseline_keyboard_arrow_down_24));
                    btnJoinRequests.setIconGravity(MaterialButton.ICON_GRAVITY_END);
                    btnJoinRequests.setIconSize(40);
                }
            }

            @SuppressLint("UnsafeOptInUsageError")
            private void generateJoinRequestBadge(BadgeDrawable joinRequestsBadge, long joinRequestsCount) {
                joinRequestsBadge.setNumber((int) joinRequestsCount);
                joinRequestsBadge.setVisible(true);
                joinRequestsBadge.setBadgeGravity(BadgeDrawable.TOP_END);
                joinRequestsBadge.setHorizontalOffset(-20);
                BadgeUtils.attachBadgeDrawable(joinRequestsBadge, tvJoinRequests);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initializeVars() {
        currentUserRole = MyMethods.Cache.getString(getApplicationContext(), "role");
        tvActivityTitle = findViewById(R.id.textViewActivityTitle);
        tvFamilyName = findViewById(R.id.textViewFamilyName);
        tvScore = findViewById(R.id.textViewScore);
        tvCode = findViewById(R.id.textViewCode);
        botnavFamily = findViewById(R.id.bottomNavigationMissions);
        imgFamilyIcon = findViewById(R.id.imgFamilyIcon);
        imgMenu = findViewById(R.id.imageViewButton);
        btnEditDetails = findViewById(R.id.buttonEditFamilyDetails);
        btnJoinRequests = findViewById(R.id.buttonJoinRequests);
            btnJoinRequests.setIcon(getDrawable(R.drawable.ic_baseline_keyboard_arrow_down_24));
            btnJoinRequests.setIconGravity(MaterialButton.ICON_GRAVITY_END);
            btnJoinRequests.setIconSize(40);
        tvJoinRequests = findViewById(R.id.tvJoinRequests);
        btnMembers = findViewById(R.id.buttonFamilyMembers);
            btnMembers.setIcon(getDrawable(R.drawable.ic_baseline_keyboard_arrow_down_24));
            btnMembers.setIconGravity(MaterialButton.ICON_GRAVITY_END);
            btnMembers.setIconSize(40);
        btnToggleCodeVisibility = findViewById(R.id.btnToggleCodeVisibility);
        btnCopyCode = findViewById(R.id.btnCopyCode);
        btnChat = findViewById(R.id.buttonFamilyChat);
        btnLeaderboards = findViewById(R.id.buttonFamilyLeaderboards);
        badgeOrders = BadgeDrawable.create(FamilyActivity.this);

        context = this;
        familyCode = MyMethods.Cache.getString(getApplicationContext(), "familycode");
        currentUserID = MyMethods.Cache.getString(getApplicationContext(), "uid");
    }

    private void updateFamilyDetails() {
        tvCode.setText(familyCode);
        tvFamilyName.setText(MyMethods.Cache.getString(getApplicationContext(), "globalfamilyname"));
        MyMethods.Generate.image(MyMethods.Cache.getString(getApplicationContext(), "familyphotourl"), imgFamilyIcon);
    }

    private void toggleEditDetailsButtonVisibility() {
        if (currentUserRole.equals("Admin")){
            generateOrderBadge();
            btnEditDetails.setVisibility(View.VISIBLE);
        }
        else {
            btnEditDetails.setVisibility(View.GONE);
        }
    }

    private void updateRvFamilyMembers() {
        rvFamilyMember = findViewById(R.id.recyclerViewFamilyMembersList);
        rvFamilyMember.setHasFixedSize(true);
        rvFamilyMember.setLayoutManager(new LinearLayoutManager(context));

        arrayFamilyMember = new ArrayList<>();
        adapterFamilyMember = new FamilyMemberAdapter(context, arrayFamilyMember, onFamilyMemberListener);
        rvFamilyMember.setAdapter(adapterFamilyMember);

        memberRef = hearthDB.getReference("family_"+familyCode+"_members");
        memberListener = memberRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayFamilyMember.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    FamilyMember familyMember = dataSnapshot.getValue(FamilyMember.class);
                    arrayFamilyMember.add(familyMember);
                }
                adapterFamilyMember.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateFamilyScore() {
        familyScoreRef = hearthDB.getReference("leaderboards/family_"+familyCode+"/score");
        familyScoreRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String score = snapshot.getValue().toString();
                tvScore.setText("Score: "+score);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onFamilyMemberClick(int position) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNavigationMissions = findViewById(R.id.bottomNavigationMissions);
        bottomNavigationMissions.setSelectedItemId(R.id.family);
        updateRvFamilyMembers();
        updateFamilyDetails();
    }

    private void generateOrderBadge(){
        // pre-fill badge with the cached number of orders (value might be outdated but rendering is faster)
        long cachedNumberOfOrders = MyMethods.Cache.getLong(getApplicationContext(), "numberoforders");
        MyMethods.Generate.badgeForBottomNav(badgeOrders, botnavFamily.findViewById(R.id.shop), cachedNumberOfOrders);

        orderArrayList = new ArrayList<>();

        // now fill badge with the real-time number of orders
        ordersReference = hearthDB.getReference("family_"+familyCode+"_orders");
        ordersListener = ordersReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("UnsafeOptInUsageError")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderArrayList.clear();

                long counter = 0;
                long numberOfOrders = snapshot.getChildrenCount();

                // display badge
                MyMethods.Generate.badgeForBottomNav(badgeOrders, botnavFamily.findViewById(R.id.shop), numberOfOrders);
                // update cache to updated value
                MyMethods.Cache.setLong(getApplicationContext(), "numberoforders", numberOfOrders);

                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Order order = dataSnapshot.getValue(Order.class);
                    orderArrayList.add(order);
                    counter++;
                }

                if (counter == numberOfOrders) {
                    for (int i=0; i<orderArrayList.size(); i++) {
                        if ((orderArrayList.get(i).getBuyername() == null) || (orderArrayList.get(i).getItemname() == null)) {
                            continue;
                        }
                        MyMethods.Notifications.orders(FamilyActivity.this, orderArrayList.get(i).getUid(), orderArrayList.get(i).getBuyername(), orderArrayList.get(i).getItemname(), orderArrayList.get(i).getDate());
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    @Override
    public void onJoinRequestClick(int position, int viewId) {
        switch (viewId) {
            case 1: // accept
                acceptJoinRequest(position);
                break;
            case 0: // deny
                denyJoinRequest(position);
                break;
        }
    }

    private void acceptJoinRequest(int position) {
        String requesterUid = arrayJoinRequests.get(position).getUid();
        String requesterFullname = arrayJoinRequests.get(position).getFullname();
        String requesterPhotourl = arrayJoinRequests.get(position).getPhotourl();

        // update requester's family code
        hearthDB.getReference("user_"+requesterUid).child("familycode").setValue(familyCode);
        // updatet family members list
        newMemberRef = hearthDB.getReference("family_"+familyCode+"_members").child("user_"+requesterUid);
        newMemberRef.child("uid").setValue(requesterUid);
        newMemberRef.child("fullname").setValue(requesterFullname);
        newMemberRef.child("photourl").setValue(requesterPhotourl);
        newMemberRef.child("role").setValue("Member");

        // remove the pending request
        hearthDB.getReference("user_"+requesterUid+"_joinrequest").removeValue();
        hearthDB.getReference("family_"+familyCode+"_joinrequests").child("user_"+requesterUid).removeValue();

        //updateRvJoinRequests();
        updateRvFamilyMembers();
    }

    private void denyJoinRequest(int position) {
        String requesterUid = arrayJoinRequests.get(position).getUid();

        // remove the pending request
        hearthDB.getReference("user_"+requesterUid+"_joinrequest").removeValue();
        hearthDB.getReference("family_"+familyCode+"_joinrequests").child("user_"+requesterUid).removeValue();

        //updateRvJoinRequests();
    }

    private void onKickedListener() {
        userFamilyCodeRef = hearthDB.getReference("user_"+currentUserID).child("familycode");
        userFamilyCodeListener = userFamilyCodeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    MyMethods.Notifications.membership(FamilyActivity.this, 2, "");
                    MaterialAlertDialogBuilder kickedDialog = new MaterialAlertDialogBuilder(FamilyActivity.this);
                    kickedDialog.setTitle("You were kicked out from this family");
                    kickedDialog.setMessage("We're sorry to inform that you were kicked out from this family.\nAll gold, XP, missions, and pending orders will be lost.");
                    kickedDialog.setPositiveButton("Back to login", (dialogInterface, i) -> {
                        Intent loginIntent = new Intent(FamilyActivity.this, LoginActivity.class);
                        startActivity(loginIntent);
                        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        finish();
                    });
                    kickedDialog.setOnCancelListener(dialogInterface -> {
                        Intent loginIntent = new Intent(FamilyActivity.this, LoginActivity.class);
                        startActivity(loginIntent);
                        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        finish();
                    });
                    if (!FamilyActivity.this.isFinishing()) {
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

                        MyMethods.Notifications.chat(FamilyActivity.this, photoUrl, fullName, message, timestamp);
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
        memberRef.removeEventListener(memberListener);
        userFamilyCodeRef.removeEventListener(userFamilyCodeListener);
        if (currentUserRole.equals("Admin")) {
            ordersReference.removeEventListener(ordersListener);
        }
    }
}
package com.hearth.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hearth.R;
import com.hearth.adapters.MissionAdapter;
import com.hearth.alarm.MissionAlarmReceiver;
import com.hearth.authentication.LoginActivity;
import com.hearth.authentication.RequestJoinActivity;
import com.hearth.methods.MyMethods;
import com.hearth.objects.CompletedMission;
import com.hearth.objects.Mission;
import com.hearth.objects.Order;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;

public class MissionsActivity extends AppCompatActivity implements MissionAdapter.OnMissionListener{

    String currentUserID = FirebaseAuth.getInstance().getUid();
    FirebaseDatabase hearthDB = FirebaseDatabase.getInstance();
    DatabaseReference missionsReference, ordersReference, tempMissionReference, missionsCompletedReference, myAlarmsReference, lastMessageRef, completedOrderRef;
    ValueEventListener missionsListener, missionsCompletedListener, myAlarmsListener, ordersListener, lastMessageListener, completedOrderListener;
    Context context;

    double goldBalance;
    String familycode;
    String currentUserRole;
    ArrayList<Long> myAlarms;
    ArrayList<Long> familyAlarms;
    ArrayList<Integer> familyAlarmsIndex;
    ArrayList<Order> orderArrayList;

    MissionAdapter missionAdapter;
    ArrayList<Mission> missionArrayList;
    ArrayList<String> missionsCompletedArrayList;
    RecyclerView recyclerViewMissionsList;
    MissionAdapter.OnMissionListener onMissionListener = this;
    ImageView imageViewGoldIcon, imageViewMenuButton;
    RoundedImageView imageViewMissionsBanner;
    TextView textViewActivityTitle, textViewGoldBalance;
    ExtendedFloatingActionButton extendedFloatingActionButtonAddMission;
    BottomNavigationView bottomNavigationMissions;
    BadgeDrawable badgeNumberOfOrders;
    LinearProgressIndicator recyclerViewProgress;
    MaterialButton btnMissionLogs;

    MaterialCheckBox checkboxCompletedMissions;

    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_missions);

        createNotification();
        initializeValues();
        topActionBarHandler();
        checkboxCompletedMissionsHandler();
        btnMissionLogsHandler();
        menuButtonHandler();
        checkIfUserLeveledUp();
        checkUserRole();
        onKickedListener();
        chatListener();
        completedOrdersListener();
    }

    private void btnMissionLogsHandler() {
        btnMissionLogs.setVisibility(View.VISIBLE);
        btnMissionLogs.setOnClickListener(view -> {
            startActivity(new Intent(MissionsActivity.this, MissionLogsActivity.class));
            overridePendingTransition(R.anim.zoom_in_enter, R.anim.zoom_in_exit);
        });
    }

    private void createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "Mission alarms";
            String description = "Mission alarms";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel("hearth_notifications", name, importance);
            notificationChannel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private void initializeValues() {
        context = this;
        familycode = MyMethods.Cache.getString(getApplicationContext(), "familycode");
        currentUserRole = MyMethods.Cache.getString(getApplicationContext(), "role");
        imageViewMissionsBanner = findViewById(R.id.imageViewMissionsBanner);
        MyMethods.Generate.banner(MyMethods.Cache.getString(getApplicationContext(), "mission_banner_link"), imageViewMissionsBanner);
        textViewActivityTitle = findViewById(R.id.textViewActivityTitle);
        bottomNavigationMissions = findViewById(R.id.bottomNavigationMissions);
        imageViewMenuButton = findViewById(R.id.imageViewButton);
        extendedFloatingActionButtonAddMission = findViewById(R.id.extendedFloatingActionButtonAddMission);
        imageViewGoldIcon = findViewById(R.id.imageViewGoldIcon);
        textViewGoldBalance = findViewById(R.id.textViewGoldBalance);
        checkboxCompletedMissions = findViewById(R.id.checkboxCompletedMissions);
        badgeNumberOfOrders = BadgeDrawable.create(MissionsActivity.this);
        recyclerViewProgress = findViewById(R.id.recyclerViewProgress);
        btnMissionLogs = findViewById(R.id.btnMissionLogs);
        recyclerViewProgress.hide();
    }

    private void menuButtonHandler() {
        imageViewMenuButton.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), MenuActivity.class));
            overridePendingTransition(R.anim.slide_in_left,R.anim.no_animation);
        });
    }

    private void topActionBarHandler() {
        imageViewGoldIcon.setVisibility(View.VISIBLE);
        textViewGoldBalance.setVisibility(View.VISIBLE);
        textViewActivityTitle.setText("Missions");
        updateGoldBalanceDisplay();
        bottomNavigationMissions.setSelectedItemId(R.id.missions);
        bottomNavigationMissions.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.shop:
                    startActivity(new Intent(getApplicationContext(), ShopActivity.class));
                    overridePendingTransition(R.anim.no_animation,R.anim.no_animation);
                    finish();
                    return true;
                case R.id.missions:
                    return true;
                case R.id.family:
                    startActivity(new Intent(getApplicationContext(), FamilyActivity.class));
                    overridePendingTransition(R.anim.no_animation,R.anim.no_animation);
                    finish();
                    return true;
            }
            return false;
        });
    }

    private void checkUserRole() {
        if (currentUserRole.equals("Admin")){
            extendedFloatingActionButtonAddMission.setVisibility(View.VISIBLE);
            generateOrderBadge();

            // start - add mission efab handler
            extendedFloatingActionButtonAddMission.setOnClickListener(view -> {
                // get temporary mission reference and get firebase-generated uid
                tempMissionReference = hearthDB.getReference("family_"+familycode+"_missions_temp").push();
                String missionUid = tempMissionReference.getKey();
                tempMissionReference.child("uid").setValue(missionUid);

                // start manage item activity and push item uid
                Intent intentAddMission = new Intent(getApplicationContext(), ManageMissionActivity.class);
                intentAddMission.putExtra("manage_mode", 0);
                intentAddMission.putExtra("mission_uid", missionUid);
                startActivity(intentAddMission);
                overridePendingTransition(R.anim.zoom_in_enter, R.anim.zoom_in_exit);
            });
            // end - add mission efab handler
        }
        else {
            extendedFloatingActionButtonAddMission.setVisibility(View.GONE);
        }
    }

    private void checkIfUserLeveledUp() {
        boolean didLevelUp = getIntent().getBooleanExtra("didLvlUp", false);
        if (didLevelUp){
            int currentLvl = getIntent().getIntExtra("currentLvl", 0);
            int updatedLvl = getIntent().getIntExtra("updatedLvl", 0);
            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(MissionsActivity.this);
            materialAlertDialogBuilder.setTitle("Level up!");
            materialAlertDialogBuilder.setMessage("Your hardwork is paying off. \nYou are now level "+updatedLvl+"!");
            materialAlertDialogBuilder.setPositiveButton("Hooray!", (dialogInterface, i) -> {
            });
            materialAlertDialogBuilder.show();

            MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.fanfare);
            mp.setLooping(false);
            mp.start();
        }
    }

    private void checkboxCompletedMissionsHandler() {
        int completedMissionsVisibility = MyMethods.Cache.getInt(getApplicationContext(), currentUserID+"show_completed_missions_state");
        if (completedMissionsVisibility == 0){
            checkboxCompletedMissions.setChecked(false);
        }
        else {
            checkboxCompletedMissions.setChecked(true);
        }
        checkboxCompletedMissions.setOnCheckedChangeListener((compoundButton, b) -> {
            recyclerViewMissionsList.setVisibility(View.INVISIBLE);
            recyclerViewProgress.show();
            missionsReference.removeEventListener(missionsListener);
            missionsCompletedReference.removeEventListener(missionsCompletedListener);
            if (b == true){
                MyMethods.Cache.setInt(getApplicationContext(), currentUserID+"show_completed_missions_state", 1);
            }
            else {
                MyMethods.Cache.setInt(getApplicationContext(), currentUserID+"show_completed_missions_state", 0);
            }
            updateRecyclerview();
        });
    }

    private void updateRecyclerview() {
        recyclerViewMissionsList = findViewById(R.id.recyclerViewMissionsList);
        recyclerViewMissionsList.setHasFixedSize(true);
        recyclerViewMissionsList.setLayoutManager(new LinearLayoutManager(context));

        missionsCompletedArrayList = new ArrayList<>();
        missionsCompletedReference = hearthDB.getReference("user_"+currentUserID+"_missions_completed");
        missionsCompletedListener = missionsCompletedReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    missionsCompletedArrayList.add(dataSnapshot.getValue().toString());
                }
                missionArrayList = new ArrayList<>();
                familyAlarms = new ArrayList<>();
                //familyAlarmsIndex = new ArrayList<>();
                missionAdapter = new MissionAdapter(context, missionArrayList, onMissionListener, missionsCompletedArrayList);
                recyclerViewMissionsList.setAdapter(missionAdapter);

                // finally, display all missions
                missionsReference = hearthDB.getReference("family_"+familycode+"_missions");

                missionsListener = missionsReference.orderByChild("alarmmilitary").addValueEventListener(new ValueEventListener() { // use retrieved familycode value to reference menu
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int counter = 0;
                        missionArrayList.clear();
                        familyAlarms.clear();
                        //familyAlarmsIndex.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            Mission mission = dataSnapshot.getValue(Mission.class);
                            missionArrayList.add(mission);
                            familyAlarms.add(mission.getAlarm());
                            //familyAlarmsIndex.add(counter);
                            counter++;
                        }
                        missionAdapter.notifyDataSetChanged();
                        recyclerViewMissionsList.setVisibility(View.VISIBLE);
                        recyclerViewProgress.hide();

                        myAlarms = new ArrayList<>();
                        myAlarmsReference = hearthDB.getReference("user_"+currentUserID+"_alarms");
                        myAlarmsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                myAlarms.clear();
                                for (DataSnapshot dataSnaphot : snapshot.getChildren()) {
                                    myAlarms.add((Long) dataSnaphot.getValue());
                                }

                                int familyAlarmsCount = familyAlarms.size();
                                for (int i=0; i<familyAlarmsCount; i++){
                                    if (!myAlarms.contains(familyAlarms.get(i))) {
                                        String missionTitle = missionArrayList.get(i).getTitle();
                                        String missionDescription = missionArrayList.get(i).getDescription();
                                        double missionReward = missionArrayList.get(i).getReward();
                                        String missionUid = missionArrayList.get(i).getUid();
                                        long missionAlarm = missionArrayList.get(i).getAlarm();
                                        String missionImagelink = missionArrayList.get(i).getImagelink();
                                        setAlarm(missionTitle, missionDescription, missionReward, missionUid, missionAlarm, missionImagelink);

                                        myAlarmsReference.child(String.valueOf(missionAlarm)).setValue(missionAlarm);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

                if (currentUserRole.equals("Admin")){
                    recyclerViewMissionsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);
                            if (dy > 0){
                                extendedFloatingActionButtonAddMission.shrink();
                                extendedFloatingActionButtonAddMission.hide();
                            }
                            else {
                                extendedFloatingActionButtonAddMission.show();
                                extendedFloatingActionButtonAddMission.extend();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void generateOrderBadge() {
        // pre-fill badge with the cached number of orders (value might be outdated but rendering is faster)
        long cachedNumberOfOrders = MyMethods.Cache.getLong(getApplicationContext(), "numberoforders");
        MyMethods.Generate.badgeForBottomNav(badgeNumberOfOrders, bottomNavigationMissions.findViewById(R.id.shop), cachedNumberOfOrders);

        orderArrayList = new ArrayList<>();

        // now fill badge with the real-time number of orders
        ordersReference = hearthDB.getReference("family_"+familycode+"_orders");
        ordersListener = ordersReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("UnsafeOptInUsageError")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderArrayList.clear();

                long counter = 0;
                long numberOfOrders = snapshot.getChildrenCount();

                // display badge
                MyMethods.Generate.badgeForBottomNav(badgeNumberOfOrders, bottomNavigationMissions.findViewById(R.id.shop), numberOfOrders);
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
                        MyMethods.Notifications.orders(MissionsActivity.this, orderArrayList.get(i).getUid(), orderArrayList.get(i).getBuyername(), orderArrayList.get(i).getItemname(), orderArrayList.get(i).getDate());
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateRecyclerview();
        updateGoldBalanceDisplay();
        BottomNavigationView bottomNavigationMissions = findViewById(R.id.bottomNavigationMissions);
        bottomNavigationMissions.setSelectedItemId(R.id.missions);
    }

    @Override
    public void onMissionClick(int position) {
        // get mission details
        String missionTitle = missionArrayList.get(position).getTitle();
        String missionDescription = missionArrayList.get(position).getDescription();
        double missionReward = missionArrayList.get(position).getReward();
        String missionUid = missionArrayList.get(position).getUid();
        String imageLink = missionArrayList.get(position).getImagelink();
        long missionAlarm = missionArrayList.get(position).getAlarm();
        String missionAlarmMilitary = missionArrayList.get(position).getAlarmmilitary();

        // get user role
        currentUserRole = MyMethods.Cache.getString(MissionsActivity.this, "role");
        switch (currentUserRole){
            case "Admin": {
                // display dialog, admin can edit the item, members can only purchase item
                MaterialAlertDialogBuilder materialAlertDialogBuilderAdmin = new MaterialAlertDialogBuilder(MissionsActivity.this);
                materialAlertDialogBuilderAdmin.setTitle("Edit or Do Mission "+missionTitle);
                materialAlertDialogBuilderAdmin.setMessage("As a family Admin, you may Edit \""+missionTitle+"\" mission details or Do the mission for a reward of "+ MyMethods.DoubleMethods.formatDoubleToString(missionReward)+" gold coins.");
                materialAlertDialogBuilderAdmin.setPositiveButton("Do this mission", (dialogInterface, i) -> {
                    doMission(missionTitle, missionDescription, missionReward, missionUid, imageLink);
                });
                materialAlertDialogBuilderAdmin.setNegativeButton("Edit", (dialogInterface, i) -> {
                    Intent intentEditMissionActivity = new Intent(getApplicationContext(), ManageMissionActivity.class);
                    intentEditMissionActivity.putExtra("manage_mode", 1);
                    intentEditMissionActivity.putExtra("mission_title", missionTitle);
                    intentEditMissionActivity.putExtra("mission_description", missionDescription);
                    intentEditMissionActivity.putExtra("mission_reward", MyMethods.DoubleMethods.formatDoubleToString(missionReward));
                    intentEditMissionActivity.putExtra("mission_uid", missionUid);
                    intentEditMissionActivity.putExtra("mission_alarm", missionAlarm);
                    intentEditMissionActivity.putExtra("mission_alarmmilitary", missionAlarmMilitary);
                    startActivity(intentEditMissionActivity);
                    overridePendingTransition(R.anim.zoom_in_enter, R.anim.zoom_in_exit);
                });
                materialAlertDialogBuilderAdmin.setNeutralButton("Cancel", (dialogInterface, i) -> {
                });
                materialAlertDialogBuilderAdmin.show();
                break;
            }
            case "Member":
                doMission(missionTitle, missionDescription, missionReward, missionUid, imageLink);
                break;
        }
    }

    public void doMission(String missionTitle, String missionDescription, double missionReward, String missionUid, String imageLink){
        // go to pomodoro activity
        Intent intentPomodoroActivity = new Intent(MissionsActivity.this, PomodoroActivity.class);
        intentPomodoroActivity.putExtra("mission_title", missionTitle);
        intentPomodoroActivity.putExtra("mission_description", missionDescription);
        intentPomodoroActivity.putExtra("mission_reward", missionReward);
        intentPomodoroActivity.putExtra("mission_uid", missionUid);
        intentPomodoroActivity.putExtra("mission_imagelink", imageLink);
        startActivity(intentPomodoroActivity);
        overridePendingTransition(R.anim.zoom_in_enter, R.anim.zoom_in_exit);
    }

    public void updateGoldBalanceDisplay(){
        double goldbalance = MyMethods.Cache.getDouble(getApplicationContext(), "gold");
        textViewGoldBalance.setText(MyMethods.DoubleMethods.formatDoubleToString(goldbalance));
    }

    private void setAlarm(String missionTitle, String missionDescription, double missionReward, String missionUid, long missionAlarm, String missionImagelink) {
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, MissionAlarmReceiver.class);

        intent.putExtra("mission_title", missionTitle);
        intent.putExtra("mission_description", missionDescription);
        intent.putExtra("mission_reward", missionReward);
        intent.putExtra("mission_uid", missionUid);
        intent.putExtra("mission_imagelink", missionImagelink);

        pendingIntent = PendingIntent.getBroadcast(this, (int) missionAlarm, intent, 0);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, missionAlarm, pendingIntent);
        // alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, missionAlarm, AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    private void onKickedListener() {
        hearthDB.getReference("user_"+currentUserID).child("familycode").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    MyMethods.Notifications.membership(MissionsActivity.this, 2, "");
                    MaterialAlertDialogBuilder kickedDialog = new MaterialAlertDialogBuilder(MissionsActivity.this);
                    kickedDialog.setTitle("You were kicked out from this family");
                    kickedDialog.setMessage("We're sorry to inform that you were kicked out from this family.\nAll gold, XP, missions, and pending orders will be lost.");
                    kickedDialog.setPositiveButton("Back to login", (dialogInterface, i) -> {
                        Intent loginIntent = new Intent(MissionsActivity.this, LoginActivity.class);
                        startActivity(loginIntent);
                        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        finish();
                    });
                    kickedDialog.setOnCancelListener(dialogInterface -> {
                        Intent loginIntent = new Intent(MissionsActivity.this, LoginActivity.class);
                        startActivity(loginIntent);
                        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        finish();
                    });
                    if (!MissionsActivity.this.isFinishing()) {
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
        lastMessageRef = hearthDB.getReference("family_"+familycode+"_chat_lastmessage");
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

                        MyMethods.Notifications.chat(MissionsActivity.this, photoUrl, fullName, message, timestamp);
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

    @Override
    protected void onPause() {
        super.onPause();
        lastMessageRef.removeEventListener(lastMessageListener);
        completedOrderRef.removeEventListener(completedOrderListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        missionsReference.removeEventListener(missionsListener);
        if (currentUserRole.equals("Admin")) {
            missionsReference.removeEventListener(ordersListener);
        }
    }
}
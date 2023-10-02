package com.hearth.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hearth.R;
import com.hearth.alarm.MissionAlarmReceiver;
import com.hearth.methods.MyMethods;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.Calendar;

public class ManageMissionActivity extends AppCompatActivity {

    FirebaseDatabase hearthDB = FirebaseDatabase.getInstance();
    String currentUserID = FirebaseAuth.getInstance().getUid();
    DatabaseReference newMissionReference, missionReference;

    RoundedImageView imageViewMissionThumbnail;
    TextInputLayout textInputLayoutTimePicker;
    TextInputEditText etMissionTitle, etMissionDescription, etReward;
    MaterialButton materialButtonClose, materialButtonDeleteMission, materialButtonSaveMission, materialButtonIncrementReward, materialButtonDecrementReward, materialButtonAddMissionThumbnail, materialButtonTimePicker;
    MaterialTimePicker materialTimePicker;

    private Calendar calendar;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    long missionAlarm = Long.MAX_VALUE;
    String ALARM_MILITARY_DEFAULT_VALUE = "2401";

    @Override
    protected void onResume() {
        super.onResume();
        // get image link after returning from browse image activity
        String mission_uid = getIntent().getExtras().getString("mission_uid");
        checkIfHasImage(mission_uid);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_mission);

        createNotification();

        materialButtonClose = findViewById(R.id.materialButtonClose);
        imageViewMissionThumbnail = findViewById(R.id.imageViewMissionThumbnail);
        materialButtonAddMissionThumbnail = findViewById(R.id.materialButtonAddMissionThumbnail);
        materialButtonDeleteMission = findViewById(R.id.materialButtonDeleteMission);
        materialButtonSaveMission = findViewById(R.id.materialButtonSaveMission);
        etMissionTitle = findViewById(R.id.textInputEditTextMissionTitle);
        etMissionDescription = findViewById(R.id.textInputEditTextMissionDescription);
        etReward = findViewById(R.id.textInputEditTextReward);
        materialButtonIncrementReward = findViewById(R.id.materialButtonIncrementReward);
        materialButtonDecrementReward = findViewById(R.id.materialButtonDecrementReward);
        materialButtonTimePicker = findViewById(R.id.materialButtonTimePicker);

        // start - initialize values
        int manage_mode = getIntent().getExtras().getInt("manage_mode");
        String mission_title = getIntent().getExtras().getString("mission_title");
        String mission_description = getIntent().getExtras().getString("mission_description");
        String mission_reward = getIntent().getExtras().getString("mission_reward");
        String mission_uid = getIntent().getExtras().getString("mission_uid");
        String mission_alarmMilitary = getIntent().getExtras().getString("mission_alarmmilitary", ALARM_MILITARY_DEFAULT_VALUE);
        missionAlarm = getIntent().getExtras().getLong("mission_alarm", Long.MAX_VALUE);

        // close button click listener
        materialButtonClose.setOnClickListener(view -> {
            deleteTempMission(mission_uid);
            finish();
        });

        checkIfHasImage(mission_uid);

        // edit = 1
        if (manage_mode == 0){
            materialButtonDeleteMission.setVisibility(View.GONE);
        }
        else if (manage_mode == 1){

            // display mission details
            etMissionTitle.setText(mission_title);
            etMissionDescription.setText(mission_description);
            etReward.setText(mission_reward);

            if (!mission_alarmMilitary.equals("2401")){
                materialButtonTimePicker.setText(MyMethods.Time.toClock(missionAlarm));
            }

            materialButtonDeleteMission.setVisibility(View.VISIBLE);
            materialButtonDeleteMission.setOnClickListener(view -> {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                deleteTempMission(mission_uid);
                deleteMission(mission_uid);
            });
        }

        // start - save button handler
        materialButtonSaveMission.setOnClickListener(view -> {
            // validate input
            if (TextUtils.isEmpty(etMissionTitle.getText().toString().trim()) || (TextUtils.isEmpty(etReward.getText().toString().trim()))){
                MyMethods.Generate.warningDialog(ManageMissionActivity.this, "Cannot save mission", "Please enter a mission title and reward");
            }
            else{
                setAlarm();

                String missionTitle = etMissionTitle.getText().toString().trim();
                String missionDescription = etMissionDescription.getText().toString().trim();
                double initialMissionReward = Double.parseDouble(etReward.getText().toString().trim());
                String missionAlarmMilitary = ALARM_MILITARY_DEFAULT_VALUE;
                if (missionAlarm!=Long.MAX_VALUE){
                    missionAlarmMilitary = MyMethods.Time.toMilitary(missionAlarm);
                }

                // validate description (can be empty)
                if (TextUtils.isEmpty(missionDescription)){
                    missionDescription = "";
                }

                // invalidate empty fields
                if (initialMissionReward < 0){
                    MyMethods.Generate.warningDialog(ManageMissionActivity.this, "Invalid Number", "Rewards may not be less than 0");
                }
                // save mission
                else {
                    // check if naay uid (this means a mission is to be updated)
                    double missionReward = initialMissionReward;
                    if (manage_mode == 0){ // if mode == add
                        deleteTempMission(mission_uid);
                        saveMissionToFirebase(mission_uid, missionTitle, missionDescription, missionReward, missionAlarm, missionAlarmMilitary);
                    }
                    else if (manage_mode == 1){ // if mode == update
                        deleteTempMission(mission_uid);
                        updateMission(getIntent().getExtras().getString("mission_uid"), missionTitle, missionDescription, missionReward, missionAlarm, missionAlarmMilitary);
                    }
                }
            }
        });
        // - save button handler

        // start - 2 decimal places limiter
        etReward.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                MyMethods.EditTextMethods.decimalPlaceValueLimiter(etReward, etReward.getText().toString());
                MyMethods.EditTextMethods.limitCharacters(etReward, etReward.getText().toString());
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });
        // end - 2 decimal places limiter

        materialButtonDecrementReward.setOnClickListener(view -> {
            TextInputEditText textInputEditTextPrice = findViewById(R.id.textInputEditTextReward);
            MyMethods.EditTextMethods.incrementDouble(textInputEditTextPrice, -1);
        });
        materialButtonIncrementReward.setOnClickListener(view -> {
            TextInputEditText textInputEditTextPrice = findViewById(R.id.textInputEditTextReward);
            MyMethods.EditTextMethods.incrementDouble(textInputEditTextPrice, 1);
        });

        materialButtonTimePicker.setOnClickListener(view -> {
            showTimePicker();
        });
    }

    private void setAlarm() {
        if (!materialButtonTimePicker.getText().toString().trim().equals("Add alarm time (Optional)")){
            if (missionAlarm !=Long.MAX_VALUE){
                alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(this, MissionAlarmReceiver.class);

                String missionUid = getIntent().getExtras().getString("mission_uid");
                intent.putExtra("mission_title", etMissionTitle.getText().toString().trim());
                intent.putExtra("mission_description", etMissionDescription.getText().toString().trim());
                intent.putExtra("mission_reward", Double.parseDouble(etReward.getText().toString().trim()));
                intent.putExtra("mission_uid", missionUid);

                int has_image = MyMethods.Cache.getInt(getApplicationContext(), getIntent().getExtras().getString("mission_uid")+"has_image");
                if (has_image == 1){
                    intent.putExtra("mission_imagelink", MyMethods.Cache.getString(getApplicationContext(), missionUid+"image_link"));
                }
                pendingIntent = PendingIntent.getBroadcast(this, (int) missionAlarm, intent, 0);

                alarmManager.setExact(AlarmManager.RTC_WAKEUP, missionAlarm, pendingIntent);
                // alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, missionAlarm, AlarmManager.INTERVAL_DAY, pendingIntent);

                hearthDB.getReference("user_"+currentUserID+"_alarms").child(String.valueOf(missionAlarm)).setValue(missionAlarm);
            }
        }
    }

    private void showTimePicker() {
        materialTimePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
                .setMinute(Calendar.getInstance().get(Calendar.MINUTE))
                .setTitleText("Select Alarm Time")
                .build();
        materialTimePicker.show(getSupportFragmentManager(), "hearth_timepicker");
        materialTimePicker.addOnPositiveButtonClickListener(view -> {
           if (materialTimePicker.getHour() >= 12){
               if (materialTimePicker.getHour() == 12){
                   materialButtonTimePicker.setText("12:" + String.format("%02d", materialTimePicker.getMinute()) + " PM");
               }
               else {
                   materialButtonTimePicker.setText(String.format("%02d", (materialTimePicker.getHour()) - 12) + ":" + String.format("%02d", materialTimePicker.getMinute()) + " PM");
               }
           }
           else {
               if (materialTimePicker.getHour() == 0){ // if 12 AM (if wala ni nga if condition, 00:00 AM ang ma-output)
                   materialButtonTimePicker.setText("12:" + String.format("%02d", materialTimePicker.getMinute()) + " AM");
               }
               else {
                   materialButtonTimePicker.setText(String.format("%02d", materialTimePicker.getHour()) + ":" + String.format("%02d", materialTimePicker.getMinute()) + " AM");
               }
           }

           calendar = Calendar.getInstance();
           calendar.set(Calendar.HOUR_OF_DAY, materialTimePicker.getHour());
           calendar.set(Calendar.MINUTE, materialTimePicker.getMinute());
           calendar.set(Calendar.SECOND, 0);
           calendar.set(Calendar.MILLISECOND, 0);

           missionAlarm = calendar.getTimeInMillis();
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

    private void checkIfHasImage(String mission_uid) {
        // get cached image mode value
        int has_image = MyMethods.Cache.getInt(getApplicationContext(), mission_uid+"has_image");

        if (has_image == 0){ // 0 == has no image
            imageViewMissionThumbnail.setVisibility(View.INVISIBLE);
            materialButtonAddMissionThumbnail.setVisibility(View.VISIBLE);

            // start - image button handler
            materialButtonAddMissionThumbnail.setOnClickListener(view -> {
                Intent intentBrowseImage = new Intent(getApplicationContext(), MissionImageBrowserActivity.class);
                intentBrowseImage.putExtra("mission_uid", mission_uid);
                startActivity(intentBrowseImage);
                overridePendingTransition(R.anim.zoom_in_enter, R.anim.zoom_in_exit);
            });
            // end - image button handler
        }
        else if (has_image == 1){ // 1 == has image
            imageViewMissionThumbnail.setVisibility(View.VISIBLE);
            materialButtonAddMissionThumbnail.setVisibility(View.INVISIBLE);

            // render image
            String imageLink = MyMethods.Cache.getString(getApplicationContext(), mission_uid+"image_link");
            MyMethods.Generate.image(imageLink, imageViewMissionThumbnail);

            // start - image button handler
            imageViewMissionThumbnail.setOnClickListener(view -> {
                Intent intentBrowseImage = new Intent(getApplicationContext(), MissionImageBrowserActivity.class);
                intentBrowseImage.putExtra("mission_uid", mission_uid);
                startActivity(intentBrowseImage);
                overridePendingTransition(R.anim.zoom_in_enter, R.anim.zoom_in_exit);
            });
            // end - image button handler
        }
    }

    private void saveMissionToFirebase(String missionUid, String missionTitle, String missionDescription, double missionReward, long missionAlarm, String missionAlarmMilitary) {
        // get family code
        String familycode = MyMethods.Cache.getString(getApplicationContext(), "familycode");

        // get new mission referencec
        newMissionReference = hearthDB.getReference("family_"+familycode+"_missions").child(missionUid);

        // insert values using retrieved family code
        newMissionReference.child("uid").setValue(missionUid);
        newMissionReference.child("title").setValue(missionTitle);
        newMissionReference.child("description").setValue(missionDescription);
        newMissionReference.child("reward").setValue(missionReward);
        newMissionReference.child("alarm").setValue(missionAlarm);
        newMissionReference.child("alarmmilitary").setValue(missionAlarmMilitary);

        int has_image = MyMethods.Cache.getInt(getApplicationContext(), missionUid+"has_image");
        if (has_image == 1){
            newMissionReference.child("imagelink").setValue(MyMethods.Cache.getString(getApplicationContext(), missionUid+"image_link"));
        }
        finish();
    }

    private void updateMission(String missionUid, String missionTitle, String missionDescription, double missionReward, long missionAlarm, String missionAlarmMilitary) {
        String strRoundedToHundredths = String.format("%.2f", missionReward);
        double roundedToHudredths = Double.parseDouble(strRoundedToHundredths);

        // get family code
        String familycode = MyMethods.Cache.getString(getApplicationContext(), "familycode");

        // update mission
        missionReference = hearthDB.getReference("family_"+familycode+"_missions").child(missionUid);
        missionReference.child("title").setValue(missionTitle);
        missionReference.child("description").setValue(missionDescription);
        missionReference.child("reward").setValue(roundedToHudredths);
        missionReference.child("alarm").setValue(missionAlarm);
        missionReference.child("alarmmilitary").setValue(missionAlarmMilitary);
        int has_image = MyMethods.Cache.getInt(getApplicationContext(), missionUid+"has_image");
        if (has_image == 1){
            missionReference.child("imagelink").setValue(MyMethods.Cache.getString(getApplicationContext(), missionUid+"image_link"));
        }
        finish();
    }

    private void deleteMission(String missionUid) {
        // get family code
        String familycode = MyMethods.Cache.getString(getApplicationContext(), "familycode");

        // add new mission
        missionReference = hearthDB.getReference("family_"+familycode+"_missions").child(missionUid);
        missionReference.removeValue();
        Toast.makeText(getApplicationContext(), "Successfully deleted mission", Toast.LENGTH_SHORT).show();

        finish();
    }

    private void deleteTempMission(String mission_uid) {
        // get family code
        String familycode = MyMethods.Cache.getString(getApplicationContext(), "familycode");

        // add new mission
        hearthDB.getReference("family_"+familycode+"_missions_temp").child(mission_uid).removeValue();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        String mission_uid = getIntent().getExtras().getString("mission_uid");
        deleteTempMission(mission_uid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.zoom_out_enter, R.anim.zoom_out_exit);
    }
}
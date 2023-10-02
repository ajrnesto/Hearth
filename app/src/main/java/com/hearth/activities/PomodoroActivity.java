package com.hearth.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.hearth.R;
import com.hearth.methods.MyMethods;

import java.util.concurrent.TimeUnit;

public class PomodoroActivity extends AppCompatActivity {

    FirebaseDatabase hearthDB = FirebaseDatabase.getInstance();
    DatabaseReference leaderboardReference, missionsDoneReference, newMissionLogRef;

    TextView textViewTimer, textViewPomoLabel, textViewMissionTitle, textViewMissionDescription, textViewMissionReward;
    MaterialButton materialButtonStart, materialButtonPause, materialButtonResume, materialButtonFinish, materialButtonClose;
    CircularProgressIndicator progressBarTimer;
    CountDownTimer countDownTimer;
    ImageView imageViewMissionImage, imageViewPomoProgress1, imageViewPomoProgress2, imageViewPomoProgress3, imageViewPomoProgress4;

    ConstraintLayout constraintLayoutPomodoroActivityParent;
    String familycode, missionUid, currentUserId;

    long pomoTimerPausePosition = 0; // store timer position when paused
    int pomoProgress = 0; // 0=default, 1=pomo, 2=break, 3=pomo, 4=break, 5=pomo, 6=break, 7=pomo, 8=longbreak
    int pomoPoints = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pomodoro);

        initializeVars();
        loadCurrentMission();

        missionUid = getIntent().getStringExtra("mission_uid");
        familycode = MyMethods.Cache.getString(getApplicationContext(), "familycode");

        progressTracker();

        materialButtonClose.setOnClickListener(view -> {
            finish();
        });

        materialButtonStart.setOnClickListener(view -> {
            startPomoTimer();
        });

        materialButtonPause.setOnClickListener(view -> {
            pausePomoTimer();
        });

        materialButtonResume.setOnClickListener(view -> {
            int remainder = pomoProgress % 2;
            if (remainder == 0){ // 0 = doing pomo, 1 = doing break
                resumePomoTimer(pomoTimerPausePosition);
            }
            else{
                resumeBreakTimer(pomoTimerPausePosition);
            }
        });

        materialButtonFinish.setOnClickListener(view -> {
            finishMission();
        });
    }

    private void initializeVars() {
        materialButtonClose = findViewById(R.id.materialButtonClose);
        textViewTimer = findViewById(R.id.textViewTimer);
        textViewMissionTitle = findViewById(R.id.textViewMissionTitle);
        textViewMissionDescription = findViewById(R.id.textViewMissionDescription);
        textViewMissionReward = findViewById(R.id.textViewMissionReward);
        imageViewMissionImage = findViewById(R.id.imageViewMissionImage);
        progressBarTimer = findViewById(R.id.progressBarTimer);
        materialButtonStart = findViewById(R.id.materialButtonStart);
        materialButtonPause = findViewById(R.id.materialButtonPause);
        materialButtonResume = findViewById(R.id.materialButtonResume);
        materialButtonFinish = findViewById(R.id.materialButtonFinish);
        imageViewPomoProgress1 = findViewById(R.id.imageViewPomoProgress1);
        imageViewPomoProgress2 = findViewById(R.id.imageViewPomoProgress2);
        imageViewPomoProgress3 = findViewById(R.id.imageViewPomoProgress3);
        imageViewPomoProgress4 = findViewById(R.id.imageViewPomoProgress4);
        textViewPomoLabel = findViewById(R.id.textViewPomoLabel);
        constraintLayoutPomodoroActivityParent = findViewById(R.id.constraintLayoutPomodoroActivityParent);
        currentUserId = MyMethods.Cache.getString(getApplicationContext(), "uid");
    }

    private void loadCurrentMission() {
        String missionTitle = getIntent().getStringExtra("mission_title");
        String missionDescription = getIntent().getStringExtra("mission_description");
        Double missionReward = getIntent().getDoubleExtra("mission_reward", 0);
        String imageLink = getIntent().getStringExtra("mission_imagelink");

        textViewMissionTitle.setText(missionTitle);
        if (TextUtils.isEmpty(missionDescription)){
            textViewMissionDescription.setVisibility(View.GONE);
        }
        else{
            textViewMissionDescription.setVisibility(View.VISIBLE);
            textViewMissionDescription.setText(missionDescription);
        }
        textViewMissionReward.setText(MyMethods.DoubleMethods.formatDoubleToString(missionReward));
        if (TextUtils.isEmpty(imageLink)){
            imageViewMissionImage.setVisibility(View.GONE);
        }
        else {
            imageViewMissionImage.setVisibility(View.VISIBLE);
            MyMethods.Generate.image(imageLink, imageViewMissionImage);
        }
    }

    private void startPomoTimer() {
        materialButtonStart.setVisibility(View.INVISIBLE);
        materialButtonPause.setVisibility(View.VISIBLE);
        // 20 mm = 1200000 ms
        countDownTimer = new CountDownTimer(10000, 100){ // 10 000 = 10 secs

            @Override
            public void onTick(long l) {
                textViewTimer.setText(String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(l),
                        TimeUnit.MILLISECONDS.toSeconds(l) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(l))));

                pomoTimerPausePosition = l;
                double progress = (Double.valueOf(String.valueOf(l)) / 10000)*5000;
                progressBarTimer.setProgress((int) progress);
            }

            @Override
            public void onFinish() {
                alertPomoFinish(); }
        }.start();
    }

    private void pausePomoTimer(){
        countDownTimer.cancel();
        materialButtonResume.setVisibility(View.VISIBLE);
        materialButtonPause.setVisibility(View.INVISIBLE);
    }

    private void resumePomoTimer(long timerPosition) {
        materialButtonResume.setVisibility(View.INVISIBLE);
        materialButtonPause.setVisibility(View.VISIBLE);
        // 20 mm = 1200000 ms
        countDownTimer = new CountDownTimer(timerPosition, 100){

            @Override
            public void onTick(long l) {
                textViewTimer.setText(String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(l),
                        TimeUnit.MILLISECONDS.toSeconds(l) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(l))));

                pomoTimerPausePosition = l;
                double progress = (Double.valueOf(String.valueOf(l)) / 10000 )*5000;
                progressBarTimer.setProgress((int) progress);
            }

            @Override
            public void onFinish() {
                alertPomoFinish();
            }
        }.start();
    }

    private void alertPomoFinish() {
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.alarm);
        mp.setLooping(true);
        mp.start();

        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            long[] pattern = {0, 100, 150, 300, 695}; // 550
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
        }
        else {
            vibrator.vibrate(500);
        }

        if (pomoProgress == 7){
            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(PomodoroActivity.this);
            materialAlertDialogBuilder.setTitle("Wow, you have completed 4 pomos!");
            materialAlertDialogBuilder.setPositiveButton("Take a long break", (dialogInterface, i) -> {
                mp.stop();
                vibrator.cancel();
                pomoProgress++;
                pomoPoints++;
                progressTracker();
            });
            materialAlertDialogBuilder.setOnCancelListener(dialogInterface -> {
                mp.stop();
                vibrator.cancel();
                pomoProgress++;
                pomoPoints++;
                progressTracker();
            });
            materialAlertDialogBuilder.show();
        }
        else {
            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(PomodoroActivity.this);
            materialAlertDialogBuilder.setTitle("Time to take a break");
            materialAlertDialogBuilder.setPositiveButton("Start break", (dialogInterface, i) -> {
                mp.stop();
                vibrator.cancel();
                pomoProgress++;
                pomoPoints++;
                progressTracker();
            });
            materialAlertDialogBuilder.setOnCancelListener(dialogInterface -> {
                mp.stop();
                vibrator.cancel();
                pomoProgress++;
                pomoPoints++;
                progressTracker();
            });
            materialAlertDialogBuilder.show();
        }
    }

    private void startBreakTimer() {
        materialButtonStart.setVisibility(View.INVISIBLE);
        materialButtonPause.setVisibility(View.VISIBLE);
        // 20 mm = 1200000 ms
        countDownTimer = new CountDownTimer(5000, 100){

            @Override
            public void onTick(long l) {
                textViewTimer.setText(String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(l),
                        TimeUnit.MILLISECONDS.toSeconds(l) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(l))));

                pomoTimerPausePosition = l;
                double progress = (Double.valueOf(String.valueOf(l)) / 5000)*5000;
                progressBarTimer.setProgress((int) progress);
            }

            @Override
            public void onFinish() {
                alertBreakFinish();
            }
        }.start();
    }

    private void startLongBreakTimer() {
        materialButtonStart.setVisibility(View.INVISIBLE);
        materialButtonPause.setVisibility(View.VISIBLE);
        // 20 mm = 1200000 ms
        countDownTimer = new CountDownTimer(5000, 100){

            @Override
            public void onTick(long l) {
                textViewTimer.setText(String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(l),
                        TimeUnit.MILLISECONDS.toSeconds(l) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(l))));

                pomoTimerPausePosition = l;
                double progress = (Double.valueOf(String.valueOf(l)) / 5000)*5000;
                progressBarTimer.setProgress((int) progress);
            }

            @Override
            public void onFinish() {
                alertLongBreakFinish();
            }
        }.start();
    }

    private void resumeBreakTimer(long timerPosition) {
        materialButtonResume.setVisibility(View.INVISIBLE);
        materialButtonPause.setVisibility(View.VISIBLE);
        // 20 mm = 1200000 ms
        countDownTimer = new CountDownTimer(timerPosition, 100){

            @Override
            public void onTick(long l) {
                textViewTimer.setText(String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(l),
                        TimeUnit.MILLISECONDS.toSeconds(l) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(l))));

                pomoTimerPausePosition = l;
                double progress = (Double.valueOf(String.valueOf(l)) / 5000)*5000;
                progressBarTimer.setProgress((int) progress);
            }

            @Override
            public void onFinish() {
                alertBreakFinish();
            }
        }.start();
    }

    private void alertBreakFinish() {
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.alarm);
        mp.setLooping(true);
        mp.start();

        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            long[] pattern = {0, 100, 150, 300, 695};
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
        }
        else {
            vibrator.vibrate(500);
        }

        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(PomodoroActivity.this);
        materialAlertDialogBuilder.setTitle("Ready to get back to work?");
        materialAlertDialogBuilder.setPositiveButton("Start pomo", (dialogInterface, i) -> {
            mp.stop();
            vibrator.cancel();
            pomoProgress++;
            pomoPoints++;
            progressTracker();
        });
        materialAlertDialogBuilder.setOnCancelListener(dialogInterface -> {
            mp.stop();
            vibrator.cancel();
            pomoProgress++;
            pomoPoints++;
            progressTracker();
        });
        materialAlertDialogBuilder.show();
    }

    private void alertLongBreakFinish() {
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.alarm);
        mp.setLooping(true);
        mp.start();

        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            long[] pattern = {0, 100, 150, 300, 695};
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
        }
        else {
            vibrator.vibrate(500);
        }

        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(PomodoroActivity.this);
        materialAlertDialogBuilder.setTitle("Ready to get back to work?");
        materialAlertDialogBuilder.setPositiveButton("Start pomo", (dialogInterface, i) -> {
            mp.stop();
            vibrator.cancel();
            pomoProgress = 0;
            imageViewPomoProgress1.setImageDrawable(getDrawable(R.drawable.ic_outline_circle_24));
            imageViewPomoProgress2.setImageDrawable(getDrawable(R.drawable.ic_outline_circle_24));
            imageViewPomoProgress3.setImageDrawable(getDrawable(R.drawable.ic_outline_circle_24));
            imageViewPomoProgress4.setImageDrawable(getDrawable(R.drawable.ic_outline_circle_24));
            progressTracker();
            startPomoTimer();
        });
        materialAlertDialogBuilder.setOnCancelListener(dialogInterface -> {
            mp.stop();
            vibrator.cancel();
            pomoProgress = 0;
            imageViewPomoProgress1.setImageDrawable(getDrawable(R.drawable.ic_outline_circle_24));
            imageViewPomoProgress2.setImageDrawable(getDrawable(R.drawable.ic_outline_circle_24));
            imageViewPomoProgress3.setImageDrawable(getDrawable(R.drawable.ic_outline_circle_24));
            imageViewPomoProgress4.setImageDrawable(getDrawable(R.drawable.ic_outline_circle_24));
            progressTracker();
            startPomoTimer();
        });
        materialAlertDialogBuilder.show();
    }

    private void progressTracker() {
        ConstraintSet constraintSet = new ConstraintSet();
        switch (pomoProgress){
            case 0:
                constraintSet.clone(constraintLayoutPomodoroActivityParent);
                constraintSet.connect(R.id.textViewPomoLabel, ConstraintSet.START, R.id.imageViewPomoProgress1, ConstraintSet.START, 0);
                constraintSet.connect(R.id.textViewPomoLabel, ConstraintSet.END, R.id.imageViewPomoProgress1, ConstraintSet.END, 0);
                constraintSet.applyTo(constraintLayoutPomodoroActivityParent);
                textViewPomoLabel.setText("Focus");
                break;
            case 1:
                constraintSet.clone(constraintLayoutPomodoroActivityParent);
                constraintSet.connect(R.id.textViewPomoLabel, ConstraintSet.START, R.id.imageViewPomoProgress1, ConstraintSet.START, 0);
                constraintSet.connect(R.id.textViewPomoLabel, ConstraintSet.END, R.id.imageViewPomoProgress1, ConstraintSet.END, 0);
                constraintSet.applyTo(constraintLayoutPomodoroActivityParent);
                textViewPomoLabel.setText("Break");

                imageViewPomoProgress1.setImageDrawable(getDrawable(R.drawable.ic_baseline_circle_24));
                startBreakTimer();
                break;
            case 2:
                constraintSet.clone(constraintLayoutPomodoroActivityParent);
                constraintSet.connect(R.id.textViewPomoLabel, ConstraintSet.START, R.id.imageViewPomoProgress2, ConstraintSet.START, 0);
                constraintSet.connect(R.id.textViewPomoLabel, ConstraintSet.END, R.id.imageViewPomoProgress2, ConstraintSet.END, 0);
                constraintSet.applyTo(constraintLayoutPomodoroActivityParent);
                textViewPomoLabel.setText("Focus");

                imageViewPomoProgress1.setImageDrawable(getDrawable(R.drawable.ic_baseline_check_circle_24));
                startPomoTimer();
                break;
            case 3:
                constraintSet.clone(constraintLayoutPomodoroActivityParent);
                constraintSet.connect(R.id.textViewPomoLabel, ConstraintSet.START, R.id.imageViewPomoProgress2, ConstraintSet.START, 0);
                constraintSet.connect(R.id.textViewPomoLabel, ConstraintSet.END, R.id.imageViewPomoProgress2, ConstraintSet.END, 0);
                constraintSet.applyTo(constraintLayoutPomodoroActivityParent);
                textViewPomoLabel.setText("Break");

                imageViewPomoProgress2.setImageDrawable(getDrawable(R.drawable.ic_baseline_circle_24));
                startBreakTimer();
                break;
            case 4:
                constraintSet.clone(constraintLayoutPomodoroActivityParent);
                constraintSet.connect(R.id.textViewPomoLabel, ConstraintSet.START, R.id.imageViewPomoProgress3, ConstraintSet.START, 0);
                constraintSet.connect(R.id.textViewPomoLabel, ConstraintSet.END, R.id.imageViewPomoProgress3, ConstraintSet.END, 0);
                constraintSet.applyTo(constraintLayoutPomodoroActivityParent);
                textViewPomoLabel.setText("Focus");

                imageViewPomoProgress2.setImageDrawable(getDrawable(R.drawable.ic_baseline_check_circle_24));
                startPomoTimer();
                break;
            case 5:
                constraintSet.clone(constraintLayoutPomodoroActivityParent);
                constraintSet.connect(R.id.textViewPomoLabel, ConstraintSet.START, R.id.imageViewPomoProgress3, ConstraintSet.START, 0);
                constraintSet.connect(R.id.textViewPomoLabel, ConstraintSet.END, R.id.imageViewPomoProgress3, ConstraintSet.END, 0);
                constraintSet.applyTo(constraintLayoutPomodoroActivityParent);
                textViewPomoLabel.setText("Break");

                imageViewPomoProgress3.setImageDrawable(getDrawable(R.drawable.ic_baseline_circle_24));
                startBreakTimer();
                break;
            case 6:
                constraintSet.clone(constraintLayoutPomodoroActivityParent);
                constraintSet.connect(R.id.textViewPomoLabel, ConstraintSet.START, R.id.imageViewPomoProgress4, ConstraintSet.START, 0);
                constraintSet.connect(R.id.textViewPomoLabel, ConstraintSet.END, R.id.imageViewPomoProgress4, ConstraintSet.END, 0);
                constraintSet.applyTo(constraintLayoutPomodoroActivityParent);
                textViewPomoLabel.setText("Focus");

                imageViewPomoProgress3.setImageDrawable(getDrawable(R.drawable.ic_baseline_check_circle_24));
                startPomoTimer();
                break;
            case 7:
                constraintSet.clone(constraintLayoutPomodoroActivityParent);
                constraintSet.connect(R.id.textViewPomoLabel, ConstraintSet.START, R.id.imageViewPomoProgress4, ConstraintSet.START, 0);
                constraintSet.connect(R.id.textViewPomoLabel, ConstraintSet.END, R.id.imageViewPomoProgress4, ConstraintSet.END, 0);
                constraintSet.applyTo(constraintLayoutPomodoroActivityParent);
                textViewPomoLabel.setText("Long Break");

                imageViewPomoProgress4.setImageDrawable(getDrawable(R.drawable.ic_baseline_circle_24));
                startLongBreakTimer();
                break;
        }
    }

    private void finishMission() {
        // get mission reward
        Double reward = getIntent().getDoubleExtra("mission_reward", 0);
        Double gold = MyMethods.Cache.getDouble(getApplicationContext(), "gold");
        Double updatedBalance = gold + reward;
        MyMethods.Cache.setDouble(getApplicationContext(), "gold", updatedBalance);
        hearthDB.getReference("user_"+MyMethods.Cache.getString(getApplicationContext(), "uid")+"_gold").setValue(updatedBalance);

        // calculate xp earned
        int xp = MyMethods.Cache.getInt(getApplicationContext(), "xp");
        int baseXp = (pomoPoints + 1);
        int generatedXp = MyMethods.Generate.randomNumber(4, 9);
        int earnedXp = baseXp * generatedXp;
        int updatedXp = earnedXp + xp;

        // check if xp earned is enough to level up
        int currentLvl = xp / 100;
        int updatedLvl = updatedXp / 100;

        hearthDB.getReference("user_"+MyMethods.Cache.getString(getApplicationContext(), "uid")+"_xp").setValue(updatedXp);
        MyMethods.Cache.setInt(getApplicationContext(), "xp", updatedXp);

        Toast.makeText(getApplicationContext(), "+"+reward+" Gold", Toast.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(), "+"+earnedXp+" Experience", Toast.LENGTH_SHORT).show();

        leaderboardReference = hearthDB.getReference("leaderboards/family_"+familycode);
        leaderboardReference.child("score").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer currentValue = currentData.getValue(Integer.class);
                if (currentValue == null){
                    currentData.setValue(earnedXp);
                }
                else {
                    currentData.setValue(currentValue + earnedXp);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                System.out.println("Transaction completed");
            }
        });

        // hide current completed mission
        hearthDB.getReference("user_"+currentUserId+"_missions_completed").push().setValue(missionUid);

        // log completed mission
        String missionTitle = getIntent().getStringExtra("mission_title");
        newMissionLogRef = hearthDB.getReference("family_"+familycode+"_missionlogs").push();
        newMissionLogRef.child("memberUid").setValue(currentUserId);
        newMissionLogRef.child("missionTitle").setValue(missionTitle);
        newMissionLogRef.child("timestamp").setValue(ServerValue.TIMESTAMP);

        if (updatedLvl > currentLvl){
            Intent lvlupIntent = new Intent(PomodoroActivity.this, MissionsActivity.class);
            lvlupIntent.putExtra("didLvlUp", true);
            lvlupIntent.putExtra("currentLvl", currentLvl);
            lvlupIntent.putExtra("updatedLvl", updatedLvl);
            lvlupIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(lvlupIntent);
            finish();
        }
        else {
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.zoom_out_enter, R.anim.zoom_out_exit);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (countDownTimer!=null){
            countDownTimer.cancel();
        }
    }
}
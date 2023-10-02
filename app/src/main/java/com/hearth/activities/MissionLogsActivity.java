package com.hearth.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hearth.R;
import com.hearth.adapters.LeaderboardAdapter;
import com.hearth.adapters.MissionLogAdapter;
import com.hearth.methods.MyMethods;
import com.hearth.objects.Leaderboard;
import com.hearth.objects.Mission;
import com.hearth.objects.MissionLog;

import java.util.ArrayList;
import java.util.Collections;

public class MissionLogsActivity extends AppCompatActivity implements MissionLogAdapter.OnMissionLogListener {

    FirebaseDatabase hearthDB = FirebaseDatabase.getInstance();
    DatabaseReference missionLogsRef;
    ValueEventListener missionLogsListener;

    MaterialButton btnBack;

    RecyclerView rvMissionLogs;
    MissionLogAdapter missionLogAdapter;
    ArrayList<MissionLog> missionLogArrayList;
    MissionLogAdapter.OnMissionLogListener onMissionLogListener = this;

    Context context;
    String familyCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission_logs);

        initializeVars();
        populateRecyclerView();

        btnBack.setOnClickListener(view -> {
            finish();
            overridePendingTransition(R.anim.zoom_out_enter, R.anim.zoom_out_exit);
        });
    }

    private void initializeVars() {
        btnBack = findViewById(R.id.btnBack);
        missionLogsRef = hearthDB.getReference();
        context = this;
        familyCode = MyMethods.Cache.getString(getApplicationContext(), "familycode");
    }

    private void populateRecyclerView() {
        rvMissionLogs = findViewById(R.id.rvMissionLogs);
        rvMissionLogs.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        rvMissionLogs.setLayoutManager(linearLayoutManager);

        missionLogArrayList = new ArrayList<>();
        missionLogAdapter = new MissionLogAdapter(context, missionLogArrayList, onMissionLogListener);
        rvMissionLogs.setAdapter(missionLogAdapter);

        missionLogsRef = hearthDB.getReference("family_"+familyCode+"_missionlogs");
        missionLogsListener = missionLogsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                missionLogArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    MissionLog missionLog = dataSnapshot.getValue(MissionLog.class);
                    missionLogArrayList.add(missionLog);
                }
                Collections.reverse(missionLogArrayList);
                missionLogAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public void onMissionLogClick(int position) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        missionLogsRef.removeEventListener(missionLogsListener);
    }
}
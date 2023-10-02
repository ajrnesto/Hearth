package com.hearth.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hearth.R;
import com.hearth.adapters.LeaderboardAdapter;
import com.hearth.methods.MyMethods;
import com.hearth.objects.Leaderboard;

import java.util.ArrayList;
import java.util.Collections;

public class LeaderboardsActivity extends AppCompatActivity implements LeaderboardAdapter.OnLeaderboardListener {

    FirebaseDatabase hearthDB = FirebaseDatabase.getInstance();
    DatabaseReference leaderboardReference;

    RecyclerView recyclerViewLeaderboards;
    LeaderboardAdapter leaderboardAdapter;
    ArrayList<Leaderboard> leaderboardArrayList;
    LeaderboardAdapter.OnLeaderboardListener onLeaderboardListener = this;

    MaterialButton buttonClose;
    String familycode;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboards);

        context = this;
        familycode = MyMethods.Cache.getString(getApplicationContext(), "familycode");
        buttonClose = findViewById(R.id.materialButtonClose);
        buttonClose.setOnClickListener(view -> {
            finish();
        });
        
        populateRecyclerView();
    }

    private void populateRecyclerView() {
        recyclerViewLeaderboards = findViewById(R.id.recyclerViewLeaderboards);
        recyclerViewLeaderboards.setHasFixedSize(true);
        recyclerViewLeaderboards.setLayoutManager(new LinearLayoutManager(context));

        leaderboardArrayList = new ArrayList<>();
        leaderboardAdapter = new LeaderboardAdapter(context, leaderboardArrayList, onLeaderboardListener);
        recyclerViewLeaderboards.setAdapter(leaderboardAdapter);

        leaderboardReference = hearthDB.getReference("leaderboards");
        leaderboardReference.orderByChild("score").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                leaderboardArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Leaderboard leaderboard = dataSnapshot.getValue(Leaderboard.class);
                    leaderboardArrayList.add(leaderboard);
                }
                Collections.reverse(leaderboardArrayList);
                leaderboardAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public void onLeaderboardClick(int position) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.zoom_out_enter, R.anim.zoom_out_exit);
    }
}
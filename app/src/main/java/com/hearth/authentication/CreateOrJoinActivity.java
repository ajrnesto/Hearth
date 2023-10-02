package com.hearth.authentication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.hearth.R;

public class CreateOrJoinActivity extends AppCompatActivity {

    private Button buttonCreate;
    private Button buttonJoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_or_join);

        // Start of declaration of buttons
        buttonCreate = findViewById(R.id.buttonCreate);
        buttonJoin = findViewById(R.id.buttonJoin);
        // End of declaration of buttons



        // Start of buttonCreate handler
        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentCreateActivity = new Intent(CreateOrJoinActivity.this, CreateFamilyActivity.class);
                CreateOrJoinActivity.this.startActivity(intentCreateActivity);
            }
        });
        // End of buttonCreate handler

        // Start of buttonJoin handler
        buttonJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentJoinActivity = new Intent(CreateOrJoinActivity.this, JoinFamilyActivity.class);
                CreateOrJoinActivity.this.startActivity(intentJoinActivity);
            }
        });
        // End of buttonJoin handler
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        FirebaseAuth.getInstance().signOut();
    }
}
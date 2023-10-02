package com.hearth.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.hearth.R;
import com.hearth.authentication.LoginActivity;
import com.hearth.methods.MyMethods;
import com.makeramen.roundedimageview.RoundedImageView;

public class MenuActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase hearthDB = FirebaseDatabase.getInstance();

    private TextView textViewFullName, textViewEmail, textViewActivityTitle, textViewLVL;
    private ProgressBar xpBar;
    private MaterialButton buttonLogout, buttonEditProfile;
    private ImageView backButton;
    RoundedImageView imgProfilePicture;
    Switch switchCompletedMissions;

    String currentUserId;

    @Override
    protected void onResume() {
        super.onResume();
        prefillInformation();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        initializeVars();
        textViewActivityTitle.setText("Menu");

        currentUserId = MyMethods.Cache.getString(getApplicationContext(), "uid");

        // Start of Personal Details population
        prefillInformation();
        // End of Personal Details population

        // Start - switch completed missions handler
        /*int switchCompletedMissionsState = MyMethods.Cache.getInt(getApplicationContext(), currentUserId+"show_completed_missions_state");
        if (switchCompletedMissionsState == 0){
            switchCompletedMissions.setChecked(false);
        }
        else {
            switchCompletedMissions.setChecked(true);
        }
        switchCompletedMissions.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b == true){
                MyMethods.Cache.setInt(getApplicationContext(), currentUserId+"show_completed_missions_state", 0);
            }
            else {
                MyMethods.Cache.setInt(getApplicationContext(), currentUserId+"show_completed_missions_state", 1);
            }
        });*/
        // End - switch completed missions handler

        buttonEditProfile.setOnClickListener(view -> {
            startActivity(new Intent(MenuActivity.this, EditProfileActivity.class));
            overridePendingTransition(R.anim.zoom_in_enter, R.anim.zoom_in_exit);
        });

        // Start - Log out button handler
        buttonLogout.setOnClickListener(view -> {

            // alert dialog
            MaterialAlertDialogBuilder alertLogout = new MaterialAlertDialogBuilder(MenuActivity.this);
            alertLogout.setTitle("Signing out");
            alertLogout.setMessage("Did you want to log out?");
            alertLogout.setOnCancelListener(dialogInterface -> {
                return;
            });
            alertLogout.setNegativeButton("Log out", (dialogInterface, i) -> {
                // clear all previous activities and then open the main activity
                FirebaseAuth.getInstance().signOut();
                Intent intentLoginActivity = new Intent(MenuActivity.this, LoginActivity.class);
                intentLoginActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                MenuActivity.this.startActivity(intentLoginActivity);
            });
            alertLogout.setNeutralButton("Cancel", (dialogInterface, i) -> {
                return;
            });
            alertLogout.show();
        });
        // End - Log out button handler

        // Start - Back button handler
        backButton.setImageDrawable(getDrawable(R.drawable.outline_arrow_back_ios_24));
        backButton.setOnClickListener(view -> finish());
        // End - Back button handler
    }

    private void prefillInformation() {
        String fullname = MyMethods.Cache.getString(getApplicationContext(), "firstname") + " " + MyMethods.Cache.getString(MenuActivity.this, "familyname");
        String email = MyMethods.Cache.getString(getApplicationContext(), "email");
        String photourl = MyMethods.Cache.getString(getApplicationContext(), "photourl");
        int xp = MyMethods.Cache.getInt(getApplicationContext(), "xp");
        int lvl = (xp/100);
        int progress = xp-(lvl*100);

        textViewFullName.setText(fullname);
        textViewEmail.setText(email);
        textViewLVL.setText("LVL "+(lvl+1));
        xpBar.setProgress(progress);
        MyMethods.Generate.image(photourl, imgProfilePicture);
    }

    private void initializeVars() {
        textViewFullName = findViewById(R.id.textViewFamilyName);
        textViewEmail = findViewById(R.id.textViewCode);
        textViewLVL = findViewById(R.id.textViewLVL);
        xpBar = findViewById(R.id.xpBar);
        buttonLogout = findViewById(R.id.buttonLogout);
        backButton = findViewById(R.id.imageViewButton);
        buttonEditProfile = findViewById(R.id.buttonEditProfile);
        switchCompletedMissions = findViewById(R.id.switchCompletedMissions);
        textViewActivityTitle = findViewById(R.id.textViewActivityTitle);
        imgProfilePicture = findViewById(R.id.imgProfilePicture);
    }

    @Override
    public void onPause(){
        super.onPause();
        overridePendingTransition(R.anim.no_animation, R.anim.slide_out_left);
    }
}
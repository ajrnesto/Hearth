package com.hearth.authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hearth.R;
import com.hearth.activities.MenuActivity;
import com.hearth.methods.MyMethods;
import com.makeramen.roundedimageview.RoundedImageView;

public class RequestJoinActivity extends AppCompatActivity {

    FirebaseDatabase hearthDB = FirebaseDatabase.getInstance();
    DatabaseReference userRequestRef, familyRequestRef, familyRef, familyJoinRequestsRef;
    ValueEventListener familyJoinRequestsListener;

    TextView tvUserName, tvUserEmail, tvFamilyName;
    RoundedImageView imgUserPhoto, imgFamilyIcon;
    MaterialButton btnLogout, btnCancel;

    String currentUserUid, familyCode, userName, userEmail, userPhotoUrl, familyName, familyPhotoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_join);

            initializeVars();
            displayUserInfo();
            displayFamilyInfo();
            responseListener();

            btnLogout.setOnClickListener(view -> {
                logout();
            });

            btnCancel.setOnClickListener(view -> {
                cancelJoinRequest();
            });
    }

    private void initializeVars() {
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvFamilyName = findViewById(R.id.tvFamilyName);
        imgUserPhoto = findViewById(R.id.imgUserPhoto);
        imgFamilyIcon = findViewById(R.id.imgFamilyIcon);
        btnLogout = findViewById(R.id.btnLogout);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void displayUserInfo() {
        // retrieve user info
        currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userName = MyMethods.Cache.getString(this, "firstname") + " " + MyMethods.Cache.getString(this, "familyname");
        userPhotoUrl = MyMethods.Cache.getString(this, "photourl");
        userEmail = MyMethods.Cache.getString(this, "email");
        // display user info
        tvUserName.setText(userName);
        tvUserEmail.setText(userEmail);
        MyMethods.Generate.image(userPhotoUrl, imgUserPhoto);
    }

    private void displayFamilyInfo() {
        familyCode = MyMethods.Cache.getString(getApplicationContext(), "familycode");
        familyRef = hearthDB.getReference("family_"+familyCode);
        familyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                familyName = snapshot.child("familyname").getValue().toString();
                tvFamilyName.setText(familyName);

                familyPhotoUrl = snapshot.child("photourl").getValue().toString();
                MyMethods.Generate.image(familyPhotoUrl, imgFamilyIcon);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void logout() {
        MaterialAlertDialogBuilder alertLogout = new MaterialAlertDialogBuilder(RequestJoinActivity.this);
        alertLogout.setTitle("Signing out");
        alertLogout.setMessage("Did you want to log out?");
        alertLogout.setOnCancelListener(dialogInterface -> {
            return;
        });
        alertLogout.setNegativeButton("Log out", (dialogInterface, i) -> {
            FirebaseAuth.getInstance().signOut();
            Intent intentLoginActivity = new Intent(RequestJoinActivity.this, LoginActivity.class);
            intentLoginActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            RequestJoinActivity.this.startActivity(intentLoginActivity);
        });
        alertLogout.setNeutralButton("Cancel", (dialogInterface, i) -> {
            return;
        });
        alertLogout.show();
    }

    private void cancelJoinRequest() {
        MaterialAlertDialogBuilder cancelDialog = new MaterialAlertDialogBuilder(RequestJoinActivity.this);
        cancelDialog.setTitle("Cancel Join Request?");
        cancelDialog.setMessage("Did you want to cancel your join request to "+tvFamilyName.getText().toString()+"?");
        cancelDialog.setNegativeButton("Cancel Request", (dialogInterface, i) -> {
            userRequestRef = hearthDB.getReference("user_"+currentUserUid+"_joinrequest");
            userRequestRef.removeValue();

            familyRequestRef = hearthDB.getReference("family_"+familyCode+"_joinrequests").child("user_"+currentUserUid);
            familyRequestRef.removeValue();

            Intent loginIntent = new Intent(RequestJoinActivity.this, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(loginIntent);
            finish();
        });
        cancelDialog.setNeutralButton("Cancel", (dialogInterface, i) -> { });
        cancelDialog.setOnCancelListener(dialogInterface -> { });
        cancelDialog.show();
    }

    private void responseListener() {
        familyJoinRequestsRef = hearthDB.getReference("family_"+familyCode+"_joinrequests").child("user_"+currentUserUid);
        familyJoinRequestsListener = familyJoinRequestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    hearthDB.getReference("user_"+currentUserUid).child("familycode").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                MyMethods.Notifications.membership(RequestJoinActivity.this, 1, familyName);
                                MaterialAlertDialogBuilder acceptedDialog = new MaterialAlertDialogBuilder(RequestJoinActivity.this);
                                acceptedDialog.setTitle("Join request accepted");
                                acceptedDialog.setMessage("Your request to join the "+familyName+" family has been accepted!\nTap on Proceed to start your Hearth journey!");
                                acceptedDialog.setPositiveButton("Proceed", (dialogInterface, i) -> {
                                    startActivity(new Intent(RequestJoinActivity.this, LoginActivity.class));
                                    finish();
                                });
                                acceptedDialog.setOnCancelListener(dialogInterface -> {
                                    startActivity(new Intent(RequestJoinActivity.this, LoginActivity.class));
                                    finish();
                                });
                                if (!RequestJoinActivity.this.isFinishing()){
                                    acceptedDialog.show();
                                }
                            }
                            else {
                                MyMethods.Notifications.membership(RequestJoinActivity.this, 0, familyName);
                                MaterialAlertDialogBuilder deniedDialog = new MaterialAlertDialogBuilder(RequestJoinActivity.this);
                                deniedDialog.setTitle("Join request denied");
                                deniedDialog.setMessage("We're sorry to inform that your request to join the "+familyName+" family has been denied.");
                                deniedDialog.setPositiveButton("Go back", (dialogInterface, i) -> {
                                    startActivity(new Intent(RequestJoinActivity.this, LoginActivity.class));
                                    finish();
                                });
                                deniedDialog.setOnCancelListener(dialogInterface -> {
                                    startActivity(new Intent(RequestJoinActivity.this, LoginActivity.class));
                                    finish();
                                });
                                if (!RequestJoinActivity.this.isFinishing()){
                                    deniedDialog.show();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        familyJoinRequestsRef.removeEventListener(familyJoinRequestsListener);
    }
}
package com.hearth.authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hearth.methods.MyMethods;
import com.hearth.R;

import java.util.ArrayList;

public class CreateFamilyActivity extends AppCompatActivity {

    // Start of variable initialization
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseDatabase hearthDB = FirebaseDatabase.getInstance();
    DatabaseReference familyReference, currentUserReference, newFamilyReference, membersReference, leaderboardsReference;
    String currentUserID = firebaseAuth.getCurrentUser().getUid();

    private TextInputEditText textInputEditTextHearthFamilyName;
    private TextView textViewFamilyCode;
    private MaterialButton buttonCopy, buttonCreate;
    private int isCodeUnique;
    // End of variable initialization

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_family);

        // Start of declaration of variables
        textInputEditTextHearthFamilyName = findViewById(R.id.textInputEditTextHearthFamilyName);
        textViewFamilyCode = findViewById(R.id.textViewFamilyCode);
        buttonCopy = findViewById(R.id.buttonCopy);
        buttonCreate = findViewById(R.id.buttonCreate);
        isCodeUnique = 0;
        // End of declaration of variables

        // Start of code generation
        do{
            // generate and display code
            textViewFamilyCode.setText(MyMethods.Generate.familyCode());

            // check if code is unique
            String code = textViewFamilyCode.getText().toString();
            familyReference = hearthDB.getReference("family_"+code);
            familyReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean codeExists = snapshot.child(code).exists();
                    if (codeExists){
                        isCodeUnique++;
                    }
                    else{
                        isCodeUnique--;
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
        }
        while(isCodeUnique >= 1);
        // End of code generation

        // Start - Get user's family name
        currentUserReference = hearthDB.getReference("user_"+currentUserID).child("familyname");
        currentUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String familyname = snapshot.getValue().toString();
                textInputEditTextHearthFamilyName.setText(familyname);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        // End - Get user's family name

        // Start of copy button handler
        buttonCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData;
                clipData = ClipData.newPlainText("Family Code", textViewFamilyCode.getText());
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(getApplicationContext(), "Copied Family Code to Clipboard", Toast.LENGTH_LONG).show();
            }
        });
        // End of copy button handler

        // Start of create button handler
        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(textInputEditTextHearthFamilyName.getText())){
                    // run this code if user did not enter a family name
                    MyMethods.Generate.warningDialog(CreateFamilyActivity.this, "Failed to Create new Family", "Please fill out all fields");
                }
                else{
                    // Start - Create Family
                    // get family code and name
                    String code = textViewFamilyCode.getText().toString();
                    String name = textInputEditTextHearthFamilyName.getText().toString().trim();
                    String[] nameArray = name.split(" ");
                    StringBuilder nameStringBuilder = new StringBuilder();
                    for (String s : nameArray) {
                        String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
                        nameStringBuilder.append(cap).append(" ");
                    }
                    // add family code to current user
                    hearthDB.getReference("user_"+currentUserID).child("familycode").setValue(code);

                    // create new family branch
                    newFamilyReference = hearthDB.getReference("family_"+code);
                    newFamilyReference.child("familycode").setValue(code);
                    newFamilyReference.child("familyname").setValue(nameStringBuilder.toString().trim());
                    String photoUrl = "https://4.bp.blogspot.com/-Xd7h725VGgk/U9zsrHl7QxI/AAAAAAAAkeg/c3ecwYFH3Qs/s1600/hoka_05_question.png";
                    newFamilyReference.child("photourl").setValue(photoUrl);

                    // create new leaderboards branch
                    leaderboardsReference = hearthDB.getReference("leaderboards/family_"+code);
                    leaderboardsReference.child("familyname").setValue(nameStringBuilder.toString().trim());
                    leaderboardsReference.child("score").setValue(0);
                    leaderboardsReference.child("score").setValue(0);
                    leaderboardsReference.child("photourl").setValue(photoUrl);

                    // create new members branch
                    membersReference = hearthDB.getReference("family_"+code+"_members/user_"+currentUserID);
                    membersReference.child("uid").setValue(currentUserID);
                    membersReference.child("role").setValue("Admin");
                    membersReference.child("photourl").setValue("https://2.bp.blogspot.com/-ZwYKR5Zu28s/U6Qo2qAjsqI/AAAAAAAAhkM/HkbDZEJwvPs/s400/omocha_robot.png");
                    // get firstname and familyname, append, then set to fullname
                    hearthDB.getReference("user_"+currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String currentUserFirstName = snapshot.child("firstname").getValue().toString().trim();
                            String currentUserFamilyName = snapshot.child("familyname").getValue().toString().trim();

                            membersReference.child("fullname").setValue(currentUserFirstName + " " + currentUserFamilyName);

                            // Start - Proceed to missions page
                            Toast.makeText(CreateFamilyActivity.this, "Creating new family...", Toast.LENGTH_SHORT).show();
                            Intent loginIntent = new Intent(CreateFamilyActivity.this, LoginActivity.class);
                            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(loginIntent);
                            finish();
                            // End - Proceed to missions page
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                    // End - Create Family
                }
            }
        });
        // End of create button handler
    }
}
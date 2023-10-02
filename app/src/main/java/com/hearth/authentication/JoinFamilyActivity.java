package com.hearth.authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hearth.R;
import com.hearth.methods.MyMethods;

public class JoinFamilyActivity extends AppCompatActivity {

    // Start of variable initialization
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    String currentUserUID = firebaseAuth.getCurrentUser().getUid();
    FirebaseDatabase hearthDB = FirebaseDatabase.getInstance();
    private DatabaseReference currentUserRef, currentFamilyRef, currentFamilyMembersRef, familyJoinRequestRef;

    private TextInputEditText textInputEditTextFamilyCode;
    private TextInputLayout textInputLayoutFamilyCode;
    private Button buttonJoinFamily;
    // End of variable initialization

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_family);

        // Start of declaration of variables
        textInputLayoutFamilyCode = findViewById(R.id.textInputLayoutFamilyCode);
        textInputEditTextFamilyCode = findViewById(R.id.textInputEditTextFamilyCode);
        buttonJoinFamily = findViewById(R.id.buttonJoinFamily);
        // End of declaration of variables

        // Start of join button handler
        buttonJoinFamily.setOnClickListener(view -> {
            joinFamily();
        });
        // End of join button handler

        textInputEditTextFamilyCode.setOnKeyListener((view, i, keyEvent) -> {
            textInputLayoutFamilyCode.setError(null);
            return false;
        });
    }

    private void joinFamily() {
        String code = textInputEditTextFamilyCode.getText().toString();
        // check if user has not left the edittext as empty
        if (TextUtils.isEmpty(textInputEditTextFamilyCode.getText())){
            MyMethods.Generate.warningDialog(JoinFamilyActivity.this, "Failed to Join Family", "Family code could not be left empty");
            return;
        }

        // else, proceed to check if the entered family code exists
        currentFamilyRef = hearthDB.getReference("family_"+code);
        currentFamilyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean codeExists = snapshot.exists();
                if (codeExists){
                    sendJoinRequest(code);
                    /*currentFamilyMembersRef = hearthDB.getReference("family_"+code+"_members/user_"+currentUserUID);
                    currentFamilyMembersRef.child("role").setValue("Member");
                    currentFamilyMembersRef.child("uid").setValue(currentUserUID);
                    currentFamilyMembersRef.child("photourl").setValue("https://2.bp.blogspot.com/-ZwYKR5Zu28s/U6Qo2qAjsqI/AAAAAAAAhkM/HkbDZEJwvPs/s400/omocha_robot.png");

                    // get current user reference
                    currentUserRef = hearthDB.getReference("user_"+currentUserUID);
                    // set user's familycode
                    currentUserRef.child("familycode").setValue(code);
                    currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String currentUserFirstName = snapshot.child("firstname").getValue().toString().trim();
                            String currentUserFamilyName = snapshot.child("familyname").getValue().toString().trim();

                            // set Families/$familyCode/Member/FullName: $currentUser.FirstName + $currentUser.FamilyName
                            currentFamilyMembersRef.child("fullname").setValue(currentUserFirstName + " " + currentUserFamilyName);

                            // Start - Proceed to missions page
                            Toast.makeText(JoinFamilyActivity.this, "Joining family...", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(JoinFamilyActivity.this, LoginActivity.class));
                            finish();
                            // End - Proceed to missions page
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });*/
                }
                else{
                    textInputLayoutFamilyCode.setError("The code does not exist");
                }
            }

            private void sendJoinRequest(String familyCode) {
                hearthDB.getReference("user_"+currentUserUID+"_joinrequest").setValue(familyCode);

                currentUserRef = hearthDB.getReference("user_"+currentUserUID);
                currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String photourl = snapshot.child("photourl").getValue().toString();
                        String firstname = snapshot.child("firstname").getValue().toString();
                        String familyname = snapshot.child("familyname").getValue().toString();
                        String fullname = firstname + " " + familyname;

                        familyJoinRequestRef = hearthDB.getReference("family_"+familyCode+"_joinrequests").child("user_"+currentUserUID);
                        familyJoinRequestRef.child("fullname").setValue(fullname);
                        familyJoinRequestRef.child("photourl").setValue(photourl);
                        familyJoinRequestRef.child("uid").setValue(currentUserUID);

                        Toast.makeText(JoinFamilyActivity.this, "Sending join request...", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(JoinFamilyActivity.this, LoginActivity.class));
                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        //
    }
}
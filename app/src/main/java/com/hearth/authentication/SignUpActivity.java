package com.hearth.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hearth.methods.MyMethods;
import com.hearth.R;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    // Start of variable initialization
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase hearthDB = FirebaseDatabase.getInstance();
    DatabaseReference newUserReference;

    private ImageView imageViewSignup;
    private TextView textViewTheFlameTheBrings2, textViewFamilyTogether2;
    private CircularProgressIndicator circularProgressSignup;
    public TextInputEditText textInputEditTextFirstName, textInputEditTextFamilyName, textInputEditTextEmailAddress, textInputEditTextPassword;
    private TextInputLayout textInputLayoutFirstName, textInputLayoutFamilyName, textInputLayoutEmailAddress, textInputLayoutPassword;
    private MaterialButton buttonSignUp, buttonLogin;
    // End of variable initialization

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Declare Android Widgets
        imageViewSignup = findViewById(R.id.imageViewSignup);
        textViewTheFlameTheBrings2 = findViewById(R.id.textViewTheFlameTheBrings2);
        textViewFamilyTogether2 = findViewById(R.id.textViewFamilyTogether2);
        textInputEditTextFirstName = findViewById(R.id.textInputEditTextFirstName);
        circularProgressSignup = findViewById(R.id.circularProgressSignup);
        textInputLayoutFirstName = findViewById(R.id.textInputLayoutFirstName);
        textInputLayoutFamilyName = findViewById(R.id.textInputLayoutFamilyName);
        textInputLayoutEmailAddress = findViewById(R.id.textInputLayoutEmailAddress);
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
        textInputEditTextFamilyName = findViewById(R.id.textInputEditTextFamilyName);
        textInputEditTextEmailAddress = findViewById(R.id.textInputEditTextEmailAddress);
        textInputEditTextPassword = findViewById(R.id.textInputEditTextPassword);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        buttonLogin = findViewById(R.id.buttonJoinFamily);

        // Start of Sign up Button Handler
        buttonSignUp.setOnClickListener(view -> {
            // Start of Retrieval of EditText Values, also add text formatting for the names (capitalize each first letter)
            String email = textInputEditTextEmailAddress.getText().toString().trim(),
                    password = textInputEditTextPassword.getText().toString().trim();

            if ( TextUtils.isEmpty(textInputEditTextFirstName.getText().toString().trim()) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
                MyMethods.Generate.warningDialog(this, "Signing up Failed", "Please fill out all fields");
            }
            else if (password.length() < 6){
                textInputLayoutPassword.setError("Password should be at least 6 characters long");
            }
            else {
                showLoading();
                firebaseRegistration(email, password);
            }
        });
        // End of Sign up Button Handler

        // Start - password edittext error clearer
        textInputEditTextPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (textInputEditTextPassword.getText().toString().trim().length() >= 6){
                    textInputLayoutPassword.setError(null);
                }
                return false;
            }
        });
        // End - password edittext error clearer

        // Start of Login Button Handler
        buttonLogin.setOnClickListener(view -> {
            finish();
        });
        // End of Login Button Handler
    }

    private void showLoading() {
        imageViewSignup.setVisibility(View.VISIBLE);
        textViewTheFlameTheBrings2.setVisibility(View.VISIBLE);
        textViewFamilyTogether2.setVisibility(View.VISIBLE);
        circularProgressSignup.setVisibility(View.VISIBLE);
        textInputLayoutFirstName.setVisibility(View.GONE);
        textInputLayoutFamilyName.setVisibility(View.GONE);
        textInputLayoutEmailAddress.setVisibility(View.GONE);
        textInputLayoutPassword.setVisibility(View.GONE);
        buttonSignUp.setVisibility(View.GONE);
        buttonLogin.setVisibility(View.GONE);
    }

    private void dismissLoading() {
        imageViewSignup.setVisibility(View.VISIBLE);
        textViewTheFlameTheBrings2.setVisibility(View.GONE);
        textViewFamilyTogether2.setVisibility(View.GONE);
        circularProgressSignup.setVisibility(View.GONE);
        textInputLayoutFirstName.setVisibility(View.VISIBLE);
        textInputLayoutFamilyName.setVisibility(View.VISIBLE);
        textInputLayoutEmailAddress.setVisibility(View.VISIBLE);
        textInputLayoutPassword.setVisibility(View.VISIBLE);
        buttonSignUp.setVisibility(View.VISIBLE);
        buttonLogin.setVisibility(View.VISIBLE);
    }

    private void toggleHearthLogoVisibility(boolean hasFocus) {
        if (hasFocus){
            imageViewSignup.setVisibility(View.GONE);
        }
        else{
            imageViewSignup.setVisibility(View.VISIBLE);
        }
    }

    private void firebaseRegistration(String email, String password) {
        // sign up user
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    // Start - Retrieve EditText Values, also add text formatting for the names (capitalize each first letter)
                    String firstname = textInputEditTextFirstName.getText().toString().trim();
                    String[] firstnameArray = firstname.split(" ");
                    StringBuilder firstnameStringBuilder = new StringBuilder();
                    for (String s : firstnameArray) {
                        String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
                        firstnameStringBuilder.append(cap + " ");
                    }
                    String familyname = textInputEditTextFamilyName.getText().toString().trim();
                    String[] familynameArray = familyname.split(" ");
                    StringBuilder familynameStringBuilder = new StringBuilder();
                    for (String s : familynameArray) {
                        String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
                        familynameStringBuilder.append(cap + " ");
                    }
                    // End - Retrieve EditText Values, also add text formatting for the names (capitalize each first letter)

                    // sign in user
                    firebaseAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            // get Current User ID
                            String currentUserID = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

                            // build new user
                            newUserReference = hearthDB.getReference("user_"+currentUserID);
                            newUserReference.child("uid").setValue(currentUserID);
                            newUserReference.child("firstname").setValue(firstnameStringBuilder.toString().trim());
                            newUserReference.child("familyname").setValue(familynameStringBuilder.toString().trim());
                            newUserReference.child("photourl").setValue("https://2.bp.blogspot.com/-ZwYKR5Zu28s/U6Qo2qAjsqI/AAAAAAAAhkM/HkbDZEJwvPs/s400/omocha_robot.png");
                            hearthDB.getReference("user_"+currentUserID+"_gold").setValue(0);
                            hearthDB.getReference("user_"+currentUserID+"_xp").setValue(0);

                            // proceed to create or join family page
                            startActivity(new Intent(new Intent(SignUpActivity.this, CreateOrJoinActivity.class)));
                            dismissLoading();
                        }
                    });
                }
                else{
                    MyMethods.Generate.warningDialog(SignUpActivity.this, "Sign up failed", "The email address is already taken");
                    dismissLoading();
                }
            }
        });
    }

    // start - progress dialog builder
    /*public void progressDialogBuilder(String message){
        progressDialogLogin.show();
        progressDialogLogin.setContentView(R.layout.progress_dialog);
        progressDialogLogin.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        TextView textViewProgressLabel = progressDialogLogin.findViewById(R.id.textViewProgressLabel);
        textViewProgressLabel.setText(message);
    }*/
    // end - progress dialog builder
}
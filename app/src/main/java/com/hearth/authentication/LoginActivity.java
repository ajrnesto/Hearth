package com.hearth.authentication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hearth.activities.MissionsActivity;
import com.hearth.methods.MyMethods;
import com.hearth.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    // Start of variable initialization
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseDatabase hearthDB = FirebaseDatabase.getInstance();
    DatabaseReference currentUserReference, currentUserJoinRequestReference;

    private TextInputLayout textInputLayoutEmailAddress, textInputLayoutPassword;
    private TextInputEditText textInputEditTextEmailAddress, textInputEditTextPassword;
    private MaterialButton buttonLogin, buttonSignUp, buttonForgotPassword;
    private TextView textViewTheFlameTheBrings, textViewFamilyTogether;
    private CircularProgressIndicator circularProgressLogin;
    private ImageView imageViewLogin;

    String currentUserID;
    // End of variable initialization

    @Override
    public void onStart() {
        super.onStart();
        boolean isInternetAvailable = isNetworkConnected();
        if (isInternetAvailable == true){
            firebaseAutoLoginUser();
        }
        else{
            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(LoginActivity.this);
            materialAlertDialogBuilder.setTitle("Check your internet connection");
            materialAlertDialogBuilder.setMessage("Please check your internet connection and try again later");
            materialAlertDialogBuilder.setPositiveButton("Okay", (dialogInterface, i) -> {
                finish();
            });
            materialAlertDialogBuilder.show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Start - Declare Android Widgets
        imageViewLogin = findViewById(R.id.imageViewLogin);
        textViewFamilyTogether = findViewById(R.id.textViewFamilyTogether);
        textViewTheFlameTheBrings = findViewById(R.id.textViewTheFlameTheBrings);
        textInputLayoutEmailAddress = this.findViewById(R.id.textInputLayoutEmailAddress);
        textInputLayoutPassword = this.findViewById(R.id.textInputLayoutPassword);
        textInputEditTextEmailAddress = this.findViewById(R.id.textInputEditTextEmailAddress);
        textInputEditTextPassword = this.findViewById(R.id.textInputEditTextPassword);
        circularProgressLogin = findViewById(R.id.circularProgressLogin);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        buttonForgotPassword = findViewById(R.id.buttonForgotPassword);

        // End - Declare of Android Widgets

        // Start - Login Button Handler
        buttonLogin.setOnClickListener(view -> {
            // Start of Retrieval of EditText Values
            String email = Objects.requireNonNull(textInputEditTextEmailAddress.getText()).toString().trim();
            String password = Objects.requireNonNull(textInputEditTextPassword.getText()).toString().trim();
            // End of Retrieval of EditText Values
            if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    showLoading();
                    firebaseLoginUser(email, password);
                }
                else {
                    MyMethods.Generate.warningDialog(LoginActivity.this,"Login Failed", "Please enter a valid email address");
                    dismissLoading();
                }
            }
            else{
                MyMethods.Generate.warningDialog(LoginActivity.this,"Login Failed", "Please fill out all fields");
                dismissLoading();
            }
        });
        // End - Login Button Handler

        // Start - Sign up Button Handler
        buttonSignUp.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, SignUpActivity.class)));
        // End - Sign up Button Handler

        buttonForgotPassword.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
        });
    }

    private void showLoading() {
        textViewFamilyTogether.setVisibility(View.VISIBLE);
        textViewTheFlameTheBrings.setVisibility(View.VISIBLE);
        circularProgressLogin.setVisibility(View.VISIBLE);
        textInputLayoutEmailAddress.setVisibility(View.GONE);
        textInputLayoutPassword.setVisibility(View.GONE);
        buttonForgotPassword.setVisibility(View.GONE);
        buttonLogin.setVisibility(View.GONE);
        buttonSignUp.setVisibility(View.GONE);
    }

    private void dismissLoading() {
        textViewFamilyTogether.setVisibility(View.GONE);
        textViewTheFlameTheBrings.setVisibility(View.GONE);
        circularProgressLogin.setVisibility(View.GONE);
        textInputLayoutEmailAddress.setVisibility(View.VISIBLE);
        textInputLayoutPassword.setVisibility(View.VISIBLE);
        buttonForgotPassword.setVisibility(View.VISIBLE);
        buttonLogin.setVisibility(View.VISIBLE);
        buttonSignUp.setVisibility(View.VISIBLE);
    }

    private void firebaseAutoLoginUser() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser != null){
            checkIfUserHasCode();
        }
        else{
            dismissLoading();
        }
    }

    private void firebaseLoginUser(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            // check if user has family code
            if (task.isSuccessful()){
                checkIfUserHasCode();
            }
            else{
                MyMethods.Generate.warningDialog(LoginActivity.this, "Login Failed", "Invalid Username or Password");
                dismissLoading();
            }
        });
    }

    private void checkIfUserHasCode() {
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        checkDate();

        // check if user has family code
        currentUserReference = hearthDB.getReference("user_"+currentUserID);
        currentUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean familyCodeExists = snapshot.child("familycode").exists();
                // if family code exists
                if (familyCodeExists){
                    // get data
                    // Cache most frequently used data
                    MyMethods.Cache.setString(getApplicationContext(), "uid", currentUserID);
                    MyMethods.Cache.setString(getApplicationContext(), "email", firebaseAuth.getCurrentUser().getEmail());
                    MyMethods.Cache.setString(getApplicationContext(), "familycode", snapshot.child("familycode").getValue().toString());
                    MyMethods.Cache.setString(getApplicationContext(), "firstname", snapshot.child("firstname").getValue().toString());
                    MyMethods.Cache.setString(getApplicationContext(), "familyname", snapshot.child("familyname").getValue().toString());
                    if (snapshot.child("photourl").exists()){
                        MyMethods.Cache.setString(getApplicationContext(), "photourl", snapshot.child("photourl").getValue().toString());
                    }
                    else {
                        currentUserReference.child("photourl").setValue("https://2.bp.blogspot.com/-ZwYKR5Zu28s/U6Qo2qAjsqI/AAAAAAAAhkM/HkbDZEJwvPs/s400/omocha_robot.png");
                        MyMethods.Cache.setString(getApplicationContext(), "photourl", "https://2.bp.blogspot.com/-ZwYKR5Zu28s/U6Qo2qAjsqI/AAAAAAAAhkM/HkbDZEJwvPs/s400/omocha_robot.png");
                    }

                    hearthDB.getReference("user_"+currentUserID+"_gold").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            double gold = Double.valueOf(snapshot.getValue().toString());
                            //int gold = snapshot.child("gold").getValue(Integer.class);
                            MyMethods.Cache.setDouble(getApplicationContext(), "gold", gold);

                            hearthDB.getReference("user_"+currentUserID+"_xp").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    int xp = Integer.valueOf(snapshot.getValue().toString());
                                    MyMethods.Cache.setInt(getApplicationContext(), "xp", xp);

                                    String familycode = MyMethods.Cache.getString(getApplicationContext(), "familycode");
                                    hearthDB.getReference("family_"+familycode+"_members").child("user_"+currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            String role = snapshot.child("role").getValue().toString();
                                            MyMethods.Cache.setString(getApplicationContext(), "role", role);

                                            hearthDB.getReference("family_"+familycode+"_orders").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @SuppressLint("UnsafeOptInUsageError")
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    long numberOfOrders = snapshot.getChildrenCount();
                                                    MyMethods.Cache.setLong(getApplicationContext(), "numberoforders", numberOfOrders);

                                                    // start - roll for shop image banners
                                                    String shop_girl = "https://1.bp.blogspot.com/-Ye9oEywWPjA/XdJI0hTY5YI/AAAAAAABWDE/hHTXt61jv9ASTARZg6slxmNEMMGMyZ8KgCNcBGAsYHQ/s450/shopping_bazzar_fleamarket_woman.png";
                                                    String shop_boy = "https://1.bp.blogspot.com/-4yeRWKzWVk8/XxU0o8r4NNI/AAAAAAABaOQ/tvJzEWxnCcM5fe9L7sl374RAv_FJ4uKwACNcBGAsYHQ/s400/shopping_bazzar_fleamarket_man.png";
                                                    String shop_rich_man = "https://4.bp.blogspot.com/-8MLnxxwFDeo/XASweK9kyxI/AAAAAAABQac/eeYFGFHtMXYtZyIYippxOGIpVLBqT8-NACLcBGAs/s800/money_okanemochi.png";
                                                    String shopbannerlink = "";
                                                    int randomNumber = MyMethods.Generate.randomNumber(1, 100);
                                                    if (randomNumber > 98) shopbannerlink = shop_rich_man;
                                                    else if (randomNumber > 49) shopbannerlink = shop_girl;
                                                    else shopbannerlink = shop_boy;
                                                    MyMethods.Cache.setString(getApplicationContext(), "shop_banner_link", shopbannerlink);
                                                    // end - roll for shop image banners

                                                    // start - roll for shop image banners
                                                    String mission_boy = "https://2.bp.blogspot.com/--LlTTU7NYvY/Voze-FJRBAI/AAAAAAAA2eY/GV87Oiv7zlU/s800/hero_man.png";
                                                    String mission_girl = "https://3.bp.blogspot.com/-N3RTseZW_t4/Voze_YdOVBI/AAAAAAAA2es/W68sS3zmdnc/s800/hero_woman.png";
                                                    String mission_boy2 = "https://3.bp.blogspot.com/-r49EiuJfyuM/XBRfDu-gOCI/AAAAAAABQ2A/vWaPXTCcLeAGYS3wU4fI8fUJ4dwWEul8ACLcBGAs/s800/job_kaji_daikou_man.png";
                                                    String mission_girl2 = "https://4.bp.blogspot.com/-Mf0op56F1Mo/XBRfECoef0I/AAAAAAABQ2E/JnqK60KZ_scqJEjjvGdOGPYyywC3Z9nMwCLcBGAs/s800/job_kaji_daikou_woman.png";
                                                    String missions_skydive = "https://4.bp.blogspot.com/-5002wsHh0Uc/WOdDwe9zRRI/AAAAAAABDls/J1aQ4e17k0QSD1xOo3l7J5ymPX0AAMSCACLcB/s800/kousenju_couple.png";
                                                    String misisonbannerlink = "";
                                                    int randomNumber2 = MyMethods.Generate.randomNumber(1, 100);
                                                    if (randomNumber2 > 98) misisonbannerlink = missions_skydive;
                                                    else if (randomNumber2 > 74) misisonbannerlink = mission_boy;
                                                    else if (randomNumber2 > 49) misisonbannerlink = mission_girl;
                                                    else if (randomNumber2 > 24) misisonbannerlink = mission_boy2;
                                                    else misisonbannerlink = mission_girl2;
                                                    MyMethods.Cache.setString(getApplicationContext(), "mission_banner_link", misisonbannerlink);
                                                    // end - roll for shop image banners

                                                    hearthDB.getReference("family_"+familycode).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            String globalFamilyName = snapshot.child("familyname").getValue().toString();
                                                            MyMethods.Cache.setString(getApplicationContext(), "globalfamilyname", globalFamilyName);

                                                            if (snapshot.child("photourl").exists()){
                                                                String familyPhotoUrl = snapshot.child("photourl").getValue().toString();
                                                                MyMethods.Cache.setString(getApplicationContext(), "familyphotourl", familyPhotoUrl);
                                                            }
                                                            else {
                                                                MyMethods.Cache.setString(getApplicationContext(), "familyphotourl", "https://4.bp.blogspot.com/-Xd7h725VGgk/U9zsrHl7QxI/AAAAAAAAkeg/c3ecwYFH3Qs/s1600/hoka_05_question.png");
                                                            }

                                                            // Start main activity
                                                            Toast.makeText(getApplicationContext(), "Signed in as "+MyMethods.Cache.getString(getApplicationContext(), "email"), Toast.LENGTH_SHORT).show();
                                                            startActivity(new Intent(LoginActivity.this, MissionsActivity.class));
                                                            finish();
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                                }
                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) { }
                                            });
                                            // End of top action bar and bottom nav bar initialization
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) { }
                                    });
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) { }
                            });
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) { }
                    });
                }
                else{
                    // checkIfUserHasPendingJoinRequest();
                    // Cache most frequently used data
                    MyMethods.Cache.setString(getApplicationContext(), "uid", currentUserID);
                    MyMethods.Cache.setString(getApplicationContext(), "email", firebaseAuth.getCurrentUser().getEmail());
                    MyMethods.Cache.setString(getApplicationContext(), "firstname", snapshot.child("firstname").getValue().toString());
                    MyMethods.Cache.setString(getApplicationContext(), "familyname", snapshot.child("familyname").getValue().toString());
                    MyMethods.Cache.setString(getApplicationContext(), "photourl", snapshot.child("photourl").getValue().toString());

                    currentUserJoinRequestReference = hearthDB.getReference("user_"+currentUserID+"_joinrequest");
                    currentUserJoinRequestReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean requestExists = snapshot.exists();

                            if (requestExists){
                                MyMethods.Cache.setString(getApplicationContext(), "familycode", snapshot.getValue().toString());

                                startActivity(new Intent(LoginActivity.this, RequestJoinActivity.class));
                                finish();
                            }
                            else {
                                startActivity(new Intent(LoginActivity.this, CreateOrJoinActivity.class));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            private void checkIfUserHasPendingJoinRequest() {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void checkDate() {
        // get date now
        Date dateNow = new Date();
        SimpleDateFormat sdfDateNow = new SimpleDateFormat("MM/dd/yyyy");
        String currentDate = sdfDateNow.format(dateNow);

        // get stored date
        String storedDate = MyMethods.Cache.getString(getApplicationContext(), currentUserID+"stored_date");

        // compare values
        if (currentDate.equals(storedDate)){
            //Toast.makeText(getApplicationContext(), "You already opened today", Toast.LENGTH_SHORT).show();
        }
        else {
            // store current date
            MyMethods.Cache.setString(getApplicationContext(), currentUserID+"stored_date", currentDate);
            // reset and clear all finished missions
            hearthDB.getReference("user_"+currentUserID+"_missions_completed").removeValue();
            //Toast.makeText(getApplicationContext(), "This is the first time you opened today", Toast.LENGTH_SHORT).show();
        }
    }

    // start - check for internet connection
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
    // end - check for internet connection
}
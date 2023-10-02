package com.hearth.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hearth.R;
import com.hearth.adapters.ProfilePictureAdapter;
import com.hearth.adapters.ShopItemAdapter;
import com.hearth.methods.MyMethods;
import com.hearth.objects.ShopItem;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;

public class EditProfileActivity extends AppCompatActivity implements ProfilePictureAdapter.OnProfilePictureListener {

    FirebaseDatabase hearthDB = FirebaseDatabase.getInstance();
    DatabaseReference userRef;

    MaterialButton btnBack, btnSave;
    TextInputLayout tilFirstName, tilFamilyName;
    TextInputEditText etFirstName, etFamilyName;
    RoundedImageView imgProfilePicture;
    ImageView imgEdit;

    RecyclerView rvProfilePictures;
    ProfilePictureAdapter profilePictureAdapter;
    ArrayList<String> photoUrls;
    ProfilePictureAdapter.OnProfilePictureListener onProfilePictureListener = this;
    Context context;

    String familyCode;
    String selectedImage = "";
    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initializeVars();
        prefillInformation();

        imgProfilePicture.setOnClickListener(view -> {
            showRecyclerView();
        });

        btnBack.setOnClickListener(view -> {
            backPressed();
        });
        
        btnSave.setOnClickListener(view -> {
            saveProfileDetails();
        });
    }

    private void saveProfileDetails() {
        String firstName = etFirstName.getText().toString();
        String familyName = etFamilyName.getText().toString();

        if ((TextUtils.isEmpty(firstName)) || (TextUtils.isEmpty(familyName))){
            MyMethods.Generate.warningDialog(EditProfileActivity.this, "Name fields cannot be empty", "Please fill out all required fields.");
            return;
        }

        if (selectedImage.equals("")) {
            selectedImage = MyMethods.Cache.getString(getApplicationContext(), "photourl");
        }

        userRef = hearthDB.getReference("user_"+currentUserId);
        userRef.child("firstname").setValue(firstName);
        MyMethods.Cache.setString(getApplicationContext(), "firstname", firstName);
        userRef.child("familyname").setValue(familyName);
        MyMethods.Cache.setString(getApplicationContext(), "familyname", familyName);
        userRef.child("photourl").setValue(selectedImage);
        MyMethods.Cache.setString(getApplicationContext(), "photourl", selectedImage);

        hearthDB.getReference("family_"+familyCode+"_members")
                .child("user_"+currentUserId)
                .child("photourl")
                .setValue(selectedImage);

        finish();
    }

    @Override
    public void onBackPressed() {
        backPressed();
    }

    private void backPressed(){
        if (rvProfilePictures.getVisibility() == View.GONE){
            finish();
        }
        else {
            hideRecyclerView();
        }
    }

    private void initializeVars() {
        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSave);
        tilFirstName = findViewById(R.id.tilFirstName);
        tilFamilyName = findViewById(R.id.tilFamilyName);
        etFirstName = findViewById(R.id.etFirstName);
        etFamilyName = findViewById(R.id.etFamilyName);
        imgProfilePicture = findViewById(R.id.imgProfilePicture);
        imgEdit = findViewById(R.id.imgEdit);
        rvProfilePictures = findViewById(R.id.rvProfilePictures);

        context = this;
        familyCode = MyMethods.Cache.getString(getApplicationContext(), "familycode");
        currentUserId = MyMethods.Cache.getString(getApplicationContext(), "uid");
    }

    private void prefillInformation() {
        etFirstName.setText(MyMethods.Cache.getString(getApplicationContext(), "firstname"));
        etFamilyName.setText(MyMethods.Cache.getString(getApplicationContext(), "familyname"));
        MyMethods.Generate.image(MyMethods.Cache.getString(getApplicationContext(), "photourl"), imgProfilePicture);
    }

    private void showRecyclerView() {
        rvProfilePictures.setVisibility(View.VISIBLE);
        loadRecyclerView();

        imgProfilePicture.setVisibility(View.GONE);
        imgEdit.setVisibility(View.GONE);
        tilFirstName.setVisibility(View.GONE);
        tilFamilyName.setVisibility(View.GONE);
        etFirstName.setVisibility(View.GONE);
        etFamilyName.setVisibility(View.GONE);
        btnSave.setVisibility(View.GONE);
    }

    private void hideRecyclerView() {
        rvProfilePictures.setVisibility(View.GONE);
        imgProfilePicture.setVisibility(View.VISIBLE);
        imgEdit.setVisibility(View.VISIBLE);
        tilFirstName.setVisibility(View.VISIBLE);
        tilFamilyName.setVisibility(View.VISIBLE);
        etFirstName.setVisibility(View.VISIBLE);
        etFamilyName.setVisibility(View.VISIBLE);
        btnSave.setVisibility(View.VISIBLE);
    }

    private void loadRecyclerView(){

        rvProfilePictures.setHasFixedSize(true);
        rvProfilePictures.setLayoutManager(new GridLayoutManager(EditProfileActivity.this, 3));

        photoUrls = new ArrayList<>();
        photoUrls.add("https://2.bp.blogspot.com/-ZwYKR5Zu28s/U6Qo2qAjsqI/AAAAAAAAhkM/HkbDZEJwvPs/s400/omocha_robot.png");
        photoUrls.add("https://1.bp.blogspot.com/-nMTqgygyczs/VqtZItHn2_I/AAAAAAAA3ZY/5ZfrZPuZzbY/s450/bird_aoitori_bluebird.png");
        photoUrls.add("https://2.bp.blogspot.com/-PPZoKIXmwmQ/WBsAy0kYtTI/AAAAAAAA_V4/Xr1KJbqEhh4tFQXRCFbtInIn2nySiTi9ACLcB/s800/pink_elephant.png");
        photoUrls.add("https://1.bp.blogspot.com/-2WkafnFIzH8/VJF-g5lgiZI/AAAAAAAApr8/0DpPOT42q_8/s800/ekubo_boy.png");
        photoUrls.add("https://2.bp.blogspot.com/-kL8PczWaQ1k/W6DTRB2y5SI/AAAAAAABO68/c7Y5tRagZG0bg98X-orq7SDKqnpmla5VACLcBGAs/s400/hair_girl_syokkaku.png");
        photoUrls.add("https://2.bp.blogspot.com/-CjOhVQHAJAA/Wj4Isga7R8I/AAAAAAABJPM/XlY55gRu93E87XdM7ICkmi3SIe89WWJawCLcBGAs/s800/sobakasu_boy.png");
        photoUrls.add("https://1.bp.blogspot.com/-R7bnEXGATis/VnE4Gjay2zI/AAAAAAAA19A/z_jja1C7kRY/s800/pose_kyosyu_girl.png");
        photoUrls.add("https://2.bp.blogspot.com/-Oyv8zbXLGbA/WVd6CvKx9MI/AAAAAAABFNc/y3zUDAbjV_o54A9vQGgDJYVkGGhjzItOACEwYBhgL/s800/recorder_boy2.png");
        photoUrls.add("https://2.bp.blogspot.com/-jL7Zv1yYHNc/UnIEKfSdGoI/AAAAAAAAZ_M/fyzfjZhyYms/s400/kenban_harmonica_girl.png");
        photoUrls.add("https://1.bp.blogspot.com/-5Ivf_XbpMGY/VYJrrYShorI/AAAAAAAAuiI/OjRHAljzJ6A/s400/oishii1_boy.png");
        photoUrls.add("https://4.bp.blogspot.com/-qOMhp0gSSgY/Uvy50I5UxqI/AAAAAAAAdqM/U5GtO7xrgVI/s400/banzai_girl.png");
        photoUrls.add("https://2.bp.blogspot.com/-1hctN7UBQ4E/UWyk200Pd3I/AAAAAAAAQnM/UREV2zX1USQ/s400/mother_silent.png");
        photoUrls.add("https://3.bp.blogspot.com/-330gH9GDMVY/V5jKcQxFYTI/AAAAAAAA81M/z31aAMRMWP0PO1xtX72JHIF6G7ZoFm2CQCLcB/s400/megane_wellington_man.png");
        photoUrls.add("https://2.bp.blogspot.com/-yTMoVO2v8Rc/VWmA09co6JI/AAAAAAAAt0k/OK4AlEJuh3M/s400/kirakira_woman.png");
        photoUrls.add("https://2.bp.blogspot.com/-lpJ_Sd6O4pA/U2ssJDuF2sI/AAAAAAAAf_8/Bu9GOZGgzSg/s800/megane_man.png");

        profilePictureAdapter = new ProfilePictureAdapter(context, photoUrls, onProfilePictureListener);
        rvProfilePictures.setAdapter(profilePictureAdapter);

        profilePictureAdapter.notifyDataSetChanged();
    }

    @Override
    public void onProfilePictureClick(int position) {
        selectedImage = photoUrls.get(position);
        backPressed();
        MyMethods.Generate.image(selectedImage, imgProfilePicture);
    }
}
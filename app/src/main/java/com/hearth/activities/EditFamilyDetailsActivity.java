package com.hearth.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hearth.R;
import com.hearth.adapters.ProfilePictureAdapter;
import com.hearth.methods.MyMethods;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;

public class EditFamilyDetailsActivity extends AppCompatActivity implements ProfilePictureAdapter.OnProfilePictureListener {

    FirebaseDatabase hearthDB = FirebaseDatabase.getInstance();
    DatabaseReference familyRef, leaderboardRef;

    RoundedImageView imgFamilyIcon;
    ImageView imgEdit;
    TextInputLayout tilFamilyName;
    TextInputEditText etFamilyName;
    RecyclerView rvFamilyIcons;
    MaterialButton btnBack, btnSave;

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
        setContentView(R.layout.activity_edit_family_details);

        initializeVars();
        prefillInformation();

        imgFamilyIcon.setOnClickListener(view -> {
            showRecyclerView();
        });

        btnBack.setOnClickListener(view -> {
            backPressed();
        });

        btnSave.setOnClickListener(view -> {
            saveFamilyDetails();
        });
    }

    @Override
    public void onBackPressed() {
        backPressed();
    }

    private void initializeVars() {
        btnBack = findViewById(R.id.btnBack);
        imgFamilyIcon = findViewById(R.id.imgFamilyIcon);
        imgEdit = findViewById(R.id.imgEdit);
        tilFamilyName = findViewById(R.id.tilFamilyName);
        etFamilyName = findViewById(R.id.etFamilyName);
        rvFamilyIcons = findViewById(R.id.rvFamilyIcons);
        btnSave = findViewById(R.id.btnSave);

        familyCode = MyMethods.Cache.getString(getApplicationContext(), "familycode");
        currentUserId = MyMethods.Cache.getString(getApplicationContext(), "uid");
        context = EditFamilyDetailsActivity.this;
    }

    private void prefillInformation() {
        etFamilyName.setText(MyMethods.Cache.getString(getApplicationContext(), "globalfamilyname"));
        MyMethods.Generate.image(MyMethods.Cache.getString(getApplicationContext(), "familyphotourl"), imgFamilyIcon);
    }

    private void backPressed(){
        if (rvFamilyIcons.getVisibility() == View.GONE){
            finish();
        }
        else {
            hideRecyclerView();
        }
    }

    private void showRecyclerView() {
        rvFamilyIcons.setVisibility(View.VISIBLE);
        loadRecyclerView();
        imgFamilyIcon.setVisibility(View.GONE);
        imgEdit.setVisibility(View.GONE);
        tilFamilyName.setVisibility(View.GONE);
        etFamilyName.setVisibility(View.GONE);
        btnSave.setVisibility(View.GONE);
    }

    private void hideRecyclerView() {
        rvFamilyIcons.setVisibility(View.GONE);
        imgFamilyIcon.setVisibility(View.VISIBLE);
        imgEdit.setVisibility(View.VISIBLE);
        tilFamilyName.setVisibility(View.VISIBLE);
        etFamilyName.setVisibility(View.VISIBLE);
        btnSave.setVisibility(View.VISIBLE);
    }

    private void loadRecyclerView() {
        rvFamilyIcons.setHasFixedSize(true);
        rvFamilyIcons.setLayoutManager(new GridLayoutManager(EditFamilyDetailsActivity.this, 3));

        photoUrls = new ArrayList<>();
        photoUrls.add("https://1.bp.blogspot.com/-vQDB-jzNh5g/U82zGkw2AHI/AAAAAAAAjKs/Tu61uwfkWZk/s800/animal_mark01_buta.png");
        photoUrls.add("https://3.bp.blogspot.com/-tVdeLo7vHdM/U82zGrKXN_I/AAAAAAAAjKo/Akh_OyYLMdQ/s800/animal_mark02_kuma.png");
        photoUrls.add("https://2.bp.blogspot.com/-XIjdkGsG_u8/U82zGlpgNUI/AAAAAAAAjKw/dxsQGmNb7BU/s800/animal_mark03_inu.png");
        photoUrls.add("https://3.bp.blogspot.com/-2AkJE-MGLfc/U82zHahLd6I/AAAAAAAAjLI/0zEEK9QOsdM/s800/animal_mark04_neko.png");
        photoUrls.add("https://2.bp.blogspot.com/-F2XdQD40qiI/U82zHl-YcUI/AAAAAAAAjK8/bNZItoYU3Qo/s800/animal_mark05_zou.png");
        photoUrls.add("https://4.bp.blogspot.com/-ZaNYAtCBvFE/U82zH6Gn8jI/AAAAAAAAjLE/U6usr9E8rok/s800/animal_mark06_uma.png");
        photoUrls.add("https://2.bp.blogspot.com/-aDf7XweReek/U82zIqQMY_I/AAAAAAAAjLQ/POasanBJzuA/s800/animal_mark07_lion.png");
        photoUrls.add("https://4.bp.blogspot.com/-s3QaUKsDsSc/U82zJLLE0tI/AAAAAAAAjLc/3h9Ge6JqsIw/s800/animal_mark08_kaba.png");
        photoUrls.add("https://4.bp.blogspot.com/-0uhh2DUaFqc/U82zJSQ_QSI/AAAAAAAAjLo/VZsybH8SfeQ/s800/animal_mark09_tora.png");
        photoUrls.add("https://1.bp.blogspot.com/-ErzyFYbu3BE/U82zJvbu-RI/AAAAAAAAjLk/z2cNkQEPdrM/s800/animal_mark10_usagi.png");
        photoUrls.add("https://3.bp.blogspot.com/-F3aKqLX4CeM/U82zKK_aRjI/AAAAAAAAjLs/d92OlApO4iU/s800/animal_mark11_panda.png");
        photoUrls.add("https://3.bp.blogspot.com/-P3sBaIyGqRE/U82zKphdy0I/AAAAAAAAjL0/k-G1al3DRho/s150/animal_mark12_saru.png");
        photoUrls.add("https://3.bp.blogspot.com/-h4mM4eR1lSo/U82zK9dcvmI/AAAAAAAAjMU/fmgufOAY97A/s150/animal_mark13_penguin.png");
        photoUrls.add("https://2.bp.blogspot.com/-QlS3RfPllyQ/U82zLDlY7fI/AAAAAAAAjMA/t0NJ7n_K4wY/s150/animal_mark14_hitsuji.png");
        photoUrls.add("https://4.bp.blogspot.com/-n3GPXtCuIHc/U82zLub22BI/AAAAAAAAjMI/dhN0ua_xgeM/s150/animal_mark15_koala.png");

        profilePictureAdapter = new ProfilePictureAdapter(context, photoUrls, onProfilePictureListener);
        rvFamilyIcons.setAdapter(profilePictureAdapter);

        profilePictureAdapter.notifyDataSetChanged();
    }

    private void saveFamilyDetails() {
        String familyName = etFamilyName.getText().toString();

        if (TextUtils.isEmpty(familyName)){
            MyMethods.Generate.warningDialog(EditFamilyDetailsActivity.this, "Name fields cannot be empty", "Please fill out all required fields.");
            return;
        }

        if (selectedImage.equals("")) {
            selectedImage = MyMethods.Cache.getString(getApplicationContext(), "familyphotourl");
        }

        familyRef = hearthDB.getReference("family_"+familyCode);
        familyRef.child("familyname").setValue(familyName);
        MyMethods.Cache.setString(getApplicationContext(), "globalfamilyname", familyName);
        familyRef.child("photourl").setValue(selectedImage);
        MyMethods.Cache.setString(getApplicationContext(), "familyphotourl", selectedImage);

        leaderboardRef = hearthDB.getReference("leaderboards/family_"+familyCode);
        leaderboardRef.child("familyname").setValue(familyName);
        leaderboardRef.child("photourl").setValue(selectedImage);

        finish();
    }

    @Override
    public void onProfilePictureClick(int position) {
        selectedImage = photoUrls.get(position);
        backPressed();
        MyMethods.Generate.image(selectedImage, imgFamilyIcon);
    }
}
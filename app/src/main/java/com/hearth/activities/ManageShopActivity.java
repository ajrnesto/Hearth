package com.hearth.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hearth.R;
import com.hearth.methods.MyMethods;
import com.makeramen.roundedimageview.RoundedImageView;

public class ManageShopActivity extends AppCompatActivity {

    FirebaseDatabase hearthDB = FirebaseDatabase.getInstance();
    DatabaseReference currentUserReference, newItemReference;
    DatabaseReference itemReference;
    String familyCode;

    RoundedImageView imageViewShopThumbnail;
    MaterialButton materialButtonClose, materialButtonDecrementReward, materialButtonIncrementReward, materialButtonAddItem, materialButtonDelete, materialButtonAddShopThumbnail;
    TextInputEditText textInputEditTextItemName, textInputEditTextPrice;

    @Override
    protected void onResume() {
        super.onResume();
        // get image link after returning from browse image activity
        String item_uid = getIntent().getExtras().getString("item_uid");
        checkIfHasImage(item_uid);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_item);

        materialButtonClose = findViewById(R.id.materialButtonClose);
        materialButtonAddShopThumbnail = findViewById(R.id.materialButtonAddShopThumbnail);
        imageViewShopThumbnail = findViewById(R.id.imageViewItemThumbnail);
        materialButtonDecrementReward = findViewById(R.id.materialButtonDecrementReward);
        materialButtonIncrementReward = findViewById(R.id.materialButtonIncrementReward);
        materialButtonAddItem = findViewById(R.id.materialButtonSaveItem);
        materialButtonDelete = findViewById(R.id.materialButtonDelete);
        textInputEditTextItemName = findViewById(R.id.textInputEditTextItemName);
        textInputEditTextPrice = findViewById(R.id.textInputEditTextPrice);

        // start - initialize values
        int manage_mode = getIntent().getExtras().getInt("manage_mode");
        String item_uid = getIntent().getExtras().getString("item_uid");

        // close button handler
        materialButtonClose.setOnClickListener(view -> {
            deleteTempItem(item_uid);
            finish();
        });

        checkIfHasImage(item_uid);

        // get intent manage mode key
        if (manage_mode == 0){ // if mode == add
            materialButtonDelete.setVisibility(View.GONE);
        }
        else if (manage_mode == 1){ // if mode == edit
            String item_name = getIntent().getExtras().getString("item_name");
            String item_price = getIntent().getExtras().getString("item_price");

            // display item details
            textInputEditTextItemName.setText(item_name);
            textInputEditTextPrice.setText(item_price);

            materialButtonDelete.setVisibility(View.VISIBLE);
            materialButtonDelete.setOnClickListener(view -> {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                deleteItem(item_uid);
            });
        }

        // start - save button handler
        materialButtonAddItem.setOnClickListener(view -> {
            // validate input
            if (TextUtils.isEmpty(textInputEditTextItemName.getText()) || TextUtils.isEmpty(textInputEditTextPrice.getText())){
                MyMethods.Generate.warningDialog(ManageShopActivity.this,"Error Adding Item", "Please enter an item name and price");
            }
            else{
                String itemName = textInputEditTextItemName.getText().toString().trim();
                double initialItemPrice = Double.parseDouble(textInputEditTextPrice.getText().toString().trim());

                // invalidate empty fields
                if (initialItemPrice < 0){
                    MyMethods.Generate.warningDialog(ManageShopActivity.this, "Invalid Number", "Prices may not be less than 0");
                }
                // save mission
                else {
                    double itemPrice = initialItemPrice;
                    if (manage_mode == 0){ // if mode = add
                        deleteTempItem(item_uid);
                        saveItemToFirebase(item_uid, itemName, itemPrice);
                    }
                    else if (manage_mode == 1){ // if mode == update
                        deleteTempItem(item_uid);
                        updateItem(item_uid, itemName, itemPrice);
                    }
                }
            }
        });
        // End - add item button handler

        // start - 2 decimal places limiter
        textInputEditTextPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                MyMethods.EditTextMethods.decimalPlaceValueLimiter(textInputEditTextPrice, textInputEditTextPrice.getText().toString());
                MyMethods.EditTextMethods.limitCharacters(textInputEditTextPrice, textInputEditTextPrice.getText().toString());
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });
        // end - 2 decimal places limiter

        // increment/decrement button handlers
        materialButtonDecrementReward.setOnClickListener(view -> {
            MyMethods.EditTextMethods.incrementDouble(textInputEditTextPrice, -1);
        });
        materialButtonIncrementReward.setOnClickListener(view -> {
            MyMethods.EditTextMethods.incrementDouble(textInputEditTextPrice, 1);
        });
    }

    private void checkIfHasImage(String item_uid) {
        // get cached image mode value
        int has_image = MyMethods.Cache.getInt(getApplicationContext(), item_uid+"has_image");

        if (has_image == 0){ // 0 == has no image
            imageViewShopThumbnail.setVisibility(View.INVISIBLE);
            materialButtonAddShopThumbnail.setVisibility(View.VISIBLE);

            // start - image button handler
            materialButtonAddShopThumbnail.setOnClickListener(view -> {
                Intent intentBrowseImage = new Intent(getApplicationContext(), ImageBrowserActivity.class);
                intentBrowseImage.putExtra("item_uid", item_uid);
                startActivity(intentBrowseImage);
                overridePendingTransition(R.anim.zoom_in_enter, R.anim.zoom_in_exit);
            });
            // end - image button handler
        }
        else if (has_image == 1){ // 1 == has image
            imageViewShopThumbnail.setVisibility(View.VISIBLE);
            materialButtonAddShopThumbnail.setVisibility(View.INVISIBLE);

            // render image
            String imageLink = MyMethods.Cache.getString(getApplicationContext(), item_uid+"image_link");
            MyMethods.Generate.image(imageLink, imageViewShopThumbnail);

            // start - image button handler
            imageViewShopThumbnail.setOnClickListener(view -> {
                Intent intentBrowseImage = new Intent(getApplicationContext(), ImageBrowserActivity.class);
                intentBrowseImage.putExtra("item_uid", item_uid);
                startActivity(intentBrowseImage);
                overridePendingTransition(R.anim.zoom_in_enter, R.anim.zoom_in_exit);
            });
            // end - image button handler
        }
    }

    private void saveItemToFirebase(String itemUid, String itemName, double itemPrice) {
        // get family code
        familyCode = MyMethods.Cache.getString(getApplicationContext(), "familycode");

        // get family new item reference
        newItemReference = hearthDB.getReference("family_"+familyCode+"_items").child(itemUid);
        // insert values using retrieved family code
        newItemReference.child("uid").setValue(itemUid);
        newItemReference.child("name").setValue(itemName);
        newItemReference.child("price").setValue(itemPrice);
        int has_image = MyMethods.Cache.getInt(getApplicationContext(), itemUid+"has_image");
        if (has_image == 1){
            newItemReference.child("imagelink").setValue(MyMethods.Cache.getString(getApplicationContext(), itemUid+"image_link"));
        }
        finish();
    }

    private void updateItem(String itemUid, String itemName, double itemPrice) {
        String strRoundedToHundredths = String.format("%.2f", itemPrice);
        double roundedToHudredths = Double.parseDouble(strRoundedToHundredths);

        // get family code
        familyCode = MyMethods.Cache.getString(getApplicationContext(), "familycode");

        // get family shop reference
        Toast.makeText(getApplicationContext(), itemUid, Toast.LENGTH_SHORT).show();
        itemReference = hearthDB.getReference("family_"+familyCode+"_items").child(itemUid);
        // insert values using retrieved family code
        itemReference.child("name").setValue(itemName);
        itemReference.child("price").setValue(itemPrice);
        int has_image = MyMethods.Cache.getInt(getApplicationContext(), itemUid+"has_image");
        if (has_image == 1){
            itemReference.child("imagelink").setValue(MyMethods.Cache.getString(getApplicationContext(), itemUid+"image_link"));
        }
        finish();
    }

    private void deleteItem(String itemUid) {
        // get family code
        String familycode = MyMethods.Cache.getString(getApplicationContext(), "familycode");

        // add new mission
        itemReference = hearthDB.getReference("family_"+familycode+"_items").child(itemUid);
        itemReference.removeValue();
        Toast.makeText(getApplicationContext(), "Successfully deleted item", Toast.LENGTH_SHORT).show();

        finish();
    }

    private void deleteTempItem(String itemUid) {
        // get family code
        String familycode = MyMethods.Cache.getString(getApplicationContext(), "familycode");

        // add new mission
        hearthDB.getReference("family_"+familycode+"_items_temp").child(itemUid).removeValue();

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        String item_uid = getIntent().getExtras().getString("item_uid");
        deleteTempItem(item_uid);
    }

    @Override
    public void onPause(){
        super.onPause();
        overridePendingTransition(R.anim.zoom_out_enter, R.anim.zoom_out_exit);
    }
}
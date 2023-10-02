package com.hearth.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;

import com.google.android.material.button.MaterialButton;
import com.hearth.R;
import com.hearth.adapters.MissionImageAdapter;
import com.hearth.methods.MyMethods;
import com.hearth.objects.MissionImage;

import java.util.ArrayList;

public class MissionImageBrowserActivity extends AppCompatActivity implements MissionImageAdapter.OnMissionImageListener {

    Context context;
    MaterialButton materialButtonClose;

    RecyclerView recyclerViewMissionBrowseImage;
    MissionImageAdapter missionImageAdapter;
    ArrayList<MissionImage> missionImageArrayList;
    MissionImageAdapter.OnMissionImageListener onMissionImageListener = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission_image_browser);

        context = this;
        materialButtonClose = findViewById(R.id.materialButtonClose);

        // start - close button listener
        materialButtonClose.setOnClickListener(view -> {
            finish();
        });
        // end - close button listener

        //set up missions recyclerview and array list
        recyclerViewMissionBrowseImage = findViewById(R.id.recyclerViewMissionBrowseImage);
        recyclerViewMissionBrowseImage.setHasFixedSize(true);
        recyclerViewMissionBrowseImage.setLayoutManager(new LinearLayoutManager(context));

        missionImageArrayList = new ArrayList<>();
        missionImageAdapter = new MissionImageAdapter(context, missionImageArrayList, onMissionImageListener);
        recyclerViewMissionBrowseImage.setAdapter(missionImageAdapter);

        // finally, display all images
        missionImageArrayList.clear();
        missionImageArrayList.add(new MissionImage("https://3.bp.blogspot.com/-K6gkRY6r3gs/WqihdXjE3xI/AAAAAAABKzs/F8pG25EHfBI-U2QbojMM2KoBhIih_e3YACLcBGAs/s800/cooking_chef_man_india.png"));
        missionImageArrayList.add(new MissionImage("https://2.bp.blogspot.com/-8kcgP3q4bV8/VVGVRyHGNZI/AAAAAAAAtj0/LwnD-T2dByg/s400/sara_arai_man.png"));
        missionImageArrayList.add(new MissionImage("https://4.bp.blogspot.com/-m9JWfaE6TKw/W0mF1cenVwI/AAAAAAABNU8/7a-nlu-eA34PTL8kTscE614f0C49XeETwCLcBGAs/s400/gomidashi_woman.png"));
        missionImageArrayList.add(new MissionImage("https://4.bp.blogspot.com/-nd2nJxtDc_8/UqmPVIRpQaI/AAAAAAAAbho/rhzcfisa8b8/s400/gomi_bunbetsu.png"));
        missionImageArrayList.add(new MissionImage("https://4.bp.blogspot.com/-XMQUHDbtfPg/UYzZhi6hgWI/AAAAAAAAR7k/ipmlKbUd9xg/s400/oosouji_yukafuki.png"));
        missionImageArrayList.add(new MissionImage("https://2.bp.blogspot.com/-AClvqlfUWxs/WxvKiaxIm3I/AAAAAAABMqA/vpbsbScM4wcyRAGXTXXOhwg9aYPv9smRQCLcBGAs/s400/toilet_sheet_fuku.png"));
        missionImageArrayList.add(new MissionImage("https://4.bp.blogspot.com/-aurP2wC6nSI/WZP34_BvCJI/AAAAAAABGCc/UDEGhvMKOKc89dZkwlFGQctBLR2vma2gwCLcBGAs/s400/soujiki_man.png"));
        missionImageArrayList.add(new MissionImage("https://3.bp.blogspot.com/-nJU3qV0HZCw/WEOPQ5DfQaI/AAAAAAABALI/52AkNHri_tEJAG0v--wJFct1RktZYs81QCLcB/s800/heyaboshi2.png"));
        missionImageArrayList.add(new MissionImage("https://3.bp.blogspot.com/-Vd9_UP69TR8/VJ6XtvAuTaI/AAAAAAAAqPA/DqxFZTy-B5o/s400/syougakkou_souji.png"));
        missionImageAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMissionImageClick(int position) {
        String imageLink = missionImageArrayList.get(position).getImagelink();
        String mission_uid = getIntent().getStringExtra("mission_uid");
        MyMethods.Cache.setInt(getApplicationContext(), mission_uid+"has_image", 1);
        MyMethods.Cache.setString(getApplicationContext(), mission_uid+"image_link", imageLink);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.zoom_out_enter, R.anim.zoom_out_exit);
    }
}
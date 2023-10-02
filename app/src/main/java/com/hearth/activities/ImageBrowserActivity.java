package com.hearth.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.FirebaseDatabase;
import com.hearth.R;
import com.hearth.adapters.RewardImageAdapter;
import com.hearth.methods.MyMethods;
import com.hearth.objects.RewardImage;

import java.util.ArrayList;

public class ImageBrowserActivity extends AppCompatActivity implements RewardImageAdapter.OnRewardImageListener{

    FirebaseDatabase hearthDB = FirebaseDatabase.getInstance();

    Context context;
    MaterialButton materialButtonClose;

    RecyclerView recyclerViewImageBrowser;
    RewardImageAdapter rewardImageAdapter;
    ArrayList<RewardImage> rewardImageArrayList;
    RewardImageAdapter.OnRewardImageListener onRewardImageListener = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_browser);

        context = this;
        materialButtonClose = findViewById(R.id.materialButtonClose);

        // start - close button listener
        materialButtonClose.setOnClickListener(view -> {
            finish();
        });
        // end - close button listener

        //set up missions recyclerview and array list
        recyclerViewImageBrowser = findViewById(R.id.recyclerViewBrowseImage);
        recyclerViewImageBrowser.setHasFixedSize(true);
        recyclerViewImageBrowser.setLayoutManager(new LinearLayoutManager(context));

        rewardImageArrayList = new ArrayList<>();
        rewardImageAdapter = new RewardImageAdapter(context, rewardImageArrayList, onRewardImageListener);
        recyclerViewImageBrowser.setAdapter(rewardImageAdapter);

        // finally, display all images
        rewardImageArrayList.clear();
        rewardImageArrayList.add(new RewardImage("https://1.bp.blogspot.com/-ZELov-QvHaU/UVWMfIiV3bI/AAAAAAAAPIM/xxWcxLdHrwk/s1600/candy.png"));
        rewardImageArrayList.add(new RewardImage("https://4.bp.blogspot.com/-DNdtNOYXZtM/XGjyq-XmxyI/AAAAAAABRis/FPQSzcjLQQcsffqRaeoX4H8W1mpJqAcTwCLcBGAs/s400/game_tetsuya_man.png"));
        rewardImageArrayList.add(new RewardImage("https://2.bp.blogspot.com/-UaRA1uGULPQ/U6gMioXOUxI/AAAAAAAAhpM/PGtbUKTyiOE/s800/bbq.png"));
        rewardImageArrayList.add(new RewardImage("https://2.bp.blogspot.com/-NM1zcPg0Ivo/UZYk3E9DdvI/AAAAAAAATHQ/GUNBHBP_Gec/s800/camp_campfire_boy_girls.png"));
        rewardImageArrayList.add(new RewardImage("https://2.bp.blogspot.com/-LHcOOQtouzU/UaVVItdVF1I/AAAAAAAAUAU/m7vyqxjD-kw/s800/icecream9_mix.png"));
        rewardImageArrayList.add(new RewardImage("https://4.bp.blogspot.com/-VpcMz2UegQA/VD3R7QgW_BI/AAAAAAAAoLM/jhqOwkIEgwg/s400/cycling_family.png"));
        rewardImageArrayList.add(new RewardImage("https://3.bp.blogspot.com/-CvrWdSNVPWg/XDXcSnPUyHI/AAAAAAABRIw/zAZW8ODkYk0a1kvNn9gZE_qhsVTy6O1UACLcBGAs/s450/kandou_movie_eigakan.png"));
        rewardImageArrayList.add(new RewardImage("https://4.bp.blogspot.com/-tTMwwjImn9o/Ur1GFwGRj-I/AAAAAAAAcXA/1gTHWhNchcE/s500/kouen_oyako.png"));
        rewardImageArrayList.add(new RewardImage("https://2.bp.blogspot.com/-t8-webWBL94/UZB7jgDTpVI/AAAAAAAASFk/qkmgqebrJnw/s500/tatemono_kouen.png"));
        rewardImageArrayList.add(new RewardImage("https://4.bp.blogspot.com/-KQ8pABuWBrA/WYKyl4QyUOI/AAAAAAABF5I/5s5TMPJ7fq0P5S4BMEnvA9sbFDcBRMYEACLcBGAs/s800/pool_asobu_couple.png"));
        rewardImageAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRewardImageClick(int position) {
        String imageLink = rewardImageArrayList.get(position).getImagelink();
        String item_uid = getIntent().getStringExtra("item_uid");
        MyMethods.Cache.setInt(getApplicationContext(), item_uid+"has_image", 1);
        MyMethods.Cache.setString(getApplicationContext(), item_uid+"image_link", imageLink);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.zoom_out_enter, R.anim.zoom_out_exit);
    }
}
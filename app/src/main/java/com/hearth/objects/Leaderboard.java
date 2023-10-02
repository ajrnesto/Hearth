package com.hearth.objects;

import android.content.Intent;

public class Leaderboard {
    String familyname;
    Integer score;
    String photourl;

    public Leaderboard() {
    }

    public Leaderboard(String familyname, Integer score, String photourl) {
        this.familyname = familyname;
        this.score = score;
        this.photourl = photourl;
    }

    public String getFamilyname() {
        return familyname;
    }

    public void setFamilyname(String familyname) {
        this.familyname = familyname;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getPhotourl() {
        return photourl;
    }

    public void setPhotourl(String photourl) {
        this.photourl = photourl;
    }
}

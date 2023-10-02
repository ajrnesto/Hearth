package com.hearth.objects;

public class CompletedMission {
    String adminname;
    String itemname;
    String uid;

    public CompletedMission() {
    }

    public CompletedMission(String adminname, String itemname, String uid) {
        this.adminname = adminname;
        this.itemname = itemname;
        this.uid = uid;
    }

    public String getAdminname() {
        return adminname;
    }

    public void setAdminname(String adminname) {
        this.adminname = adminname;
    }

    public String getItemname() {
        return itemname;
    }

    public void setItemname(String itemname) {
        this.itemname = itemname;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}

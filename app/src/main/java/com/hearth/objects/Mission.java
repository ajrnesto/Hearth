package com.hearth.objects;

public class Mission {
    private String title;
    private String description;
    private String uid;
    private double reward;
    private String imagelink;
    private long alarm;
    private String alarmmilitary;

    public Mission() {}  // Needed for Firebase

    public Mission(String title, String description, String uid, double reward, String imagelink, long alarm, String alarmmilitary) {
        this.title = title;
        this.description = description;
        this.uid = uid;
        this.reward = reward;
        this.imagelink = imagelink;
        this.alarm = alarm;
        this.alarmmilitary = alarmmilitary;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public double getReward() {
        return reward;
    }

    public void setReward(double reward) {
        this.reward = reward;
    }

    public String getImagelink() {
        return imagelink;
    }

    public void setImagelink(String imagelink) {
        this.imagelink = imagelink;
    }

    public long getAlarm() {
        return alarm;
    }

    public void setAlarm(long alarm) {
        this.alarm = alarm;
    }

    public String getAlarmmilitary() {
        return alarmmilitary;
    }

    public void setAlarmmilitary(String alarmmilitary) {
        this.alarmmilitary = alarmmilitary;
    }
}

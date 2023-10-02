package com.hearth.objects;

public class Order {
    public String buyername;
    public long date;
    public String imagelink;
    public String itemname;
    public String uid;

    public Order(String buyername, long date, String imagelink, String itemname, String uid) {
        this.buyername = buyername;
        this.date = date;
        this.imagelink = imagelink;
        this.itemname = itemname;
        this.uid = uid;
    }

    public Order() {
    }

    public String getBuyername() {
        return buyername;
    }

    public void setBuyername(String buyername) {
        this.buyername = buyername;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getImagelink() {
        return imagelink;
    }

    public void setImagelink(String imagelink) {
        this.imagelink = imagelink;
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

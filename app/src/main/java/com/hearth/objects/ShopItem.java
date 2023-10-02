package com.hearth.objects;

public class ShopItem {
    private String name;
    private double price;
    private String uid;
    private String imagelink;

    public ShopItem() {}  // Needed for Firebase

    public ShopItem(String name, double price, String uid, String imagelink) {
        this.name = name;
        this.price = price;
        this.uid = uid;
        this.imagelink = imagelink;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }

    public void setPrice(double price) { this.price = price; }

    public String getUid() { return uid; }

    public void setUid(String uid) { this.uid = uid; }

    public String getImagelink() { return imagelink; }

    public void setImagelink(String imagelink) { this.imagelink = imagelink; }
}
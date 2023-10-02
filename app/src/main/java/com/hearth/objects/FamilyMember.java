package com.hearth.objects;

public class FamilyMember {
    private String fullname;
    private String role;
    private String uid;
    private String photourl;

    public FamilyMember() {}  // Needed for Firebase

    public FamilyMember(String fullname, String role, String uid, String photourl) {
        this.fullname = fullname;
        this.role = role;
        this.uid = uid;
        this.photourl = photourl;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPhotourl() {
        return photourl;
    }

    public void setPhotourl(String photourl) {
        this.photourl = photourl;
    }
}
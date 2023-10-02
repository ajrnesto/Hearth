package com.hearth.objects;

public class JoinRequest {
    String uid;
    String fullname;
    String photourl;

    public JoinRequest() {
    }

    public JoinRequest(String uid, String fullname, String photourl) {
        this.uid = uid;
        this.fullname = fullname;
        this.photourl = photourl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getPhotourl() {
        return photourl;
    }

    public void setPhotourl(String photourl) {
        this.photourl = photourl;
    }
}

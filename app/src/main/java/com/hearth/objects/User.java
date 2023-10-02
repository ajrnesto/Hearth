package com.hearth.objects;

public class User {
    private String familyCode;
    private String familyName;
    private String firstName;

    public User() {
    }

    public User(String familyCode, String familyName, String firstName) {
        this.familyCode = familyCode;
        this.familyName = familyName;
        this.firstName = firstName;
    }

    public String getFamilyCode() {
        return familyCode;
    }

    public void setFamilyCode(String familyCode) {
        this.familyCode = familyCode;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
}

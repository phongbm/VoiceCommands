package com.phongbm.voicecommands;

public class Contact {
    private static final String TAG = "Contact";

    private String number, displayName;

    public Contact(String number, String displayName) {
        this.number = number;
        this.displayName = displayName;
    }

    public String getNumber() {
        return number;
    }

    public String getDisplayName() {
        return displayName;
    }

}
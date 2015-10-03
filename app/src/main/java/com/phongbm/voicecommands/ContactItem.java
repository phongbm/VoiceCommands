package com.phongbm.voicecommands;

public class ContactItem {
    private String number, displayName;

    public ContactItem(String number, String displayName) {
        this.number = number;
        this.displayName = displayName;
    }

    public String getNumber() {
        return number;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public boolean equals(Object object) {
        return object != null && (object instanceof ContactItem)
                && (number.equals(((ContactItem) object).getNumber()));
    }

}
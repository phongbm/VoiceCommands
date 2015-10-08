package com.phongbm.voicecommands;

import com.phongbm.common.CommonValue;

public class ContactItem {
    private static final String TAG = "ContactItem";
    private static String typeOfComparisons;

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
        if (object == null || !(object instanceof ContactItem)) {
            return false;
        }
        switch (typeOfComparisons) {
            case CommonValue.TYPE_NUMBER:
                return number.equals(((ContactItem) object).getNumber());
            case CommonValue.TYPE_DISPLAY_NAME:
                return displayName.toUpperCase().equals(
                        ((ContactItem) object).getDisplayName().toUpperCase());
            default:
                return false;
        }
    }

    public static void setTypeOfComparisons(String typeOfComparisons) {
        ContactItem.typeOfComparisons = typeOfComparisons;
    }

}
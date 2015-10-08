package com.phongbm.voicecommands;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;

public class ContactManager {
    private Context context;
    private ArrayList<Contact> contacts;

    public ContactManager(Context context) {
        this.context = context;
        contacts = new ArrayList<>();
    }

    public void getContacts() {
        if (contacts == null) {
            contacts = new ArrayList<>();
        } else {
            contacts.clear();
        }
        Cursor cursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},
                null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        if (cursor != null) {
            int indexNumber = cursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.NUMBER);
            int indexDisplayName = cursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                contacts.add(new Contact(cursor.getString(indexNumber),
                        cursor.getString(indexDisplayName)));
                cursor.moveToNext();
            }
            cursor.close();
        }
    }

    public String getDisplayNameFromNumber(String number) {
        for (int i = 0; i < contacts.size(); i++) {
            if (contacts.get(i).getNumber().equals(number)) {
                return contacts.get(i).getDisplayName();
            }
        }
        return null;
    }

    public String getNumberFromDisplayName(String displayName) {
        Contact contact;
        String name;
        boolean existDisplayName = false;
        ArrayList<Contact> contactsTemp = new ArrayList<>();
        for (int i = 0; i < contacts.size(); i++) {
            contact = contacts.get(i);
            name = contact.getDisplayName();
            if (name.toUpperCase().equals(displayName.toUpperCase())) {
                return contact.getNumber();
            } else {
                if (name.toUpperCase().contains(displayName.toUpperCase())) {
                    existDisplayName = true;
                    contactsTemp.add(contact);
                }
            }
        }
        if (!existDisplayName) {
            return null;
        }
        int minLength = contactsTemp.get(0).getDisplayName().length();
        int index = 0;
        for (int i = 1; i < contactsTemp.size(); i++) {
            if (contactsTemp.get(i).getDisplayName().length() < minLength) {
                minLength = contactsTemp.get(i).getDisplayName().length();
                index = i;
            }
        }
        return contactsTemp.get(index).getNumber();
    }

}
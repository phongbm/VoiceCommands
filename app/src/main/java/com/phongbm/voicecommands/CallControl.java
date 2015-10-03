package com.phongbm.voicecommands;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;

public class CallControl {
    private static CallControl callControl;

    public static CallControl getInstance() {
        if (callControl == null) {
            callControl = new CallControl();
        }
        return callControl;
    }

    public void endCall(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        ITelephony telephony;
        try {
            Class className = Class.forName(telephonyManager.getClass().getName());
            Method method = className.getDeclaredMethod("getITelephony");
            method.setAccessible(true);
            telephony = (ITelephony) method.invoke(telephonyManager);
            telephony.endCall();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void answerRingingCall(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        ITelephony telephony;
        try {
            Class className = Class.forName(telephonyManager.getClass().getName());
            Method method = className.getDeclaredMethod("getITelephony");
            method.setAccessible(true);
            telephony = (ITelephony) method.invoke(telephonyManager);
            telephony.answerRingingCall();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
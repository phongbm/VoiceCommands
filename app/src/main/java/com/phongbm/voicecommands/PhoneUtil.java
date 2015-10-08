package com.phongbm.voicecommands;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.KeyEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PhoneUtil {
    public static String TAG = PhoneUtil.class.getSimpleName();

    public static void endCall(Context context) {
        try {
            Object telephonyObject = getTelephonyObject(context);
            if (null != telephonyObject) {
                Class telephonyClass = telephonyObject.getClass();
                Method endCallMethod = telephonyClass.getMethod("endCall");
                endCallMethod.setAccessible(true);
                endCallMethod.invoke(telephonyObject);
            }
        } catch (SecurityException | NoSuchMethodException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private static Object getTelephonyObject(Context context) {
        Object telephonyObject = null;
        try {
            // 初始化iTelephony
            TelephonyManager telephonyManager = (TelephonyManager)
                    context.getSystemService(Context.TELEPHONY_SERVICE);
            // Will be used to invoke hidden methods with reflection
            // Get the current object implementing ITelephony interface
            Class telManager = telephonyManager.getClass();
            Method getITelephony = telManager.getDeclaredMethod("getITelephony");
            getITelephony.setAccessible(true);
            telephonyObject = getITelephony.invoke(telephonyManager);
        } catch (SecurityException | NoSuchMethodException | IllegalArgumentException
                | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return telephonyObject;
    }


    private static void answerRingingCallWithReflect(Context context) {
        try {
            Object telephonyObject = getTelephonyObject(context);
            if (null != telephonyObject) {
                Class telephonyClass = telephonyObject.getClass();
                Method endCallMethod = telephonyClass.getMethod("answerRingingCall");
                endCallMethod.setAccessible(true);
                endCallMethod.invoke(telephonyObject);
                // ITelephony iTelephony = (ITelephony) telephonyObject;
                // iTelephony.answerRingingCall();
            }
        } catch (SecurityException | IllegalArgumentException | IllegalAccessException
                | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }


    private static void answerRingingCallWithBroadcast(Context context) {
        AudioManager localAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        //判断是否插上了耳机
        boolean isWiredHeadsetOn = localAudioManager.isWiredHeadsetOn();
        if (!isWiredHeadsetOn) {
//          Intent headsetPluggedIntent = new Intent(Intent.ACTION_HEADSET_PLUG);
//          headsetPluggedIntent.putExtra("state", 1);
//          headsetPluggedIntent.putExtra("microphone", 0);
//          headsetPluggedIntent.putExtra("name", "");
//          context.sendBroadcast(headsetPluggedIntent);
            Intent meidaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK);
            meidaButtonIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
            context.sendOrderedBroadcast(meidaButtonIntent, null);
//          Intent headsetUnpluggedIntent = new Intent(Intent.ACTION_HEADSET_PLUG);
//          headsetUnpluggedIntent.putExtra("state", 0);
//          headsetUnpluggedIntent.putExtra("microphone", 0);
//          headsetUnpluggedIntent.putExtra("name", "");
//          context.sendBroadcast(headsetUnpluggedIntent);
        } else {
            Intent meidaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK);
            meidaButtonIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
            context.sendOrderedBroadcast(meidaButtonIntent, null);
        }
    }

    public static void answerRingingCall(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {  //2.3或2.3以上系统
            answerRingingCallWithBroadcast(context);
        } else {
            answerRingingCallWithReflect(context);
        }
    }

    public static void callPhone(Context context, String phoneNumber) {
        if (!TextUtils.isEmpty(phoneNumber)) {
            try {
                Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                context.startActivity(callIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void dialPhone(Context context, String phoneNumber) {
        if (!TextUtils.isEmpty(phoneNumber)) {
            try {
                Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
                context.startActivity(callIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
package com.phongbm.voicecommands;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.phongbm.common.CommonValue;

public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCompletedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive...");
        Intent intentStartService = new Intent();
        intentStartService.setClassName(CommonValue.PACKAGE_NAME,
                CommonValue.SERVICE_CLASS_NAME);
        context.startService(intentStartService);
    }

}
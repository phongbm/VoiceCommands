package com.phongbm.voicecommands;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class IncomingCallReceiver extends BroadcastReceiver {
    private static final String TAG = "IncomingCallReceiver";

    private TextToSpeechEngine textToSpeechEngine;
    private AudioManager audioManager;
    private boolean isCalling = false;

    public IncomingCallReceiver() {
        isCalling = false;
    }

    public IncomingCallReceiver(Context context) {
        isCalling = false;
        textToSpeechEngine = new TextToSpeechEngine(context);
        textToSpeechEngine.setUtteranceId("INCOMING_CALL");
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive...");
        if (isCalling) {
            return;
        }
        isCalling = true;
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            MyPhoneStateListener phoneListener = new MyPhoneStateListener();
            telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            Log.i(TAG, "State: " + state + "\n"
                    + "IncomingNumber: " + incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.i(TAG, "CALL_STATE_IDLE...");
                    isCalling = false;
                    //speechRecognizer.stopListening();
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.i(TAG, "CALL_STATE_RINGING...");

                    /*int index = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
                    String displayName = VoiceCommandsService.this
                            .getDisplayNameFromNumber(incomingNumber);
                    if (displayName != null) {
                        textToSpeechEngine.speakOut(TextToSpeech.QUEUE_FLUSH, "Incoming call from " + displayName);
                    } else {
                        textToSpeechEngine.speakOut(TextToSpeech.QUEUE_FLUSH, "Incoming call from " + incomingNumber);
                    }
                    textToSpeechEngine.pause(1000);
                    textToSpeechEngine.speakOut(TextToSpeech.QUEUE_ADD, "Accept or Divert");

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            speechRecognizer.startListening(speechRecognizerIntent);
                        }
                    }, 7500);*/
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.i(TAG, "CALL_STATE_OFFHOOK...");
                    break;
            }
        }
    }

}
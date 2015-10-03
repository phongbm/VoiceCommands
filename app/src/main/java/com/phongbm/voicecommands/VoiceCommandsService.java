package com.phongbm.voicecommands;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

public class VoiceCommandsService extends Service {
    private static final String TAG = "VoiceCommandsService";

    private Context context;
    private TextToSpeechEngine textToSpeechEngine;
    private AudioManager audioManager;
    private IncomingCallReceiver incomingCallReceiver;
    private ArrayList<ContactItem> contactItems;
    private Intent speechRecognizerIntent;
    private SpeechRecognizer speechRecognizer;
    private boolean isCallStateRinging, isRinging;

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate...");
        super.onCreate();
        context = this;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        isCallStateRinging = false;
        isRinging = false;

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());
        speechRecognizerIntent.putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 10000);
        speechRecognizerIntent.putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 10000);
        speechRecognizerIntent.putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 10000);
        speechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
        this.registerIncomingCallReceiver();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand...");
        this.initializeContacts();
        return Service.START_STICKY;
    }

    private void registerIncomingCallReceiver() {
        if (incomingCallReceiver == null) {
            incomingCallReceiver = new IncomingCallReceiver();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        this.registerReceiver(incomingCallReceiver, intentFilter);
    }

    private void initializeContacts() {
        contactItems = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},
                null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        if (cursor != null) {
            int indexNumber = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int indexDisplayName = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                contactItems.add(new ContactItem(cursor.getString(indexNumber),
                        cursor.getString(indexDisplayName)));
                cursor.moveToNext();
            }
            cursor.close();
        }
    }

    private String getDisplayNameFromNumber(String number) {
        ContactItem contactItem = new ContactItem(number, null);
        int index = contactItems.indexOf(contactItem);
        if (index >= 0) {
            return contactItems.get(index).getDisplayName();
        }
        return null;
    }

    private class SpeechRecognitionListener implements RecognitionListener {
        @Override
        public void onReadyForSpeech(Bundle params) {
            Log.i(TAG, "onReadyForSpeech...");
        }

        @Override
        public void onBeginningOfSpeech() {
            Log.i(TAG, "onBeginningOfSpeech...");
        }

        @Override
        public void onRmsChanged(float rmsdB) {
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            Log.i(TAG, "onBufferReceived...");
        }

        @Override
        public void onEndOfSpeech() {
            Log.i(TAG, "onEndOfSpeech...");
        }

        @Override
        public void onError(int error) {
            Log.i(TAG, "onError");
            if (isRinging) {
                Log.i(TAG, "isRinging...");
                speechRecognizer.startListening(speechRecognizerIntent);
            }
            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    Log.i(TAG, "ERROR_AUDIO");
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    Log.i(TAG, "ERROR_CLIENT");
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    Log.i(TAG, "ERROR_INSUFFICIENT_PERMISSIONS");
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    Log.i(TAG, "ERROR_NETWORK");
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    Log.i(TAG, "ERROR_NETWORK_TIMEOUT");
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    Log.i(TAG, "ERROR_NO_MATCH");
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    Log.i(TAG, "ERROR_RECOGNIZER_BUSY");
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    Log.i(TAG, "ERROR_SERVER");
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    Log.i(TAG, "ERROR_SPEECH_TIMEOUT");
                    break;
            }
        }

        @Override
        public void onResults(Bundle results) {
            Log.i(TAG, "onResults...");
            ArrayList<String> resultsRecognition = results.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION);
            if (resultsRecognition != null && resultsRecognition.size() > 0) {
                Log.i(TAG, "Result: " + resultsRecognition.get(0));
                if (isRinging) {
                    switch (resultsRecognition.get(0).toUpperCase()) {
                        case "OK":
                            Log.i(TAG, "Accept...");
                            CallControl.getInstance().answerRingingCall(context);
                            speechRecognizer.stopListening();
                            break;
                        case "GOOGLE":
                            Log.i(TAG, "Divert...");
                            CallControl.getInstance().endCall(context);
                            break;
                    }
                }
            }

        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            Log.i(TAG, "onPartialResults...");
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            Log.i(TAG, "onEvent...");
        }
    }

    public class IncomingCallReceiver extends BroadcastReceiver {
        private static final String TAG = "IncomingCallReceiver";

        public IncomingCallReceiver() {
            textToSpeechEngine = new TextToSpeechEngine(context);
            textToSpeechEngine.setUtteranceId("INCOMING_CALL");
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive...");
            if (isCallStateRinging) {
                return;
            }
            isCallStateRinging = true;
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
                        isRinging = false;
                        textToSpeechEngine.stop();
                        speechRecognizer.stopListening();
                        break;
                    case TelephonyManager.CALL_STATE_RINGING:
                        Log.i(TAG, "CALL_STATE_RINGING...");
                        if (isRinging) {
                            return;
                        }
                        isRinging = true;
                        int index = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
                        String displayName = VoiceCommandsService.this
                                .getDisplayNameFromNumber(incomingNumber);
                        if (displayName != null) {
                            textToSpeechEngine.speakOut(TextToSpeech.QUEUE_FLUSH,
                                    "Incoming call from " + displayName);
                        } else {
                            textToSpeechEngine.speakOut(TextToSpeech.QUEUE_FLUSH,
                                    "Incoming call from " + incomingNumber);
                        }
                        textToSpeechEngine.pause(1000);
                        textToSpeechEngine.speakOut(TextToSpeech.QUEUE_ADD, "Accept or divert?");

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                speechRecognizer.startListening(speechRecognizerIntent);
                            }
                        }, 7500);
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        Log.i(TAG, "CALL_STATE_OFFHOOK...");
                        isRinging = true;
                        textToSpeechEngine.stop();
                        speechRecognizer.stopListening();
                        break;
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        this.unregisterReceiver(incomingCallReceiver);
        textToSpeechEngine.shutdown();
        speechRecognizer.destroy();
        super.onDestroy();
    }

}
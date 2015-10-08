package com.phongbm.voicecommands;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Telephony;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
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
    private SMSReceiver smsReceiver;
    private ContactManager contactManager;
    private Intent speechRecognizerIntent;
    private SpeechRecognizer speechRecognizer;
    private boolean isCallStateRinging, isRinging, isReceivedSms, isCommandCall;
    private String messageBody;
    private VCReceiver vcReceiver;

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate...");
        super.onCreate();
        context = this;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        isCallStateRinging = false;
        isRinging = false;
        isReceivedSms = false;
        isCommandCall = false;
        messageBody = null;

        contactManager = new ContactManager(context);
        textToSpeechEngine = new TextToSpeechEngine(context);

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
        this.registerSMSReceiver();
        this.registerVCReceiver();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand...");
        contactManager.getContacts();
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

    private void registerSMSReceiver() {
        if (smsReceiver == null) {
            smsReceiver = new SMSReceiver();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        this.registerReceiver(smsReceiver, intentFilter);
    }

    private void registerVCReceiver() {
        if (vcReceiver == null) {
            vcReceiver = new VCReceiver();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("COMMAND");
        this.registerReceiver(vcReceiver, intentFilter);
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
            if (isRinging) {
                Log.i(TAG, "isRinging...");
                speechRecognizer.startListening(speechRecognizerIntent);
            }
            if (isReceivedSms) {
                Log.i(TAG, "isReceivedSms...");
                speechRecognizer.startListening(speechRecognizerIntent);
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
                            isRinging = false;
                            speechRecognizer.stopListening();
                            // CallControl.getInstance().answerRingingCall(context);
                            break;
                        case "GOOGLE":
                            Log.i(TAG, "Divert...");
                            CallControl.getInstance().endCall(context);
                            break;
                    }
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
                if (isReceivedSms) {
                    switch (resultsRecognition.get(0).toUpperCase()) {
                        case "READ":
                            Log.i(TAG, messageBody);
                            textToSpeechEngine.speakOut(TextToSpeech.QUEUE_ADD, messageBody);
                            break;
                    }
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
                if (isCommandCall) {
                    String result = resultsRecognition.get(0);
                    Log.i(TAG, result);
                    int indexFirstSpace = result.indexOf(" ");
                    if (indexFirstSpace == -1) {
                        return;
                    }
                    String command = result.substring(0, indexFirstSpace);
                    String content = result.substring(indexFirstSpace + 1);
                    Log.i(TAG, command);
                    Log.i(TAG, content);
                    String number = contactManager.getNumberFromDisplayName(content);
                    if (number == null) {
                        Log.i(TAG, "NULL");
                        return;
                    }
                    Log.i(TAG, "Number: " + number);
                    isCommandCall = false;

                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    callIntent.setData(Uri.parse("tel:" + number));
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
                            != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    VoiceCommandsService.this.startActivity(callIntent);
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

    private class IncomingCallReceiver extends BroadcastReceiver {
        private static final String TAG = "IncomingCallReceiver";

        public IncomingCallReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive...");
            if (isCallStateRinging) {
                return;
            }
            isCallStateRinging = true;

            textToSpeechEngine.setUtteranceId("INCOMING_CALL");

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
            private static final String TAG = "MyPhoneStateListener";

            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                Log.i(TAG, "State: " + state + "\n"
                        + "IncomingNumber: " + incomingNumber);
                switch (state) {
                    case TelephonyManager.CALL_STATE_IDLE:
                        Log.i(TAG, "CALL_STATE_IDLE...");
                        isCommandCall = false;

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
                        String displayName = contactManager.getDisplayNameFromNumber(incomingNumber);
                        if (displayName != null) {
                            textToSpeechEngine.speakOut(TextToSpeech.QUEUE_FLUSH,
                                    "Incoming call from " + displayName);
                        } else {
                            textToSpeechEngine.speakOut(TextToSpeech.QUEUE_FLUSH,
                                    "Incoming call from " + incomingNumber);
                        }
                        textToSpeechEngine.pause(1000);
                        textToSpeechEngine.speakOut(TextToSpeech.QUEUE_ADD, "Accept or divert");

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                speechRecognizer.startListening(speechRecognizerIntent);
                            }
                        }, 7500);
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        Log.i(TAG, "CALL_STATE_OFFHOOK...");
                        isCommandCall = false;

                        isRinging = false;
                        textToSpeechEngine.stop();
                        speechRecognizer.stopListening();
                        break;
                }
            }
        }
    }

    private class SMSReceiver extends BroadcastReceiver {
        private static final String TAG = "SMSReceiver";

        public SMSReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive...");

            textToSpeechEngine.setUtteranceId("SMS_RECEIVED");

            Bundle bundle = intent.getExtras();
            final Object[] objects = (Object[]) bundle.get("pdus");
            if (objects == null) {
                return;
            }
            isReceivedSms = true;
            String displayOriginatingAddress = null;
            for (int i = 0; i < objects.length; i++) {
                SmsMessage smsMessage;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    smsMessage = SmsMessage.createFromPdu((byte[]) objects[i],
                            Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
                } else {
                    smsMessage = SmsMessage.createFromPdu((byte[]) objects[i]);
                }
                displayOriginatingAddress = smsMessage.getDisplayOriginatingAddress();
                messageBody = smsMessage.getMessageBody();
            }
            String displayName = contactManager.getDisplayNameFromNumber(displayOriginatingAddress);
            if (displayName != null) {
                textToSpeechEngine.speakOut(TextToSpeech.QUEUE_FLUSH,
                        "There is a new text message from " + displayName);
            } else {
                textToSpeechEngine.speakOut(TextToSpeech.QUEUE_FLUSH,
                        "There is a new text message from " + displayOriginatingAddress);
            }
            textToSpeechEngine.pause(1000);
            textToSpeechEngine.speakOut(TextToSpeech.QUEUE_ADD, "To read now, say read");

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
            }, 10000);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isReceivedSms = false;
                }
            }, 20000);
        }

    }

    private class VCReceiver extends BroadcastReceiver {
        private static final String TAG = "VCReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra("ACTION");
            switch (action) {
                case "CALL":
                    Log.i(TAG, "CALL");
                    isCommandCall = true;
                    speechRecognizer.startListening(speechRecognizerIntent);
                    break;
                case "MESSAGE":
                    Log.i(TAG, "MESSAGE");
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        this.unregisterReceiver(incomingCallReceiver);
        this.unregisterReceiver(smsReceiver);
        this.unregisterReceiver(vcReceiver);
        textToSpeechEngine.shutdown();
        speechRecognizer.destroy();
        super.onDestroy();
    }

}
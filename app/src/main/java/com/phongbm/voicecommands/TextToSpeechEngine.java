package com.phongbm.voicecommands;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;

public class TextToSpeechEngine extends UtteranceProgressListener
        implements android.speech.tts.TextToSpeech.OnInitListener {
    private static final String TAG = "TextToSpeechEngine";

    private TextToSpeech textToSpeech;
    private HashMap<String, String> maps;
    private String utteranceId;

    public TextToSpeechEngine(Context context) {
        textToSpeech = new TextToSpeech(context, this);
        textToSpeech.setOnUtteranceProgressListener(this);
        maps = new HashMap<>();
    }

    public void setUtteranceId(String utteranceId) {
        this.utteranceId = utteranceId;
        maps.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.i(TAG, "This language is not supported");
                return;
            } else {
                Log.i(TAG, "This language is supported");
            }
            textToSpeech.setPitch(0.75F);
            textToSpeech.setSpeechRate(0.75F);
        } else {
            Log.i(TAG, "Error: " + status);
        }
    }

    public void speakOut(int queueMode, String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech.speak(message, queueMode, null, utteranceId);
        } else {
            textToSpeech.speak(message, queueMode, maps);
        }
    }

    public void pause(int duration) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech.playSilentUtterance(duration, TextToSpeech.QUEUE_ADD, utteranceId);
        } else {
            textToSpeech.playSilence(duration, TextToSpeech.QUEUE_ADD, maps);
        }
    }

    @Override
    public void onStart(String utteranceId) {
        Log.i(TAG, "onStart... " + utteranceId);
    }

    @Override
    public void onDone(String utteranceId) {
        Log.i(TAG, "onDone... " + utteranceId);
    }

    @Override
    public void onError(String utteranceId) {
        Log.i(TAG, "onError... " + utteranceId);
    }

    public void stop() {
        if (textToSpeech.isSpeaking()) {
            textToSpeech.stop();
        }
    }

    public void shutdown() {
        textToSpeech.shutdown();
    }

}
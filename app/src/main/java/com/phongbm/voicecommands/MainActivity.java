package com.phongbm.voicecommands;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.phongbm.common.CommonValue;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CHECK_TTS_DATA = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        this.checkTextToSpeechEngine();
    }

    private void startService() {
        Log.i(TAG, "startService...");
        Intent intent = new Intent();
        intent.setClassName(CommonValue.PACKAGE_NAME, CommonValue.SERVICE_CLASS_NAME);
        this.startService(intent);
    }

    private void checkTextToSpeechEngine() {
        Intent intent = new Intent();
        intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        this.startActivityForResult(intent, REQUEST_CHECK_TTS_DATA);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHECK_TTS_DATA) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                Log.i(TAG, "CHECK_VOICE_DATA_PASS...");
                this.startService();
            } else {
                Intent intent = new Intent();
                intent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                this.startActivity(intent);
            }
        }
    }

}
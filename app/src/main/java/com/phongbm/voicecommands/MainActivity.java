package com.phongbm.voicecommands;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.phongbm.common.CommonValue;

import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CHECK_TTS_DATA = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        this.setPermissions();
        this.checkTextToSpeechEngine();

        (findViewById(R.id.btnCall)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction("COMMAND");
                intent.putExtra("ACTION", "CALL");
                MainActivity.this.sendBroadcast(intent);
            }
        });

        (findViewById(R.id.btnMessage)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction("COMMAND");
                intent.putExtra("ACTION", "MESSAGE");
                MainActivity.this.sendBroadcast(intent);
            }
        });
    }

    private void setPermissions() {
        try {
            Process process = Runtime.getRuntime().exec("su", null, null);
            OutputStream outputStream = process.getOutputStream();
            outputStream.write(("pm grant " + this.getPackageName()
                    + " android.permission.MODIFY_PHONE_STATE\n")
                    .getBytes("ASCII"));
            outputStream.flush();
            outputStream.close();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
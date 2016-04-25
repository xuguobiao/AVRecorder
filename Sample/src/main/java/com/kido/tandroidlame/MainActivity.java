package com.kido.tandroidlame;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button audioRecorder = (Button) findViewById(R.id.audioRecorder);
        audioRecorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Mp3AudioRecordActivity.class));
            }
        });

        Button videoRecorder = (Button) findViewById(R.id.videoRecorder);
        videoRecorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Mp4VideoRecorderActivity.class));
            }
        });

    }
}

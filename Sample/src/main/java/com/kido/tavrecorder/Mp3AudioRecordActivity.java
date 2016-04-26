/*
 * Copyright (C) 2016 Naman Dwivedi
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package com.kido.tavrecorder;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.kido.androidlame.manager.DebugLog;
import com.kido.androidlame.manager.VoiceRecorder;

import java.io.File;

public class Mp3AudioRecordActivity extends Activity {

  private TextView statusTextView;
  private VoiceRecorder voiceRecorder;
  private String successPath = "";

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_audio_record);

    final Button start = (Button) findViewById(R.id.startRecording);
    final Button stop = (Button) findViewById(R.id.stopRecording);
    final Button open = (Button) findViewById(R.id.openMeida);

    statusTextView = (TextView) findViewById(R.id.statusText);
    voiceRecorder = new VoiceRecorder(1, 10);


    start.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!voiceRecorder.isRecording()) {
          new Thread() {
            @Override
            public void run() {
              String savePath = Consts.MEDIA_PATH + "/" + System.currentTimeMillis() + ".mp3";
              voiceRecorder.startRecording(savePath, new VoiceRecorder.OnRecordListener() {
                @Override
                public void onRecord(int curDurationSecond) {
                  String info = "onRecord->duration=" + curDurationSecond + "s";
                  DebugLog.i(info);
                  statusTextView.setText(info);
                }

                @Override
                public void onFail(int failCode, String failMessage) {
                  String info = "onFail->failCode=" + failCode + ", failMessage=" + failMessage;
                  DebugLog.e(info);
                  statusTextView.setText(info);
                  open.setVisibility(View.GONE);
                  Toast.makeText(Mp3AudioRecordActivity.this, info, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFinish(int totalDurationSecond, String savePath) {
                  String info = "onFinish->totalDurationSecond=" + totalDurationSecond + ", savePath=" + savePath;
                  DebugLog.i(info);
                  statusTextView.setText(info);
                  successPath = savePath;
                  open.setVisibility(View.VISIBLE);
                  Toast.makeText(Mp3AudioRecordActivity.this, info, Toast.LENGTH_SHORT).show();
                }
              });
            }
          }.start();

        } else
          Toast.makeText(Mp3AudioRecordActivity.this, "Already recording", Toast.LENGTH_SHORT).show();
      }
    });

    stop.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        voiceRecorder.stopRecording();
      }
    });

    open.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        File file = new File(successPath);
        intent.setDataAndType(Uri.fromFile(file), "audio/*");
        startActivity(intent);
      }
    });

  }

}

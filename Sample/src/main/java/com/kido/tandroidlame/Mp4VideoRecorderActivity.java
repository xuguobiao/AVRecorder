package com.kido.tandroidlame;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kido.videorecorder.VideoRecorder;


public class Mp4VideoRecorderActivity extends Activity {
  private TextView statusTextView;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_mp4_video_recorder);

    Button start = (Button) findViewById(R.id.startRecording);
    statusTextView = (TextView)findViewById(R.id.statusText);

    start.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        VideoRecorder.getInstance().startRecording(Mp4VideoRecorderActivity.this, new VideoRecorder.OnRecordListener() {
          @Override
          public void onFail(int failCode, String failMessage) {
            statusTextView.setText("onFail->failCode=" + failCode + ", failMessage=" + failMessage);
          }

          @Override
          public void onFinish(int totalDurationSecond, String savePath) {
            statusTextView.setText("onFinish->totalDurationSecond=" + totalDurationSecond + ", savePath=" + savePath);
          }
        });
      }
    });
  }
}

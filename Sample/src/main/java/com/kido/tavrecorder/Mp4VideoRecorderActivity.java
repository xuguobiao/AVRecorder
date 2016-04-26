package com.kido.tavrecorder;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kido.videorecorder.manager.VideoRecorder;

import java.io.File;


public class Mp4VideoRecorderActivity extends Activity {
  private TextView statusTextView;
  private String successPath = "";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_mp4_video_recorder);

    Button start = (Button) findViewById(R.id.startRecording);
    final Button open = (Button) findViewById(R.id.openMeida);

    statusTextView = (TextView) findViewById(R.id.statusText);

    start.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        VideoRecorder.getInstance().startRecording(Mp4VideoRecorderActivity.this, new VideoRecorder.OnRecordListener() {
          @Override
          public void onFail(int failCode, String failMessage) {
            statusTextView.setText("onFail->failCode=" + failCode + ", failMessage=" + failMessage);
            open.setVisibility(View.GONE);
          }

          @Override
          public void onFinish(int totalDurationSecond, String savePath) {
            statusTextView.setText("onFinish->totalDurationSecond=" + totalDurationSecond + ", savePath=" + savePath);
            successPath = savePath;
            open.setVisibility(View.VISIBLE);
          }
        });
      }
    });

    open.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        File file = new File(successPath);
        intent.setDataAndType(Uri.fromFile(file), "video/*");
        startActivity(intent);
      }
    });
  }
}

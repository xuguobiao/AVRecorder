package com.kido.tavrecorder;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kido.videorecorder.manager.RecordVideoActivity;
import com.kido.videorecorder.manager.VideoRecorder;

import java.io.File;


public class Mp4VideoRecorderActivity extends Activity {
  private TextView mStatusTextView;
  private Button mStartOnListenerButton, mStartOnResultButton, openMediaButton;

  private String successPath = "";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_mp4_video_recorder);


    mStatusTextView = (TextView) findViewById(R.id.statusText);
    mStartOnListenerButton = (Button) findViewById(R.id.startOnListener);
    mStartOnResultButton = (Button) findViewById(R.id.startOnResult);
    openMediaButton = (Button) findViewById(R.id.openMeida);

    mStartOnListenerButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startRecorderOnListener();
      }
    });

    mStartOnResultButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startRecorderOnResult();
      }
    });


    openMediaButton.setOnClickListener(new View.OnClickListener() {
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

  private void startRecorderOnListener(){
    VideoRecorder.getsInstance().startRecording(Mp4VideoRecorderActivity.this, new VideoRecorder.OnRecordListener() {
      @Override
      public void onFail() {
        mStatusTextView.setText("onFail..");
        openMediaButton.setVisibility(View.GONE);
      }

      @Override
      public void onFinish(String savePath) {
        mStatusTextView.setText("onFinish-> savePath=" + savePath);
        successPath = savePath;
        openMediaButton.setVisibility(View.VISIBLE);
      }
    });
  }

  private void startRecorderOnResult(){
    Intent intent = new Intent(this, RecordVideoActivity.class);
    startActivityForResult(intent, REQUEST_CODE_VIDEO);
  }

  private static final int REQUEST_CODE_VIDEO = 52001;

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_CODE_VIDEO) {
      if (resultCode == RESULT_OK) {
        // success
        String savePath = data.getStringExtra(RecordVideoActivity.KEY_PATH);
        mStatusTextView.setText("onFinish-> savePath=" + savePath);
        successPath = savePath;
        openMediaButton.setVisibility(View.VISIBLE);
      } else {
        // failure
        mStatusTextView.setText("onFail..");
        openMediaButton.setVisibility(View.GONE);
      }
    }

  }
}

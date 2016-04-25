package com.kido.tandroidlame;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


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
      }
    });
  }
}

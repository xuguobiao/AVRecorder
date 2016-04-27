package com.kido.videorecorder.manager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.TextView;

import com.kido.videorecorder.R;
import com.kido.videorecorder.base.PhoneUtil;
import com.kido.videorecorder.base.VideoRecorderView;

import java.io.File;

public class RecordVideoActivity extends Activity implements View.OnClickListener {

  public static final String KEY_PATH = "path";
  private View mCancelView, mOkView;
  private VideoRecorderView mRecoderView;
  private Button mVideoControllerButton;
  private TextView mMessageTextView;


  private boolean isCancel = false;

  private String mVideoSavePath = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_record);

    mCancelView = findViewById(R.id.cancelTextView);
    mOkView = findViewById(R.id.okImageView);
    mCancelView.setOnClickListener(this);
    mOkView.setOnClickListener(this);

    mRecoderView = (VideoRecorderView) findViewById(R.id.recoderView); // 这里的RecoderView需设置成和VideoRecorder录制的size比例一致
    mVideoControllerButton = (Button) findViewById(R.id.videoControllerButton);
    mMessageTextView = (TextView) findViewById(R.id.messageTextView);

    ViewGroup.LayoutParams params = mRecoderView.getLayoutParams();
    int[] dev = PhoneUtil.getResolution(this);
    params.width = dev[0];
    params.height = (int) (((float) dev[0]));
    mRecoderView.setLayoutParams(params);
    mRecoderView.setRecorderListener(new VideoRecorderListener());
    mVideoControllerButton.setOnTouchListener(new VideoTouchListener());

  }

  @Override
  protected void onStop() {
    // TODO: 2016/4/27 Kido: let stop being destroy
    super.onStop();
  }
  
  @Override
  protected  void onDestroy(){
    // TODO: 2016/4/27 Kido: do some release here
    mRecoderView.releaseAll();
    super.onDestroy();

  }

  @Override
  public void onClick(View view) {
    if (view == mCancelView) {
      sendFailMessage();
    } else if (view == mOkView) {
      if (mVideoSavePath != null) {
        sendFinishMessage(mVideoSavePath);
      }
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      return true;
    } else {
      return super.onKeyDown(keyCode, event);
    }
  }

  class VideoRecorderListener implements VideoRecorderView.RecorderListener {
    @Override
    public void recording(int maxtime, int nowtime) {
      DebugLog.log("recording->maxtime=" + maxtime + ", nowtime=" + nowtime);
    }

    @Override
    public void recordSuccess(File videoFile) {
      String videoPath = videoFile != null ? videoFile.getAbsolutePath() : null;
      DebugLog.log("recordSuccess->videoPaht=" + videoPath);
      toggleOkButton(true, videoPath);
      releaseAnimations();
    }

    @Override
    public void recordStop() {
      DebugLog.log("recordStop...");
    }

    @Override
    public void recordCancel() {
      DebugLog.log("recordCancel...");
      toggleOkButton(false, null);
      releaseAnimations();
    }

    @Override
    public void recordStart() {
      DebugLog.log("recordStart...");
      toggleOkButton(false, null);
    }

    @Override
    public void videoStop() {
      DebugLog.log("videoStop...");
    }

    @Override
    public void videoStart() {
      DebugLog.log("videoStart...");
    }
  }

  class VideoTouchListener implements View.OnTouchListener {

    @Override
    public boolean onTouch(View v, MotionEvent event) {

      switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
          mRecoderView.startRecord();// 有点耗时，影响按下的效果
          isCancel = false;
          pressAnimations();
          break;
        case MotionEvent.ACTION_MOVE:
          if (event.getX() > 0
              && event.getX() < mVideoControllerButton.getWidth()
              && event.getY() > 0
              && event.getY() < mVideoControllerButton.getHeight()) {
            showPressMessage();
            isCancel = false;
          } else {
            cancelAnimations();
            isCancel = true;
          }
          break;
        case MotionEvent.ACTION_UP:
          if (isCancel) {
            mRecoderView.cancelRecord();
          } else {
            mRecoderView.endRecord();
          }
          mMessageTextView.setVisibility(View.GONE);
          releaseAnimations();
          break;
        default:
          break;
      }
      return false;
    }
  }

  /**
   * 移动取消弹出动画
   */
  public void cancelAnimations() {
    mMessageTextView.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
    mMessageTextView.setTextColor(getResources().getColor(android.R.color.white));
    mMessageTextView.setText(R.string.let_go_cancel);
  }

  /**
   * 显示提示信息
   */
  public void showPressMessage() {
    mMessageTextView.setVisibility(View.VISIBLE);
    mMessageTextView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    mMessageTextView.setTextColor(getResources().getColor(android.R.color.holo_green_light));
    mMessageTextView.setText(R.string.move_up_cancel);
  }


  /**
   * 按下时候动画效果
   */
  public void pressAnimations() {
    AnimationSet animationSet = new AnimationSet(true);
    ScaleAnimation scaleAnimation = new ScaleAnimation(1, 1.5f,
        1, 1.5f,
        Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f);
    scaleAnimation.setDuration(200);

    AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
    alphaAnimation.setDuration(200);

    animationSet.addAnimation(scaleAnimation);
    animationSet.addAnimation(alphaAnimation);
    animationSet.setFillAfter(true);

    mVideoControllerButton.startAnimation(animationSet);
  }

  /**
   * 释放时候动画效果
   */
  public void releaseAnimations() {
    AnimationSet animationSet = new AnimationSet(true);
    ScaleAnimation scaleAnimation = new ScaleAnimation(1.5f, 1f,
        1.5f, 1f,
        Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f);
    scaleAnimation.setDuration(200);

    AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
    alphaAnimation.setDuration(200);

    animationSet.addAnimation(scaleAnimation);
    animationSet.addAnimation(alphaAnimation);
    animationSet.setFillAfter(true);

    mMessageTextView.setVisibility(View.GONE);
    mVideoControllerButton.startAnimation(animationSet);
  }

  private void toggleOkButton(boolean enable, String videoPath) {
    int okVisi = enable ? View.VISIBLE : View.INVISIBLE;
    mOkView.setEnabled(enable);
    mOkView.setVisibility(okVisi);
    mVideoSavePath = videoPath;
  }

  private void sendFailMessage() {
    DebugLog.e("sendFailMessage..");
    if (VideoRecorder.getsInstance().getOnRecordListener() != null) {
      VideoRecorder.getsInstance().getOnRecordListener().onFail();
    }
    VideoRecorder.getsInstance().setOnRecordListener(null); // prevent duplicate callback
    Intent intent = new Intent();
    setResult(Activity.RESULT_CANCELED, intent);
    finish();
  }

  private void sendFinishMessage(String savePath) {
    DebugLog.e("sendFinishMessage->savePath=" + savePath);
    if (VideoRecorder.getsInstance().getOnRecordListener() != null) {
      VideoRecorder.getsInstance().getOnRecordListener().onFinish(savePath);
    }
    VideoRecorder.getsInstance().setOnRecordListener(null); // prevent duplicate callback
    Intent intent = new Intent();
    intent.putExtra(KEY_PATH, savePath);
    setResult(Activity.RESULT_OK, intent);
    finish();
  }


}

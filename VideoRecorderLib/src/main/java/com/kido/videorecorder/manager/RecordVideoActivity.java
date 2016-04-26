package com.kido.videorecorder.manager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import com.kido.videorecorder.base.utils.FileUtil;
import com.kido.videorecorder.R;
import com.kido.videorecorder.base.camera.CameraHelper;
import com.kido.videorecorder.base.recorder.WXLikeVideoRecorder;
import com.kido.videorecorder.base.views.CameraPreviewView;

/**
 * 新视频录制页面
 *
 */
public class RecordVideoActivity extends Activity implements View.OnClickListener {

  public static final String KEY_PATH = "filePath";

  private static final String TAG = "RecordVideoActivity";

  // 输出宽度
  private static final int OUTPUT_WIDTH = 320;
  // 输出高度
  private static final int OUTPUT_HEIGHT = 240;
  // 宽高比
  private static final float RATIO = 1f * OUTPUT_WIDTH / OUTPUT_HEIGHT;

  private Camera mCamera;

  private WXLikeVideoRecorder mRecorder;

  private TextView mStartTextView;

  private boolean mIsStarted = false;
  private boolean mIsClickFinished = false;
  private int mTotalDurationSecond = -1;
  private static final long INTERVAL_TIMER_MS = 1000;// ms
  private Timer mRecorderTimer = new Timer();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    int cameraId = CameraHelper.getDefaultCameraID();
    // Create an instance of Camera
    mCamera = CameraHelper.getCameraInstance(cameraId);
    if (null == mCamera) {
      sendFailMessage(VideoRecorder.FAILURE_CODE_CAMERA_FAIL, "failed to open camera.");
      return;
    }
    // 初始化录像机
    mRecorder = new WXLikeVideoRecorder(this, FileUtil.MEDIA_FILE_DIR);
    mRecorder.setOutputSize(OUTPUT_WIDTH, OUTPUT_HEIGHT);

    setContentView(R.layout.activity_my_recorder);
    CameraPreviewView preview = (CameraPreviewView) findViewById(R.id.camera_preview);
    preview.setCamera(mCamera, cameraId);

    mRecorder.setCameraPreviewView(preview);

    mStartTextView = (TextView) findViewById(R.id.button_start);
    mStartTextView.setOnClickListener(this);

  }

  @Override
  protected void onStop() {
    super.onStop();
    stopAndRelease();
    if (!mIsClickFinished) {
      sendFailMessage(VideoRecorder.FAILURE_CODE_ON_PAUSE, "recorder activity is paused.");
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mTimerTask.cancel();
    mRecorderTimer.cancel();
    mTimerTask = null;
    mRecorderTimer = null;
    releaseCamera();              // release the camera immediately on pause event
  }

  private TimerTask mTimerTask = new TimerTask() {
    @Override
    public void run() {
      if (mTotalDurationSecond > WXLikeVideoRecorder.MAX_RECORD_TIME / 1000) {
        this.cancel();
        stopRecord();
        return;
      }
      mTotalDurationSecond++;
      final String timeString = formatSecond(mTotalDurationSecond);
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          mStartTextView.setText(timeString);
        }
      });
    }
  };

  private String formatSecond(int totalSecond) {
    totalSecond = totalSecond < 0 ? 0 : totalSecond;
    int minute = totalSecond / 60;
    int second = totalSecond % 60;

    String minuteStr = minute < 10 ? "0" + minute : minute + "";
    String secondStr = second < 10 ? "0" + second : second + "";

    String timeStr = minuteStr + ":" + secondStr;
    return timeStr;
  }


  private void stopAndRelease() {
    if (mRecorder != null && mRecorder.isRecording()) {
      // 页面不可见就要停止录制
      mRecorder.stopRecording();
      // 录制时退出，直接舍弃视频
    }
    releaseCamera();              // release the camera immediately on pause event
  }

  private void releaseCamera() {
    if (mCamera != null) {
      mCamera.setPreviewCallback(null);
      // 释放前先停止预览
      mCamera.stopPreview();
      mCamera.release();        // release the camera for other applications
      mCamera = null;
    }
  }


  /**
   * 开始录制
   */
  private void startRecord() {
    if (mRecorder.isRecording()) {
      return;
    }
    if (!FileUtil.isSDCardMounted()) {
      sendFailMessage(VideoRecorder.FAILURE_CODE_SDCARD_ERROR, "sdcard id unmounted.");
      return;
    }
    // TODO: 2016/4/26  Kido: do other check here
    // initialize video camera
    // 录制视频
    if (!mRecorder.startRecording()) {
      sendFailMessage(VideoRecorder.FAILURE_CODE_WRITE_ERROR, "video write error.");
    } else {
      mTotalDurationSecond = -1;
      mRecorderTimer.schedule(mTimerTask, 0, INTERVAL_TIMER_MS);
    }
  }


  /**
   * 停止录制
   */
  private void stopRecord() {
    mRecorder.stopRecording();
    mTimerTask.cancel();
    mRecorderTimer.cancel();
    String videoPath = mRecorder.getFilePath();
    // 没有录制视频
    if (null == videoPath) {
      sendFailMessage(VideoRecorder.FAILURE_CODE_FILE_CREATE_ERROR, "no video file.");
      return;
    }
    // 告诉宿主页面录制视频的路径
//            startActivity(new Intent(this, PlayVideoActiviy.class).putExtra(PlayVideoActiviy.KEY_FILE_PATH, videoPath));
    // TODO: 2016/4/25 callback here
    sendFinishMessage(mTotalDurationSecond, videoPath);
  }

  @Override
  public void onClick(View v) {
    if (v == mStartTextView) {
      if (mIsStarted) {
        mIsClickFinished = true;
        stopRecord();
      } else {
//        mStartTextView.setText("");
        startRecord();
      }
      mIsStarted = !mIsStarted;
    }
  }


  private void sendFailMessage(int failCode, String failMessage) {
    if (VideoRecorder.getInstance().getOnRecordListener() != null) {
      DebugLog.e("sendFailMessage->failCode=" + failCode + ", failMessage=" + failMessage);
      VideoRecorder.getInstance().getOnRecordListener().onFail(failCode, failMessage);
    }
    String videoPath = mRecorder.getFilePath();
    if (!TextUtils.isEmpty(videoPath)) {
      new Thread(new StartRecordFailCallbackRunnable(this)).start();
    }
    VideoRecorder.getInstance().setOnRecordListener(null); // prevent duplicate callback
    finish();
  }

  private void sendFinishMessage(int totalDurationSecond, String savePath) {
    if (VideoRecorder.getInstance().getOnRecordListener() != null) {
      DebugLog.e("sendFinishMessage->totalDurationSecond=" + totalDurationSecond + ", savePath=" + savePath);
      VideoRecorder.getInstance().getOnRecordListener().onFinish(totalDurationSecond, savePath);
    }
    VideoRecorder.getInstance().setOnRecordListener(null); // prevent duplicate callback
    finish();
  }

  /**
   * 开始录制失败回调任务
   *
   * @author Martin
   */
  public static class StartRecordFailCallbackRunnable implements Runnable {

    private WeakReference<RecordVideoActivity> mNewRecordVideoActivityWeakReference;

    public StartRecordFailCallbackRunnable(RecordVideoActivity activity) {
      mNewRecordVideoActivityWeakReference = new WeakReference<>(activity);
    }

    @Override
    public void run() {
      RecordVideoActivity activity;
      if (null == (activity = mNewRecordVideoActivityWeakReference.get()))
        return;

      String filePath = activity.mRecorder.getFilePath();
      if (!TextUtils.isEmpty(filePath)) {
        FileUtil.deleteFile(filePath);
        DebugLog.e("video fail, delete file->" + filePath);
      }
    }
  }
//
//  /**
//   * 停止录制回调任务
//   *
//   * @author Martin
//   */
//  public static class StopRecordCallbackRunnable implements Runnable {
//
//    private WeakReference<RecordVideoActivity> mNewRecordVideoActivityWeakReference;
//
//    public StopRecordCallbackRunnable(RecordVideoActivity activity) {
//      mNewRecordVideoActivityWeakReference = new WeakReference<>(activity);
//    }
//
//    @Override
//    public void run() {
//      RecordVideoActivity activity;
//      if (null == (activity = mNewRecordVideoActivityWeakReference.get()))
//        return;
//
//      String filePath = activity.mRecorder.getFilePath();
//      if (!TextUtils.isEmpty(filePath)) {
//        if (activity.isCancelRecord) {
//          FileUtil.deleteFile(filePath);
//        } else {
//          Toast.makeText(activity, "Video file path: " + filePath, Toast.LENGTH_LONG).show();
//        }
//      }
//    }
//  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      if (mRecorder != null && mRecorder.isRecording()) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.wenxin_tip));
        builder.setMessage(getString(R.string.current_video_recording));
        builder.setPositiveButton(R.string.cancel, null);
        builder.setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            sendFailMessage(VideoRecorder.FAILURE_CODE_ON_BACK, "user click back while recording.");
          }
        });
        builder.create().show();
      }
      return true;
    } else {
      return super.onKeyDown(keyCode, event);
    }
  }
}

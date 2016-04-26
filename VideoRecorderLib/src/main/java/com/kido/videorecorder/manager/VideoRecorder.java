package com.kido.videorecorder.manager;

import android.content.Context;
import android.content.Intent;

/**
 * @author XuGuobiao
 * @email everlastxgb@gmail.com
 * @create_date 2016/4/26
 */
public class VideoRecorder {


  public static final int FAILURE_CODE_INIT_ERROR = -10;

  public static final int FAILURE_CODE_PERMISSION_DENY = -100;
  public static final int FAILURE_CODE_FILE_CREATE_ERROR = -101;
  public static final int FAILURE_CODE_DURATION_TOO_SHORT = -102;
  public static final int FAILURE_CODE_WRITE_ERROR = -103;
  public static final int FAILURE_CODE_UNDER_RECORDING = -104;
  public static final int FAILURE_CODE_SDCARD_ERROR = -105;
  public static final int FAILURE_CODE_ON_PAUSE = -106;
  public static final int FAILURE_CODE_ON_BACK = -107;
  public static final int FAILURE_CODE_CAMERA_FAIL = -108;
//  public static final int FAILURE_CODE_DURATION_TOO_LONG = -108;

  public static final int FAILURE_CODE_EXEPTION = -500;

  private static VideoRecorder instance;

  private OnRecordListener mOnRecordListener;

  public interface OnRecordListener {

    void onFail(int failCode, String failMessage);

    void onFinish(int totalDurationSecond, String savePath);

  }

  // 以为目前视频录制时打开activity，为了达到callback的效果而不是onResult监听，此处用了单例
  public static synchronized VideoRecorder getInstance() {
    if (instance == null) {
      instance = new VideoRecorder();
    }
    return instance;
  }

  /**
   * this will open the recorder activity
   */
  public void startRecording(Context context, OnRecordListener listener) {
    mOnRecordListener = listener;
    Intent intent = new Intent(context, RecordVideoActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(intent);
  }

  public OnRecordListener getOnRecordListener() {
    return mOnRecordListener;
  }

  void setOnRecordListener(OnRecordListener listener) {
    mOnRecordListener = listener;
  }
}

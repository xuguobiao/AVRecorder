package com.kido.videorecorder.base;

import android.content.Context;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.kido.videorecorder.R;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class VideoRecorderView extends LinearLayout implements MediaRecorder.OnErrorListener {

  // 视频展示
  private SurfaceView mSurfaceView;
  private SurfaceHolder mSurfaceHolder;

  private SurfaceView mVideoSurfaceView;
  private ImageView mPlayVideoImageView;

  // 进度条
  private ProgressBar mProgressBar_left;
  private ProgressBar mProgressBar_right;

  // 录制视频
  private MediaRecorder mMediaRecorder;
  // 摄像头
  private Camera mCamera;
  private Timer mTimer;

  // 视频播放
  private MediaPlayer mMediaPlayer;

  // 时间限制
  private static final int sRecordMaxTime = Config.VIDEO_MAX_DURATION_SECOND;
  private int mTimeCount;
  // 生成的文件
  private File mRecordFile;

  private Context mContext;
  private UIHandler mHandler;

  // 正在录制
  private boolean mIsRecording = false;
  // 录制成功
  private boolean mIsSuccess = false;

  private RecorderListener mRecorderListener;

  public VideoRecorderView(Context context) {
    this(context, null);
  }

  public VideoRecorderView(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.mContext = context;
    init();
  }

//  public VideoRecorderView(Context mContext, AttributeSet attrs, int defStyleAttr) {
//    super(mContext, attrs, defStyleAttr);
//    this.mContext = mContext;
//  }

  private void init() {
    mHandler = new UIHandler(this);

    LayoutInflater.from(mContext).inflate(R.layout.ui_recorder, this);
    mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
    mVideoSurfaceView = (SurfaceView) findViewById(R.id.playView);
    mPlayVideoImageView = (ImageView) findViewById(R.id.playVideoImageView);

    mProgressBar_left = (ProgressBar) findViewById(R.id.progressBar_left);
    mProgressBar_right = (ProgressBar) findViewById(R.id.progressBar_right);

    mProgressBar_left.setMax(sRecordMaxTime * 20);
    mProgressBar_right.setMax(sRecordMaxTime * 20);

    mProgressBar_left.setProgress(sRecordMaxTime * 20);

    mSurfaceHolder = mSurfaceView.getHolder();
    mSurfaceHolder.addCallback(new CustomCallBack());
    mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    initCamera();

    mPlayVideoImageView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        playVideo();
      }
    });
  }

  private class CustomCallBack implements SurfaceHolder.Callback {

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
      initCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
      freeCameraResource();
    }
  }


  @Override
  public void onError(MediaRecorder mr, int what, int extra) {
    try {
      if (mr != null)
        mr.reset();
    } catch (Exception e) {

    }
  }

  /**
   * 初始化摄像头
   */
  private void initCamera() {
    if (mCamera != null)
      freeCameraResource();
    try {
      mCamera = Camera.open();
    } catch (Exception e) {
      e.printStackTrace();
      freeCameraResource();
    }
    if (mCamera == null)
      return;

    mCamera.setDisplayOrientation(Config.VIDEO_ORIENTATION);

    try {
      mCamera.setPreviewDisplay(mSurfaceHolder);
    } catch (IOException e) {
      e.printStackTrace();
    }
    mCamera.startPreview();

    try {
      String focusMode = mCamera.getParameters().getFocusMode();
      if (Camera.Parameters.FOCUS_MODE_AUTO.equals(focusMode) || Camera.Parameters.FOCUS_MODE_MACRO.equals(focusMode))
        mCamera.autoFocus(null);// This method is only valid when preview is active (between startPreview() and before stopPreview()).
    } catch (Exception e) {
      Log.e("kido", "autoFocus exception->" + e.getMessage());
    }

    mCamera.unlock();
  }

  /**
   * 初始化摄像头配置
   */
  private void initRecord() {
    mMediaRecorder = new MediaRecorder();
    mMediaRecorder.reset();
    if (mCamera != null)
      mMediaRecorder.setCamera(mCamera);

    mMediaRecorder.setOnErrorListener(this);
    mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
    mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
    mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

    mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
    mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
    mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);

    mMediaRecorder.setVideoSize(Config.VIDEO_WIDTH, Config.VIDEO_HEIGHT);
    mMediaRecorder.setVideoFrameRate(Config.VIDEO_FRAME_RATE);
    mMediaRecorder.setVideoEncodingBitRate(Config.VIDEO_ENCODING_BIT_RATE);
//        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
    mMediaRecorder.setOrientationHint(Config.VIDEO_ORIENTATION);

    mMediaRecorder.setMaxDuration(sRecordMaxTime * 1000);
    mMediaRecorder.setOutputFile(mRecordFile.getAbsolutePath());
  }

  private void prepareRecord() {
    try {
      mMediaRecorder.prepare();
      mMediaRecorder.start();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * 开始录制
   */
  public void startRecord() {

    // 录制中
    if (mIsRecording)
      return;
    // 创建文件
    createRecordDir();

    initCamera();

    mVideoSurfaceView.setVisibility(View.GONE);
    mPlayVideoImageView.setVisibility(View.GONE);
    mSurfaceView.setVisibility(View.VISIBLE);

    // 初始化控件
    initRecord();
    prepareRecord();
    mIsRecording = true;
    if (mRecorderListener != null)
      mRecorderListener.recordStart();
    // 到底可录制的最大时长就自动化结束
    mTimeCount = 0;
    mTimer = new Timer();
    mTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        mTimeCount++;
        mProgressBar_left.setProgress(mTimeCount);
        mProgressBar_right.setProgress(sRecordMaxTime * 20 - mTimeCount);
        if (mRecorderListener != null)
          mRecorderListener.recording(sRecordMaxTime * 1000, mTimeCount * 50);
        if (mTimeCount == sRecordMaxTime * 20) {
          Message message = new Message();
          message.what = MESSAGE_VIDEO_END;
          mHandler.sendMessage(message);
        }
      }
    }, 0, 50);
  }

  private static final int MESSAGE_VIDEO_END = 1;

  private static class UIHandler extends Handler {
    private VideoRecorderView mRecorderView = null;

    public UIHandler(VideoRecorderView videoRecorderView) {
      mRecorderView = new WeakReference<VideoRecorderView>(videoRecorderView).get();
    }

    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      if (mRecorderView == null) {
        return;
      }
      switch (msg.what) {
        case MESSAGE_VIDEO_END:
          mRecorderView.endRecord();
          break;
      }
    }
  }

  /**
   * 停止录制
   */
  public void endRecord() {
    if (!mIsRecording)
      return;
    mIsRecording = false;
    if (mRecorderListener != null) {
      mRecorderListener.recordStop();
      mRecorderListener.recordSuccess(mRecordFile);
    }
    stopRecord();
    releaseRecord();
    freeCameraResource();
    mVideoSurfaceView.setVisibility(View.VISIBLE);
    mPlayVideoImageView.setVisibility(View.VISIBLE);
  }

  /**
   * 取消录制
   */
  public void cancelRecord() {
    mVideoSurfaceView.setVisibility(View.GONE);
    mPlayVideoImageView.setVisibility(View.GONE);
    mSurfaceView.setVisibility(View.VISIBLE);
    if (!mIsRecording)
      return;
    mIsRecording = false;
    stopRecord();
    releaseRecord();
    freeCameraResource();
    mIsRecording = false;
    if (mRecordFile.exists())
      mRecordFile.delete();
    if (mRecorderListener != null)
      mRecorderListener.recordCancel();
    initCamera();
  }

  /**
   * 停止录制
   */
  private void stopRecord() {
    mProgressBar_left.setProgress(sRecordMaxTime * 20);
    mProgressBar_right.setProgress(0);

    if (mTimer != null)
      mTimer.cancel();
    if (mMediaRecorder != null) {
      // 设置后不会崩
      mMediaRecorder.setOnErrorListener(null);
      mMediaRecorder.setPreviewDisplay(null);
      try {
        mMediaRecorder.stop();
      } catch (IllegalStateException e) {
        e.printStackTrace();
      } catch (RuntimeException e) {
        e.printStackTrace();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }


  public void destoryMediaPlayer() {
    if (mMediaPlayer == null)
      return;
    mMediaPlayer.setDisplay(null);
    mMediaPlayer.reset();
    mMediaPlayer.release();
    mMediaPlayer = null;
  }

  /**
   * 播放视频
   */
  public void playVideo() {
    mSurfaceView.setVisibility(View.GONE);
    mVideoSurfaceView.setVisibility(View.VISIBLE);
    mPlayVideoImageView.setVisibility(View.GONE);
    mMediaPlayer = new MediaPlayer();
    try {
      mMediaPlayer.reset();
      mMediaPlayer.setDataSource(mRecordFile.getAbsolutePath());
      mMediaPlayer.setDisplay(mVideoSurfaceView.getHolder());
      mMediaPlayer.prepare();// 缓冲
      mMediaPlayer.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (mRecorderListener != null)
      mRecorderListener.videoStart();
    mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override
      public void onCompletion(MediaPlayer mp) {
        if (mRecorderListener != null)
          mRecorderListener.videoStop();
        mPlayVideoImageView.setVisibility(View.VISIBLE);
      }
    });
  }

  public RecorderListener getRecorderListener() {
    return mRecorderListener;
  }

  public void setRecorderListener(RecorderListener mRecorderListener) {
    this.mRecorderListener = mRecorderListener;
  }

  public SurfaceView getSurfaceView() {

    return mSurfaceView;
  }

  public void setSurfaceView(SurfaceView surfaceView) {
    this.mSurfaceView = surfaceView;
  }

  public MediaPlayer getMediaPlayer() {
    return mMediaPlayer;
  }

  public interface RecorderListener {

    void recording(int maxtime, int nowtime);

    void recordSuccess(File videoFile);

    void recordStop();

    void recordCancel();

    void recordStart();

    void videoStop();

    void videoStart();
  }


  /**
   * 创建视频文件
   */
  private void createRecordDir() {
    File sampleDir = new File(Consts.MEDIA_RECORD_PATH);
    if (!sampleDir.exists()) {
      sampleDir.mkdirs();
    }
    File vecordDir = sampleDir;
    // 创建文件
    mRecordFile = new File(vecordDir, Consts.RECORDING_TMP_MP4_NAME);
//    try {
//      mRecordFile = File.createTempFile(Consts.PREF_RECORDING, ".mp4", vecordDir);// mp4格式
//    } catch (IOException e) {
//    }
  }

  private void deleteRecordDir() {
    if (mRecordFile != null) {
      if (mRecordFile.exists()) {
        mRecordFile.delete();
      }
    }
  }

  /**
   * 释放资源
   */
  private void releaseRecord() {
    if (mMediaRecorder != null) {
      mMediaRecorder.setOnErrorListener(null);
      try {
        mMediaRecorder.release();
      } catch (IllegalStateException e) {
        e.printStackTrace();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    mMediaRecorder = null;
  }

  /**
   * 释放摄像头资源
   */
  private void freeCameraResource() {
    if (null != mCamera) {
      mCamera.setPreviewCallback(null);
      mCamera.stopPreview();
      mCamera.lock();// Since API level 14, camera is automatically locked for applications in start().
      mCamera.release();
      mCamera = null;
    }
  }
}

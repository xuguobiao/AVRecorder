package com.kido.androidlame.manager;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;

import com.kido.androidlame.base.AndroidLame;
import com.kido.androidlame.base.LameBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * 供录音并保存为mp3文件 <br> Created by Kido on 16/4/24.
 */
public class VoiceRecorder {

  public static final int FAILURE_CODE_PERMISSION_DENY = -100;
  public static final int FAILURE_CODE_FILE_CREATE_ERROR = -101;
  public static final int FAILURE_CODE_DURATION_TOO_SHORT = -102;
  public static final int FAILURE_CODE_WRITE_ERROR = -103;
  public static final int FAILURE_CODE_UNDER_RECORDING = -104;

  public static final int FAILURE_CODE_EXEPTION = -500;

  private static final int DEFAULT_IN_SAMPLE_RATE = 8000;
  private static final String SUFFIX_MP3 = ".mp3";


  /**
   * 录音允许最短的时间(s)
   */
  private int mMinDurationSecond = 0;

  /**
   * 录音允许最长的时间(s)
   */
  private int mMaxDurationSecond = Integer.MAX_VALUE / 2;

  /**
   * 记录当前是否正在录音状态
   */
  private boolean mIsRecording = false;

  private OnRecordListener mOnRecordListener = null;

  private ResultHandler mHandler;


  public interface OnRecordListener {
    void onRecord(int curDurationSecond);

    /**
     * @param failCode    VoiceRecorder.ILURE_CODE_XXX
     * @param failMessage 失败信息,可能为空
     */
    void onFail(int failCode, String failMessage);

    void onFinish(int totalDurationSecond, String savePath);
  }

  public VoiceRecorder() {
    this(0, Integer.MAX_VALUE / 2);
  }

  /**
   * @param minDurationSecond 录音允许最短的时间,单位为秒
   * @param maxDurationSecond 录音允许最长的时间,单位为秒
   */
  public VoiceRecorder(int minDurationSecond, int maxDurationSecond) {
    this.mMinDurationSecond = minDurationSecond;
    this.mMaxDurationSecond = maxDurationSecond;
    this.mHandler = new ResultHandler(this);
  }

  public long getMinDurationSecond() {
    return mMinDurationSecond;
  }

  public void setMinDuration(int mMinDurationSecond) {
    this.mMinDurationSecond = mMinDurationSecond;
  }

  public long getMaxDurationSecond() {
    return mMaxDurationSecond;
  }

  public void setMaxDuration(int mMaxDurationSecond) {
    this.mMaxDurationSecond = mMaxDurationSecond;
  }

  public boolean isRecording() {
    return mIsRecording;
  }

  public OnRecordListener getOnRecordListener() {
    return mOnRecordListener;
  }

  /**
   * @param savePath         录音成功后的mp3文件保存的绝对路径
   * @param onRecordListener 录音状态的监听回调
   */
  public void startRecording(String savePath, OnRecordListener onRecordListener) {
    mOnRecordListener = onRecordListener;
    if (isRecording()) {
      sendFailMessage(FAILURE_CODE_UNDER_RECORDING, "it is under recording, please stop it before restart.");
      return;
    }
    // TODO: 16/4/24 Kido:  check others here, like permission,sdcard mounted...

    mIsRecording = true;
    new Thread(new RecordRunnable(savePath)).start();
  }


  public void stopRecording() {
    mIsRecording = false;
  }

  private File checkAndCreateFile(String filePath) {
    File destFile = new File(filePath);
    if (!destFile.getParentFile().exists()) {
      destFile.getParentFile().mkdirs();
    }
    if (!destFile.exists()) {
      DebugLog.log("dest file not exists, creating new file..");
      try {
        destFile.createNewFile();
        return destFile;
      } catch (IOException e) {
        DebugLog.e("createNewFile->" + e.getMessage());

      }
    }
    return null;
  }

  private class RecordRunnable implements Runnable {

    private String mSavePath = "";

    public RecordRunnable(String savePath) {
      this.mSavePath = savePath + "";
    }

    @Override
    public void run() {
      boolean isFinished = false;
      String filePath = mSavePath.endsWith(SUFFIX_MP3) ? mSavePath : mSavePath + SUFFIX_MP3;
      File destFile = checkAndCreateFile(filePath);

      if (destFile == null) {
        sendFailMessage(FAILURE_CODE_FILE_CREATE_ERROR, "");
        return;
      }

      AudioRecord audioRecord = null;
      AndroidLame androidLame = null;
      FileOutputStream outputStream = null;

      try {
        int minBuffer = AudioRecord.getMinBufferSize(DEFAULT_IN_SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT);

        DebugLog.log("Initialising audio recorder..");
        audioRecord = new AudioRecord(
            MediaRecorder.AudioSource.MIC, DEFAULT_IN_SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT, minBuffer * 2);

        //5 seconds data
        DebugLog.log("creating short buffer array");
        short[] buffer = new short[DEFAULT_IN_SAMPLE_RATE * 2 * 5];

        // 'mp3buf' should be at least 7200 bytes long to hold all possible emitted data.
        DebugLog.log("creating mp3 buffer");
        byte[] mp3buffer = new byte[(int) (7200 + buffer.length * 2 * 1.25)];
        outputStream = new FileOutputStream(new File(mSavePath));

        DebugLog.log("Initialising Android Lame");
        androidLame = new LameBuilder()
            .setInSampleRate(DEFAULT_IN_SAMPLE_RATE)
            .setOutChannels(1)
            .setOutBitrate(32)
            .setOutSampleRate(DEFAULT_IN_SAMPLE_RATE)
            .build();

        DebugLog.log("started audio recording..");
        long startTime = System.currentTimeMillis();
        audioRecord.startRecording();

        int bytesRead = 0;
        int recordDuration = 0;
        while (mIsRecording) {

          DebugLog.log("reading to short array buffer, buffer sze- " + minBuffer);
          bytesRead = audioRecord.read(buffer, 0, minBuffer);
          DebugLog.log("bytes read=" + bytesRead);

          if (bytesRead > 0) {

            DebugLog.log("encoding bytes to mp3 buffer..");
            int bytesEncoded = androidLame.encode(buffer, buffer, bytesRead, mp3buffer);
            DebugLog.log("bytes encoded=" + bytesEncoded);

            if (bytesEncoded > 0) {
              DebugLog.log("writing mp3 buffer to outputstream with " + bytesEncoded + " bytes");
              outputStream.write(mp3buffer, 0, bytesEncoded);
            }
          }

          int curDuration = (int) ((System.currentTimeMillis() - startTime) / 1000);//s
          if (curDuration > recordDuration) { // duration有增加才会回调onRecord()
            recordDuration = curDuration;
            sendRecordingMessage(recordDuration);
          }
          if (curDuration >= mMaxDurationSecond) { // 检查录音时间是否超长了
            break;
          }
        }// end while

        if (recordDuration < mMinDurationSecond) {// 检查录音时间是否过短
          sendFailMessage(FAILURE_CODE_DURATION_TOO_SHORT, "current record duration is too short, duration=" + recordDuration + "s");
          return;
        }

        DebugLog.log("stopped recording, flushing final mp3buffer");

        int outputMp3buf = androidLame.flush(mp3buffer);
        DebugLog.log("flushed " + outputMp3buf + " bytes");

        if (outputMp3buf > 0) {
          DebugLog.log("writing final mp3buffer to outputstream");
          outputStream.write(mp3buffer, 0, outputMp3buf);
        }
        DebugLog.e("Output recording saved in " + mSavePath);
        isFinished = true;
        sendFinishMessage(recordDuration, mSavePath);

      } catch (Exception e) {

        DebugLog.e("write exception->" + e.getMessage());
        sendFailMessage(FAILURE_CODE_WRITE_ERROR, e.getMessage());


      } finally {
        mIsRecording = false;
        DebugLog.log("deleting fail file");
        if (!isFinished && destFile != null) { // 失败的情况下要删除先前创建的文件
          destFile.delete();
        }
        DebugLog.log("releasing audio recorder");
        if (audioRecord != null) {
          try {
            audioRecord.stop();
            audioRecord.release();
          } catch (Exception e1) {
          }
        }

        DebugLog.log("closing android lame");
        if (androidLame != null) {
          try {
            androidLame.close();
          } catch (Exception e2) {
          }
        }
        DebugLog.log("closing output stream");
        if (outputStream != null) {
          try {
            outputStream.close();
          } catch (IOException e3) {
          }
        }

      }

    }
  }

  private final static int MESSAGE_RECORD_FAIL = 0;
  private final static int MESSAGE_RECORD_FINISH = 1;
  private final static int MESSAGE_RECORD_ING = 2;

//  private final static String KEY_DURATION = "duration";
//  private final static String KEY_INFO= "info";

  /**
   * using this Handler to callback to the calling thread
   */
  private static class ResultHandler extends Handler {

    private VoiceRecorder mVoiceRecorder;

    public ResultHandler(VoiceRecorder voiceRecorder) {
      super();
      this.mVoiceRecorder = new WeakReference<VoiceRecorder>(voiceRecorder).get();
    }

    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      if (mVoiceRecorder == null) {
        return;
      }
      switch (msg.what) {
        case MESSAGE_RECORD_FAIL:
          int failCode = msg.arg1;
          String failMessage = String.valueOf(msg.obj);
          if (mVoiceRecorder.getOnRecordListener() != null) {
            mVoiceRecorder.getOnRecordListener().onFail(failCode, failMessage);
          }
          break;
        case MESSAGE_RECORD_FINISH:
          int totalDuration = msg.arg1;
          String savePath = String.valueOf(msg.obj);
          if (mVoiceRecorder.getOnRecordListener() != null) {
            mVoiceRecorder.getOnRecordListener().onFinish(totalDuration, savePath);
          }
          break;
        case MESSAGE_RECORD_ING:
          int curDuration = msg.arg1;
          if (mVoiceRecorder.getOnRecordListener() != null) {
            mVoiceRecorder.getOnRecordListener().onRecord(curDuration);
          }
          break;


      }

    }
  }

  private void sendFailMessage(int failCode, String failMessage) {
    if (mOnRecordListener != null) {
      Message msg = mHandler.obtainMessage();
      msg.what = MESSAGE_RECORD_FAIL;
      msg.arg1 = failCode;
      msg.obj = failMessage;
      mHandler.sendMessage(msg);
    }
  }

  private void sendFinishMessage(int duration, String savePath) {
    if (mOnRecordListener != null) {
      Message msg = mHandler.obtainMessage();
      msg.what = MESSAGE_RECORD_FINISH;
      msg.arg1 = duration;
      msg.obj = savePath;
      mHandler.sendMessage(msg);
    }
  }

  private void sendRecordingMessage(int duration) {
    if (mOnRecordListener != null) {
      Message msg = mHandler.obtainMessage();
      msg.what = MESSAGE_RECORD_ING;
      msg.arg1 = duration;
      mHandler.sendMessage(msg);
    }
  }


}

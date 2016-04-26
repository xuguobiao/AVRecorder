package com.kido.videorecorder.base;

import android.os.Environment;

/**
 * Created by Kido on 16/4/24.
 */
public class Consts {

  private final static String FOLDER_NAME = "KidoRecorder";
  private final static String ROOT_PATH = Environment.getExternalStorageDirectory() + "/" + FOLDER_NAME;
  public final static String MEDIA_PATH = ROOT_PATH + "/" + "Media";
  public final static String MEDIA_RECORD_PATH = MEDIA_PATH + "/" + "Record";
  public final static String PREF_RECORDING = "recording_";
  public final static String RECORDING_TMP_MP4_NAME = "recording_tmp.mp4";
}

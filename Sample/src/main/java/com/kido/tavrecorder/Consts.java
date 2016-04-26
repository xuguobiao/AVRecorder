package com.kido.tavrecorder;

import android.os.Environment;

/**
 * Created by Kido on 16/4/24.
 */
public class Consts {

  private final static String FOLDER_NAME = "KidoRecorder";
  private final static String ROOT_PATH = Environment.getExternalStorageDirectory() + "/" + FOLDER_NAME;
  public final static String MEDIA_PATH = ROOT_PATH + "/" + "media";
}

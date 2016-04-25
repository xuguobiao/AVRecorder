package com.kido.androidlame.manager;

/**
 * Created by Kido on 16/4/24.
 */
public class RecordException extends Exception {

  private int mCode;

  public RecordException(int code, String detailMessage) {
    super(detailMessage);
    mCode = code;
  }

  public int getCode() {
    return mCode;
  }
}

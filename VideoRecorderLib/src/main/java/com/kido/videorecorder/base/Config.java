package com.kido.videorecorder.base;

/**
 * @author XuGuobiao
 * @email everlastxgb@gmail.com
 * @create_date 2016/4/27
 */
public class Config {

  public static final int VIDEO_WIDTH = 352 * 2;
  public static final int VIDEO_HEIGHT = 288 * 2;
  // CamcorderProfile.QUALITY_CIF 352*288 horizontal
  // 4CIF (704x576)

  public static final int VIDEO_FRAME_RATE = 25;
  public static final int VIDEO_ENCODING_BIT_RATE = 768 * 1024 *2;

  public static final int VIDEO_ORIENTATION = 90; // vertical: 90
  public static final int VIDEO_MAX_DURATION_SECOND = 10; // second

}

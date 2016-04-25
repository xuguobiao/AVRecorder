# KidoAVRecorder

Audio and Video Recorder project created by Kido. 
Audio Recorder based on AndroidLame written by Naman Dwivedi.
Video Recorder based on VideoRecorder written by qdrzwd.

To be continued..

#Usage

###AndroidManifest.xml

```xml
  <uses-permission android:name="android.permission.INTERNET"/>
  <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
  <uses-permission android:name="android.permission.RECORD_AUDIO"/>
  <uses-permission android:name="android.permission.WAKE_LOCK"/>
  <uses-permission android:name="android.permission.CAMERA"/>

  <permission
      android:name="android.permission.FLASHLIGHT"
      android:permissionGroup="android.permission-group.HARDWARE_CONTROLS"
      android:protectionLevel="normal"/>

  <uses-feature android:name="android.hardware.camera"/>
  <uses-feature
      android:name="android.hardware.camera.front"
      android:required="false"/>

   <!-- for Video Recorder -->
   <activity
     android:name="com.kido.videorecorder.manager.FFmpegRecorderActivity"
     android:screenOrientation="portrait"
     android:theme="@android:style/Theme.NoTitleBar.Fullscreen"/>

```

###Java Code

###### VoiceRecorder
```java
VoiceRecorder voiceRecorder = new VoiceRecorder(minDurationSecond, maxDurationSecond);// init recorder

String savePath = Consts.MEDIA_PATH + "/" + System.currentTimeMillis() + ".mp3";

// start recorder
voiceRecorder.startRecording(savePath, new VoiceRecorder.OnRecordListener() {
  @Override
  public void onRecord(int curDurationSecond) {
  }

  @Override
  public void onFail(int failCode, String failMessage) {
  }

  @Override
  public void onFinish(int totalDurationSecond, String savePath) {
  }
});

// stop recorder
voiceRecorder.stopRecording();

// failure code below in VoiceRecorder
  public static final int FAILURE_CODE_PERMISSION_DENY = -100;
  public static final int FAILURE_CODE_FILE_CREATE_ERROR = -101;
  public static final int FAILURE_CODE_DURATION_TOO_SHORT = -102;
  public static final int FAILURE_CODE_WRITE_ERROR = -103;
  public static final int FAILURE_CODE_UNDER_RECORDING = -104;

  public static final int FAILURE_CODE_EXEPTION = -500;
  
```
###### VideoRecorder
```java
// qdrzwd's project(*.so) seems too large

```

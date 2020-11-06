package com.giosis.util.qdrive.barcodescanner;


import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.util.Log;

import com.giosis.util.qdrive.international.MyApplication;
import com.giosis.util.qdrive.international.R;

import java.io.IOException;


public final class BeepManager {

    String TAG = "BeepManager";

    static int BELL_SOUNDS_SUCCESS = 1; //띵동
    static int BELL_SOUNDS_ERROR = 2; //삐~
    static int BELL_SOUNDS_DUPLE = 3; // 삐비~

    private static final float BEEP_VOLUME = 0.10f;
    private static final long VIBRATE_DURATION = 200L;

    private final Activity activity;
    private MediaPlayer mediaPlayer;
    private boolean vibrate;
    private int sound;


    BeepManager(Activity activity) {

        this.activity = activity;
        this.mediaPlayer = null;

        updatePrefs();
    }


    void updatePrefs() {

        String vibrationString = MyApplication.preferences.getScanVibration();

        if (vibrationString.equals("ON")) {

            vibrate = true;
        } else {

            vibrate = false;
        }

        if (mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it too loud,
            // so we now play on the music stream.

            activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
            //   mediaPlayer = buildMediaPlayer(activity);
        }
    }

    void playBeepSoundAndVibrate(int sound) {

        AssetFileDescriptor file = null;

        if (sound == 1) {
            file = activity.getResources().openRawResourceFd(R.raw.bell);
        } else if (sound == 2) {
            file = activity.getResources().openRawResourceFd(R.raw.beep_error);
        } else if (sound == 3) {
            file = activity.getResources().openRawResourceFd(R.raw.beep_double);
        }


        mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());

            file.close();
            mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
            mediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mediaPlayer != null) {
            // 바코드 인식 소리 높임, 90% 보다 작을 때 90%로
            // eylee 20150709 start
            AudioManager audio = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);

            int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
            int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

            float percent = 0.9f;

            int ninetyVolume = (int) (maxVolume * percent);
            if (currentVolume < ninetyVolume) {
                audio.setStreamVolume(AudioManager.STREAM_MUSIC, ninetyVolume, 0);
            }


            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    mediaPlayer.seekTo(0);
                }


                // TODO TEST  테스트하기 (시끌~)
                if (MyApplication.preferences.getUserId().equals("karam.kim") && ManualHelper.MOBILE_SERVER_URL.contains("test")) {

                    Log.e("krm0219", "Sound Start");
                } else if (MyApplication.preferences.getUserId().equals("jay.cho") && ManualHelper.MOBILE_SERVER_URL.contains("staging")) {

                    Log.e("krm0219", "Sound Start");
                } else {

                    mediaPlayer.start();
                }
            } catch (Exception e) {

                Log.e(TAG, "Beep Sound Start Exception : " + e.toString());
            }
        }


        Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        Log.e("krm0219", "playBeepSoundAndVibrate  VibrationString " + vibrate + " / " + sound);

        if (vibrate) {
            if (vibrator != null) {

                vibrator.vibrate(VIBRATE_DURATION);
            }
        } else {

            if (sound == 2 || sound == 3) {
                // ** 에러 시 진동 필수는 변동 없음
                // sound 2 : Error // sound 3 : Duplication

                if (vibrator != null) {
                    vibrator.vibrate(VIBRATE_DURATION);
                }
            }
        }
    }


    void destroy() {

        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private MediaPlayer buildMediaPlayer(Context activity) {        // 1, 2, 3

        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer player) {
                player.seekTo(0);
            }
        });

        AssetFileDescriptor file = null;
        if (sound == 1) {
            file = activity.getResources().openRawResourceFd(R.raw.bell);
        } else if (sound == 2) {
            file = activity.getResources().openRawResourceFd(R.raw.beep_error);
        } else if (sound == 3) {
            file = activity.getResources().openRawResourceFd(R.raw.beep_double);
        }

        try {

            mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            file.close();
            mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
            mediaPlayer.prepare();
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            mediaPlayer = null;
        }

        return mediaPlayer;
    }
}
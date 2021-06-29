/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.giosis.library.barcodescanner;


import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.util.Log;

import com.giosis.library.BuildConfig;
import com.giosis.library.R;
import com.giosis.library.util.Preferences;


public class BeepManager {

    public static int BELL_SOUNDS_SUCCESS = 1; //띵동
    public static int BELL_SOUNDS_ERROR = 2; //삐----
    public static int BELL_SOUNDS_DUPLE = 3; // 삐비~
    String TAG = "BeepManager";


    private static final float BEEP_VOLUME = 0.10f;
    private static final long VIBRATE_DURATION = 200L;

    private final Activity activity;
    private MediaPlayer mediaPlayer;
    private boolean vibrate;

    public BeepManager(Activity activity) {

        this.activity = activity;
        this.mediaPlayer = null;

        updatePrefs();
    }

    public void updatePrefs() {

        String vibrationString = Preferences.INSTANCE.getScanVibration();
        vibrate = vibrationString.equals("ON");

        if (mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
            //   mediaPlayer = buildMediaPlayer(activity);
        }
    }

    public void playBeepSoundAndVibrate(int sound) {

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

            int ninetyVolume = (int) (maxVolume * 0.9f);
            if (currentVolume < ninetyVolume) {
                audio.setStreamVolume(AudioManager.STREAM_MUSIC, ninetyVolume, 0);
            }


            try {

                if (mediaPlayer.isPlaying()) {

                    mediaPlayer.pause();
                    mediaPlayer.seekTo(0);
                }

                // TEST  테스트하기 (시끌~)
                String opID = Preferences.INSTANCE.getUserId();

                if (opID.equals("karam.kim") || BuildConfig.DEBUG) {

                    Log.e(TAG, "TEST    Sound Start  " + sound);
                } else {

                    mediaPlayer.start();
                }
            } catch (Exception e) {

                Log.e(TAG, "Beep Sound Start Exception : " + e.toString());
            }
        }


        Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        Log.e(TAG, "playBeepSoundAndVibrate  VibrationString " + vibrate + " / " + sound);

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

    public void destroy() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
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

package com.giosis.util.qdrive.barcodescanner;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;

import com.giosis.util.qdrive.singapore.R;
import com.giosis.util.qdrive.util.SharedPreferencesHelper;

import java.io.IOException;


/**
 * Manages beeps and vibrations for {@link CaptureActivity}.
 */
final class BeepManager {

    private static final String TAG = BeepManager.class.getSimpleName();

    private static final float BEEP_VOLUME = 0.10f;
    private static final long VIBRATE_DURATION = 200L;

    private final Activity activity;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private boolean vibrate;
    private int sound;

    BeepManager(Activity activity, int sound) {

        this.activity = activity;
        this.mediaPlayer = null;
        this.sound = sound;

        updatePrefs();
    }

    void updatePrefs() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        playBeep = shouldBeep(prefs, activity);
        // vibrate = prefs.getBoolean(PreferencesActivity.KEY_VIBRATE, true);

        SharedPreferences sharedPreferences = activity.getSharedPreferences("PREF_SCAN_SETTING", Activity.MODE_PRIVATE);
        String vibrationString = sharedPreferences.getString("vibration", "0");
        vibrate = vibrationString.equals("ON");

        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = buildMediaPlayer(activity);
        }
    }

    private static boolean shouldBeep(SharedPreferences prefs, Context activity) {

        boolean shouldPlayBeep = prefs.getBoolean(PreferencesActivity.KEY_PLAY_BEEP, true);
        if (shouldPlayBeep) {
            // See if sound settings overrides this
            AudioManager audioService = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
                shouldPlayBeep = false;
            }
        }

        // 20150709 진동일때, 바코드 인식 소리나게 by eylee
        shouldPlayBeep = true;
        return shouldPlayBeep;
    }

    private MediaPlayer buildMediaPlayer(Context activity) {        // 1, 2, 3

        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        // When the beep has finished playing, rewind to queue up another one.
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer player) {

                player.seekTo(0);
            }
        });

        AssetFileDescriptor file;
        if (sound == 1) {
            file = activity.getResources().openRawResourceFd(R.raw.bell);
        } else if (sound == 2) {
            file = activity.getResources().openRawResourceFd(R.raw.beep_error);
        } else if (sound == 3) {
            file = activity.getResources().openRawResourceFd(R.raw.beep_double);
        } else {
            file = activity.getResources().openRawResourceFd(R.raw.bell);
        }

        try {

            mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            file.close();
            mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
            mediaPlayer.prepare();
        } catch (IOException ioe) {

            Log.e(TAG, TAG + "  buildMediaPlayer Exception : " + ioe.toString());
            mediaPlayer = null;
        }

        return mediaPlayer;
    }


    void playBeepSoundAndVibrate() {

        if (playBeep && mediaPlayer != null) {
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
                String opID = SharedPreferencesHelper.getSigninOpID(activity);
                if (opID.equals("karam.kim") && ManualHelper.MOBILE_SERVER_URL.contains("test")) {

                    Log.e("krm0219", "Sound Start");
                } else {

                    mediaPlayer.start();
                }
            } catch (Exception e) {
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
}

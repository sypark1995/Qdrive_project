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
package com.giosis.util.qdrive.singapore.barcodescanner

import android.app.Activity
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import com.giosis.util.qdrive.singapore.BuildConfig
import com.giosis.util.qdrive.singapore.R
import com.giosis.util.qdrive.singapore.util.Preferences

class BeepManager(private val activity: Activity, private val sound: Int) {
    var TAG = "BeepManager"

    private var mediaPlayer: MediaPlayer? = null
    private var vibrate = false

    init {
        updatePrefs()
    }

    fun updatePrefs() {

        val vibrationString = Preferences.scanVibration
        vibrate = vibrationString == "ON"

        if (mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            activity.volumeControlStream = AudioManager.STREAM_MUSIC
            mediaPlayer = buildMediaPlayer()
        }
    }

    private fun buildMediaPlayer(): MediaPlayer? {

        var player: MediaPlayer? = MediaPlayer()
        //Log.e(TAG, "Sound $sound")

        var file: AssetFileDescriptor? = null
        when (sound) {
            1 -> {
                file = activity.resources.openRawResourceFd(R.raw.bell)
            }
            2 -> {
                file = activity.resources.openRawResourceFd(R.raw.beep_error)
            }
            3 -> {
                file = activity.resources.openRawResourceFd(R.raw.beep_double)
            }
        }

        try {

            player!!.setDataSource(file!!.fileDescriptor, file.startOffset, file.length)
            file.close()
            player.setVolume(BEEP_VOLUME, BEEP_VOLUME)
            player.prepare()
        } catch (e: Exception) {
            Log.e(TAG, "Beep Sound Source Exception : ${e.message}")
            e.printStackTrace()
            player = null
        }

        return player
    }


    fun playBeepSoundAndVibrate() {

        if (mediaPlayer != null) {
            // 바코드 인식 소리 높임, 90% 보다 작을 때 90%로
            // eylee 20150709 start
            val audio = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            val currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

            val ninetyVolume = (maxVolume * 0.9f).toInt()
            if (currentVolume < ninetyVolume) {
                audio.setStreamVolume(AudioManager.STREAM_MUSIC, ninetyVolume, 0)
            }

            try {
                if (mediaPlayer!!.isPlaying) {
                    mediaPlayer!!.pause()
                    mediaPlayer!!.seekTo(0)
                }

                // TEST_  테스트하기
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "TEST    Sound Start  $sound")
                    audio.setStreamVolume(AudioManager.STREAM_MUSIC, 2, 0)
                    mediaPlayer!!.start()
                } else {
                    mediaPlayer!!.start()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Beep Sound Start Exception : ${e.message}")
            }
        }

        val vibrator = activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (vibrate) {

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1)
                vibrator.vibrate(VibrationEffect.createOneShot(VIBRATE_DURATION, VibrationEffect.DEFAULT_AMPLITUDE))
            else
                vibrator.vibrate(VIBRATE_DURATION)
        } else {
            if (sound == 2 || sound == 3) {
                // ** 에러 시 진동 필수는 변동 없음
                // sound 2 : Error // sound 3 : Duplication
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1)
                    vibrator.vibrate(VibrationEffect.createOneShot(VIBRATE_DURATION, VibrationEffect.DEFAULT_AMPLITUDE))
                else
                    vibrator.vibrate(VIBRATE_DURATION)
            }
        }
    }

    fun destroy() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer!!.stop()
                mediaPlayer!!.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {

        @JvmField
        var BELL_SOUNDS_SUCCESS = 1 //띵동

        @JvmField
        var BELL_SOUNDS_ERROR = 2 //삐----

        @JvmField
        var BELL_SOUNDS_DUPLE = 3 // 삐비~

        private const val BEEP_VOLUME = 0.10f
        private const val VIBRATE_DURATION = 200L
    }
}
package com.speakeasy.watsonbarassistant.SpeechandText

import android.media.MediaPlayer
import android.media.MediaRecorder
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.speakeasy.watsonbarassistant.mFileName
import com.speakeasy.watsonbarassistant.permissionToRecordAccepted
import java.io.IOException

private const val LOG_TAG = "AudioHandler"

class AudioHandler : AppCompatActivity() {

    private var mRecorder: MediaRecorder? = null
    private var mPlayer: MediaPlayer? = null

    fun startPlaying() {

        mPlayer = MediaPlayer().apply {
            try {
                setDataSource(mFileName)
                prepare()
                start()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }
        }
    }

    fun stopPlaying() {
        mPlayer?.release()
        mPlayer = null
    }

    fun startRecording() {
        Log.d(LOG_TAG, "Start Recording")

        if (permissionToRecordAccepted) {
            Log.i(LOG_TAG, "Start Recording: Permission Accepted")
            mRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setOutputFile(mFileName)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

                Log.i(LOG_TAG, "fileName: " + mFileName)
                try {
                    prepare()
                } catch (e: IOException) {
                    Log.e(LOG_TAG, "prepare() failed: " + e.message)
                }

                start()
            }
        } else {
            Log.i(LOG_TAG, "Start Recording: Permission Denied")
            //TODO Toast.makeText(context, "Released", Toast.LENGTH_SHORT).show()
        }
    }

    fun stopRecording() {
        mRecorder?.apply {
            stop()
            release()
        }
        mRecorder = null
    }

    override fun onStop() {
        super.onStop()
        mRecorder?.release()
        mRecorder = null
        mPlayer?.release()
        mPlayer = null
    }
}
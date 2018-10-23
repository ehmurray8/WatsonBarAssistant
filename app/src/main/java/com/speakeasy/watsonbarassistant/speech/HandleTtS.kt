package com.speakeasy.watsonbarassistant.speech

import android.media.MediaPlayer
import android.util.Log
import com.speakeasy.watsonbarassistant.ERROR_FILE_NAME
import java.io.IOException

class HandleTtS: CompletedTtS {

    override fun onTaskCompleted(outFile:  String, mediaPlayer: MediaPlayer) {

        if (outFile == ERROR_FILE_NAME){
            Log.i("HandlerTtS", "Invalid file returned.")
        } else {
            mediaPlayer.apply {
                try {
                    setDataSource(outFile)
                    prepare()
                    start()
                } catch (e: IOException) {
                    Log.e("Text to Speech", "prepare() failed")
                }
            }
        }
    }
}
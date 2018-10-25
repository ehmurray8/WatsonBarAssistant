package com.speakeasy.watsonbarassistant.speech

import android.media.MediaPlayer


interface CompletedTtS {
    fun onTaskCompleted(outFile: String, mediaPlayer: MediaPlayer)
}
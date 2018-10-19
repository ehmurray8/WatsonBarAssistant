package com.speakeasy.watsonbarassistant.SpeechandText

import android.util.Log
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionResult
import com.speakeasy.watsonbarassistant.ERROR_FILE_NAME
import com.speakeasy.watsonbarassistant.IngredientsTab

class HandleTtS: CompletedTtS {

    override fun onTaskCompleted(fileName:  String) {

        if (fileName == ERROR_FILE_NAME){
            //TODO display failure :(
            Log.i("HandlerTtS", "Invalid file returned.")
        } else {
            val audioHandler = AudioHandler()
            Log.i("HandlerTtS", "Valid, playing file.")
            audioHandler.startPlaying(fileName)
        }
    }
}
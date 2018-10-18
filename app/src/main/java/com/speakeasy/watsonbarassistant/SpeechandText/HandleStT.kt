package com.speakeasy.watsonbarassistant.SpeechandText

import android.util.Log
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionResult
import com.speakeasy.watsonbarassistant.IngredientsTab

class HandleStT: CompletedStT {

    override fun onTaskCompleted(result: MutableList<SpeechRecognitionResult>) {
        Log.i("Answer: ", result.toString())

        for (value in result){
            IngredientsTab.words.add(value.toString())
        }
    }
}
package com.speakeasy.watsonbarassistant.SpeechandText

import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionResult

interface CompletedStT {
    fun onTaskCompleted(result: MutableList<SpeechRecognitionResult>)
}
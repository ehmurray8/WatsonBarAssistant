package com.speakeasy.watsonbarassistant

import android.util.Log
import android.widget.Toast
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionResults
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.RecognizeCallback


class MicrophoneRecognizeDelegate(private val ingredientsTab: IngredientsTab): RecognizeCallback {
    override fun onConnected() {
        Log.d("Speech To Text", "Connected")
    }

    override fun onListening() {
        Log.d("Speech To Text", "Listening")
    }


    override fun onTranscription(speechResults: SpeechRecognitionResults?) {
        Log.d("Speech To Text", "Transcription")
        if(speechResults?.results != null && !speechResults.results.isEmpty()) {
            val text = speechResults.results[0].alternatives[0].transcript
            showMicText(text)
            Log.d("Speech To Text", "Text: $text")
        }
    }

    override fun onInactivityTimeout(runtimeException: RuntimeException?) {
        Log.d("Speech To Text", "Inactivity timeout")
    }

    override fun onTranscriptionComplete() {
        Log.d("Speech To Text", "Transcription complete")
    }

    override fun onDisconnected() {
        Log.d("Speech To Text", "Disconnected")
        ingredientsTab.setStopListening()
    }

    override fun onError(e: Exception?) {
        Log.d("Speech To Text", "Error")
        e?.printStackTrace()
        Log.d("Speech To Text", "The error was:\n${e?.toString()}")
        ingredientsTab.setStopListening()
    }

    private fun showMicText(text: String) {
        Toast.makeText(ingredientsTab.activity?.baseContext, text, Toast.LENGTH_SHORT).show()
    }
}
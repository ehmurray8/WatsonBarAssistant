package com.speakeasy.watsonbarassistant.SpeechandText

import android.os.AsyncTask
import com.speakeasy.watsonbarassistant.*
import com.ibm.watson.developer_cloud.service.security.IamOptions
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionResult
import java.io.File
import java.lang.Exception


class SpeechToText(private val inputListener: CompletedStT):
        AsyncTask<String, Void, MutableList<SpeechRecognitionResult>>() {
    val audioHandler = AudioHandler()

    override fun doInBackground(vararg args: String): MutableList<SpeechRecognitionResult> {
        if(BarAssistant.isInternetConnected()) {
            val file = args[0]
            val options = IamOptions.Builder()
                    .apiKey(StT_API_KEY)
                    .build()

            val speechToText = SpeechToText(options)

            speechToText.setEndPoint(StT_URL)

            try {
                //val files = Arrays.asList("audio-file1.flac", "audio-file2.flac")

                val recognizeOptions = RecognizeOptions.Builder()
                        .audio(File(file))
                        .contentType("audio/mpeg")
                        .timestamps(true)
                        .wordAlternativesThreshold(0.9.toFloat())
                        .build()

                val speechRecognitionResults = speechToText.recognize(recognizeOptions).execute()
                println(speechRecognitionResults)

                return speechRecognitionResults.results
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return mutableListOf()
    }

    override fun onPostExecute(result: MutableList<SpeechRecognitionResult>){
        super.onPostExecute(result)
        inputListener.onTaskCompleted(result)
    }

    fun startRecording(){
        audioHandler.startRecording()
    }

    fun stopRecording(){
        audioHandler.stopRecording()
    }

    fun startPlaying(){
        audioHandler.startPlaying()
    }

    fun stopPlaying(){
        audioHandler.stopPlaying()
    }
}
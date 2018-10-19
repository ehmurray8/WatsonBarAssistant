package com.speakeasy.watsonbarassistant.SpeechandText

import android.os.AsyncTask
import android.util.Log
import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer
import com.ibm.watson.developer_cloud.service.security.IamOptions
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionResult
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech
import java.io.File
import java.lang.Exception
import com.ibm.watson.developer_cloud.text_to_speech.v1.util.WaveUtils
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.SynthesizeOptions
import com.speakeasy.watsonbarassistant.*
import kotlinx.io.IOException
import java.io.FileOutputStream


class TextToSpeech(private val inputListener: CompletedTtS):
        AsyncTask<String, Void, String>() {

    override fun doInBackground(vararg args: String): String {
        var outFile = ERROR_FILE_NAME

        if(BarAssistant.isInternetConnected()) {
            val input = args[0]
            Log.i("TextToSpeech", input)
            val textToSpeech = TextToSpeech()

            textToSpeech.setUsernameAndPassword(TtS_USERNAME, TtS_PASSWORD)

            try {
                outFile = OUTPUT_FILE_NAME
                val synthesizeOptions = SynthesizeOptions.Builder()
                        .text(input)
                        .accept("audio/wav")
                        .voice("en-US_AllisonVoice")
                        .build()


                val streamPlayer = StreamPlayer()
                streamPlayer.playStream(textToSpeech.synthesize(synthesizeOptions).execute())
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return outFile
    }

    override fun onPostExecute(result: String){
        super.onPostExecute(result)
        inputListener.onTaskCompleted(result)
    }
}
package com.speakeasy.watsonbarassistant.speech

import android.media.MediaPlayer
import android.os.AsyncTask
import android.util.Log
import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.SynthesizeOptions
import com.speakeasy.watsonbarassistant.*
import com.ibm.watson.developer_cloud.service.security.IamOptions




class TextToSpeech(private val inputListener: CompletedTtS, private val mediaPlayer: MediaPlayer):
        AsyncTask<String, Void, String>() {

    override fun doInBackground(vararg args: String): String {
        var outFile = ERROR_FILE_NAME

        if(BarAssistant.isInternetConnected()) {
            val input = args[0]

            val options = IamOptions.Builder()
                    .apiKey(TtS_API_KEY)
                    .build()

            val textToSpeech = TextToSpeech(options)

            textToSpeech.endPoint = TtS_URL

            try {
                outFile = OUTPUT_FILE_NAME
                val synthesizeOptions = SynthesizeOptions.Builder()
                        .text(input)
                        .accept("audio/wav")
                        .voice("en-GB_KateVoice")
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
        inputListener.onTaskCompleted(result, mediaPlayer)
    }
}
package com.speakeasy.watsonbarassistant.SpeechAndText

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.os.Message
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast

import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneHelper
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream
import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer
import com.ibm.watson.developer_cloud.android.library.audio.utils.ContentType
import com.ibm.watson.developer_cloud.conversation.v1.Conversation
import com.ibm.watson.developer_cloud.conversation.v1.model.InputData
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageOptions
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionResults
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.RecognizeCallback
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.Voice
import com.speakeasy.watsonbarassistant.R


import java.util.ArrayList
import java.util.HashMap

/*
class MainActivity : AppCompatActivity() {


    private var recyclerView: RecyclerView? = null
    private var mAdapter: ChatAdapter? = null
    private var messageArrayList: ArrayList<*>? = null
    private var inputMessage: EditText? = null
    private var btnSend: ImageButton? = null
    private var btnRecord: ImageButton? = null
    //private Map<String,Object> context = new HashMap<>();
    internal var context: com.ibm.watson.developer_cloud.conversation.v1.model.Context? = null
    internal var streamPlayer: StreamPlayer? = null
    private var initialRequest: Boolean = false
    private var permissionToRecordAccepted = false
    private var listening = false
    private var speechService: SpeechToText? = null
    private var capture: MicrophoneInputStream? = null
    //private val recoTokens: SpeakerLabelsDiarization.RecoTokens? = null
    private var microphoneHelper: MicrophoneHelper? = null

    //Private Methods - Speech to Text
    private//.model("en-UK_NarrowbandModel")
    //TODO: Uncomment this to enable Speaker Diarization
    //.speakerLabels(true)
    val recognizeOptions: RecognizeOptions
        get() = RecognizeOptions.Builder()
                .contentType(ContentType.OPUS.toString())
                .interimResults(true)
                .inactivityTimeout(2000)
                .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputMessage = findViewById(R.id.message) as EditText
        btnSend = findViewById(R.id.btn_send) as ImageButton
        btnRecord = findViewById(R.id.btn_record) as ImageButton
        val customFont = "Montserrat-Regular.ttf"
        val typeface = Typeface.createFromAsset(assets, customFont)
        inputMessage!!.typeface = typeface
        recyclerView = findViewById(R.id.recycler_view) as RecyclerView

        messageArrayList = ArrayList()
        mAdapter = ChatAdapter(messageArrayList)
        microphoneHelper = MicrophoneHelper(this)


        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        recyclerView!!.layoutManager = layoutManager
        recyclerView!!.itemAnimator = DefaultItemAnimator()
        recyclerView!!.adapter = mAdapter
        this.inputMessage!!.setText("")
        this.initialRequest = true
        sendMessage()

        //Watson Text-to-Speech Service on IBM Cloud
        val service = TextToSpeech()
        service.setUsernameAndPassword("Text to Speech service username", "Text to Speech service password")

        val permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to record denied")
            makeRequest()
        }


        recyclerView!!.addOnItemTouchListener(RecyclerTouchListener(applicationContext, recyclerView, object : ClickListener() {
            fun onClick(view: View, position: Int) {
                val thread = Thread(Runnable {
                    val audioMessage: Message?
                    try {

                        audioMessage = messageArrayList!![position] as Message
                        streamPlayer = StreamPlayer()
                        if (audioMessage != null && !audioMessage!!.getMessage().isEmpty())
                        //Change the Voice format and choose from the available choices
                            streamPlayer.playStream(service.synthesize(audioMessage!!.getMessage(), Voice.EN_ALLISON).execute())
                        else
                            streamPlayer.playStream(service.synthesize("No Text Specified", Voice.EN_LISA).execute())

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                })
                thread.start()
            }

            fun onLongClick(view: View, position: Int) {
                recordMessage()

            }
        }))

        btnSend!!.setOnClickListener {
            if (checkInternetConnection()) {
                sendMessage()
            }
        }

        btnRecord!!.setOnClickListener { recordMessage() }
    }

    // Speech to Text Record Audio permission
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_RECORD_AUDIO_PERMISSION -> permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
            RECORD_REQUEST_CODE -> {

                if (grantResults.size == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    Log.i(TAG, "Permission has been denied by user")
                } else {
                    Log.i(TAG, "Permission has been granted by user")
                }
                return
            }
            MicrophoneHelper.REQUEST_PERMISSION -> {
                if (grantResults.size > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission to record audio denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
        // if (!permissionToRecordAccepted ) finish();

    }

    protected fun makeRequest() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                MicrophoneHelper.REQUEST_PERMISSION)
    }


    // Sending a message to Watson Conversation Service
    /*
    private fun sendMessage() {

        val inputmessage = this.inputMessage!!.text.toString().trim { it <= ' ' }
        if (!this.initialRequest) {
            val inputMessage = Message()
            inputMessage.setMessage(inputmessage)
            inputMessage.setId("1")
            messageArrayList!!.add(inputMessage)
        } else {
            val inputMessage = Message()
            inputMessage.setMessage(inputmessage)
            inputMessage.setId("100")
            this.initialRequest = false
            Toast.makeText(applicationContext, "Tap on the message for Voice", Toast.LENGTH_LONG).show()

        }

        this.inputMessage!!.setText("")
        mAdapter!!.notifyDataSetChanged()

        val thread = Thread(Runnable {
            try {

                val service = Conversation("VERSION_DATE_2017_05_26")
                service.setUsernameAndPassword("Watson Assistant username", "Watson Assistant password")

                val input = InputData.Builder(inputmessage).build()
                val options = MessageOptions.Builder("Workspace ID").input(input).context(context).build()
                val response = service.message(options).execute()

                //Passing Context of last conversation
                if (response!!.context != null) {
                    //context.clear();
                    context = response.context

                }
                val outMessage = Message()
                if (response != null) {
                    if (response.output != null && response.output.containsKey("text")) {
                        val responseList = response.output["text"] as ArrayList<*>
                        if (null != responseList && responseList.size > 0) {
                            outMessage.setMessage(responseList[0] as String)
                            outMessage.setId("2")
                        }
                        messageArrayList!!.add(outMessage)
                    }

                    runOnUiThread {
                        mAdapter!!.notifyDataSetChanged()
                        if (mAdapter!!.getItemCount() > 1) {
                            recyclerView!!.layoutManager.smoothScrollToPosition(recyclerView, null, mAdapter!!.getItemCount() - 1)

                        }
                    }


                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

        thread.start()

    }
    */

    //Record a message via Watson Speech to Text
    private fun recordMessage() {
        //mic.setEnabled(false);
        speechService = SpeechToText()
        speechService!!.setUsernameAndPassword("Speech to Text username", "Speech to Text password")

        if (listening != true) {
            capture = microphoneHelper!!.getInputStream(true)
            Thread(Runnable {
                try {
                    speechService!!.recognizeUsingWebSocket(recognizeOptions, MicrophoneRecognizeDelegate())
                } catch (e: Exception) {
                    showError(e)
                }
            }).start()
            listening = true
            Toast.makeText(this@MainActivity, "Listening....Click to Stop", Toast.LENGTH_LONG).show()

        } else {
            try {
                microphoneHelper!!.closeInputStream()
                listening = false
                Toast.makeText(this@MainActivity, "Stopped Listening....Click to Start", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    /**
     * Check Internet Connection
     * @return
     */
    private fun checkInternetConnection(): Boolean {
        // get Connectivity Manager object to check connection
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork = cm.activeNetworkInfo
        val isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting

        // Check for network connections
        if (isConnected) {
            return true
        } else {
            Toast.makeText(this, " No Internet Connection available ", Toast.LENGTH_LONG).show()
            return false
        }

    }

    private inner class MicrophoneRecognizeDelegate : BaseRecognizeCallback() {

        override fun onTranscription(speechResults: SpeechRecognitionResults) {
            System.out.println(speechResults)
            //TODO: Uncomment this to enable Speaker Diarization
            /*recoTokens = new SpeakerLabelsDiarization.RecoTokens();
            if(speechResults.getSpeakerLabels() !=null)
            {
                recoTokens.add(speechResults);
                Log.i("SPEECHRESULTS",speechResults.getSpeakerLabels().get(0).toString());
            }*/
            if (speechResults.getResults() != null && !speechResults.getResults().isEmpty()) {
                val text = speechResults.getResults().get(0).getAlternatives().get(0).getTranscript()
                showMicText(text)
            }
        }

        override fun onConnected() {

        }

        override fun onError(e: Exception) {
            showError(e)
            enableMicButton()
        }

        override fun onDisconnected() {
            enableMicButton()
        }

        override fun onInactivityTimeout(runtimeException: RuntimeException?) {

        }

        override fun onListening() {

        }

        override fun onTranscriptionComplete() {

        }
    }

    private fun showMicText(text: String) {
        runOnUiThread { inputMessage!!.setText(text) }
    }

    private fun enableMicButton() {
        runOnUiThread { btnRecord!!.isEnabled = true }
    }

    private fun showError(e: Exception) {
        runOnUiThread {
            Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    companion object {
        private val REQUEST_RECORD_AUDIO_PERMISSION = 200
        private val TAG = "MainActivity"
        private val RECORD_REQUEST_CODE = 101
    }

}*/
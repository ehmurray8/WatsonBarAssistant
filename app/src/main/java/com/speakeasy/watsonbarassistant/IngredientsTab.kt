package com.speakeasy.watsonbarassistant


import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneHelper
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream
import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer
import com.ibm.watson.developer_cloud.android.library.audio.utils.ContentType
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionResults
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback
import kotlinx.android.synthetic.main.fragment_ingredient_tab.*
import java.util.ArrayList


class IngredientsTab : Fragment() {



    var listening = false

    private val recyclerView: RecyclerView? = null
    //private val mAdapter: ChatAdapter? = null
    private val messageArrayList: ArrayList<String>? = null
    var inputMessage: EditText? = null
    private val btnSend: ImageButton? = null
    private val btnRecord: ImageButton? = null
    //private Map<String,Object> context = new HashMap<>();
    //var context: com.ibm.watson.developer_cloud.conversation.v1.model.Context? = null
    var streamPlayer: StreamPlayer? = null
    private val initialRequest: Boolean = false
    private val speechService: SpeechToText = SpeechToText()
    private var capture: MicrophoneInputStream = MicrophoneInputStream(false)
    //private val recoTokens: SpeakerLabelsDiarization.RecoTokens? = null
    private val microphoneHelper: MicrophoneHelper? = null

    private val fireStore = FirebaseFirestore.getInstance()

    private lateinit var menuAnimOpen: Animation
    private lateinit var menuAnimClose: Animation
    private lateinit var menuAnimRotateOut: Animation
    private lateinit var menuAnimRotateBack: Animation

    private var isAddMenuOpen: Boolean = false
    private var viewAdapter: IngredientsAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
       super.onCreateView(inflater, container, savedInstanceState)
       return inflater.inflate(R.layout.fragment_ingredient_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewManager = LinearLayoutManager(activity)
        val mainMenu = activity as MainMenu
        viewAdapter = IngredientsAdapter(mainMenu.ingredients, mainMenu.documentsMap)

        ingredients_recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        menuAnimOpen = AnimationUtils.loadAnimation(context, R.anim.menu_anim_open)
        menuAnimClose = AnimationUtils.loadAnimation(context, R.anim.menu_anim_close)
        menuAnimRotateOut = AnimationUtils.loadAnimation(context, R.anim.menu_anim_rotate_out)
        menuAnimRotateBack = AnimationUtils.loadAnimation(context, R.anim.menu_anim_rotate_back)

        addMenuButton.setOnClickListener {
            if (isAddMenuOpen) {
                closeMenus()
            } else {
                openMenus()
            }

        }
        addViaTextButton.setOnClickListener {
            closeMenus()
            ingredientInputView.visibility = View.VISIBLE
            ingredientInputView.setOnEditorActionListener { _, actionId, _ ->
                addMenuButton.hide()
                return@setOnEditorActionListener when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        val name = ingredientInputView.text.toString()
                        val ingredient = Ingredient(name)
                        addIngredient(ingredient)
                        ingredientInputView.selectAll()
                        ingredientInputView.setText("")
                        addMenuButton.show()
                        true
                    }
                    else -> false
                }
            }
        }
        addViaCameraButton.setOnClickListener { Toast.makeText(context, "Camera support to be added!", Toast.LENGTH_SHORT).show() }
        addViaVoiceButton.setOnClickListener {
            Toast.makeText(context, "Voice support to be added!", Toast.LENGTH_SHORT).show()

            //recordMessage()
        }


        val itemDecorator = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        ingredients_recycler_view.addItemDecoration(itemDecorator)

        setupSwipeHandler()
    }

    private fun addIngredient(ingredient: Ingredient) {
        val ingredients = (activity as MainMenu).ingredients
        if(ingredients.any { it.name.toLowerCase() == ingredient.name.toLowerCase() }) {
            Toast.makeText(activity, "${ingredient.name} is already stored as an ingredient.", Toast.LENGTH_SHORT).show()
        } else {
            addIngredientToFireStore(ingredient)
        }
    }

     private fun setupSwipeHandler() {
         val context = activity?.baseContext ?: return
         val swipeHandler = object : SwipeToDeleteCallback(context) {
             override fun onMove(p0: RecyclerView, p1: RecyclerView.ViewHolder, p2: RecyclerView.ViewHolder): Boolean {
                 super.onMove(p0, p1, p2)
                 p0.scrollTo(1, 1)
                 return true
             }

             override fun onSwiped(p0: RecyclerView.ViewHolder, direction: Int) {
                 val position = p0.layoutPosition
                 viewAdapter?.removeAt(position)
             }
         }
         val itemTouchHelper = ItemTouchHelper(swipeHandler)

         itemTouchHelper.attachToRecyclerView(ingredients_recycler_view)
     }

    private fun addIngredientToFireStore(ingredient: Ingredient) {
        val mainMenu = (activity as? MainMenu)
        val uid = mainMenu?.currentUser?.uid ?: return
        fireStore.collection("app").document(uid)
                .collection("ingredients").add(ingredient).addOnSuccessListener { _ ->
                    Toast.makeText(context, "Successfully added ${ingredient.name}.", Toast.LENGTH_SHORT).show()
                    mainMenu.ingredients.add(ingredient)
                    mainMenu.ingredients.sortBy { it.name.toLowerCase().replace("\\s".toRegex(), "") }
                    refresh()
                }.addOnFailureListener {
                    Toast.makeText(context, "Failed to add ${ingredient.name}.", Toast.LENGTH_SHORT).show()
                }
    }

    private fun refresh() {
        viewAdapter?.notifyDataSetChanged()
    }

    private fun closeMenus(){
        addMenuButton.startAnimation(menuAnimRotateBack)
        addViaTextButton.startAnimation(menuAnimClose)
        addViaCameraButton.startAnimation(menuAnimClose)
        addViaVoiceButton.startAnimation(menuAnimClose)
        addViaTextButton.isClickable = false
        addViaTextButton.hide()
        addViaCameraButton.isClickable = false
        addViaCameraButton.hide()
        addViaVoiceButton.isClickable = false
        addViaVoiceButton.hide()
        ingredientInputView.visibility = View.GONE
        isAddMenuOpen = false
    }

    private fun openMenus(){
        addMenuButton.startAnimation(menuAnimRotateOut)
        addViaTextButton.startAnimation(menuAnimOpen)
        addViaCameraButton.startAnimation(menuAnimOpen)
        addViaVoiceButton.startAnimation(menuAnimOpen)
        addViaTextButton.isClickable = true
        addViaTextButton.show()
        addViaCameraButton.isClickable = true
        addViaCameraButton.show()
        addViaVoiceButton.isClickable = true
        addViaVoiceButton.show()
        isAddMenuOpen = true
    }


    //Record a message via Watson Speech to Text
    private fun recordMessage() {
        //mic.setEnabled(false);
        Toast.makeText(context, "In button", Toast.LENGTH_SHORT).show()
        Log.i("Speech to Text", "Button Test")
        speechService.setUsernameAndPassword(StT_USERNAME, StT_PASSWORD)
        if (listening !== true) {
            capture = MicrophoneInputStream(true)
            Thread(Runnable {
                try {
                    speechService.recognizeUsingWebSocket(getRecognizeOptions(),  MicrophoneRecognizeDelegate())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }).start()
            listening = true
            Toast.makeText(context, "Listening....Click to Stop", Toast.LENGTH_LONG).show()
        } else {
            try {
                capture.close()
                listening = false
                Toast.makeText(context, "Stopped Listening....Click to Start", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }



    //Private Methods - Speech to Text
    private fun getRecognizeOptions(): RecognizeOptions {
        return  RecognizeOptions.Builder()
                .contentType(ContentType.OPUS.toString())
                //.model("en-UK_NarrowbandModel")
                .interimResults(true)
                .inactivityTimeout(2000)
                //TODO: Uncomment this to enable Speaker Diarization
                //.speakerLabels(true)
                .build()
    }


    private class MicrophoneRecognizeDelegate : BaseRecognizeCallback() {

        override fun onTranscription(speechResults: SpeechRecognitionResults) {
            System.out.println(speechResults)
            //TODO: Uncomment this to enable Speaker Diarization
            /*recoTokens = new SpeakerLabelsDiarization.RecoTokens()
            if(speechResults.getSpeakerLabels() !=null)
            {
                recoTokens.add(speechResults)
                Log.i("SPEECHRESULTS",speechResults.getSpeakerLabels().get(0).toString())
            }*/

            if(speechResults.getResults() != null && !speechResults.getResults().isEmpty()) {
                var text = speechResults.getResults().get(0).getAlternatives().get(0).getTranscript()
                //inputMessage.setText(text)
            }
        }

        override fun onConnected() {

        }

        override fun onError(e: Exception) {
            //showError(e)
            //btnRecord.setEnabled(true)
        }

        override fun onDisconnected() {
            //btnRecord.setEnabled(true)
        }

        override fun onInactivityTimeout(runtimeException: RuntimeException ) {

        }

        override fun onListening() {

        }

        override fun onTranscriptionComplete() {

        }
    }


}

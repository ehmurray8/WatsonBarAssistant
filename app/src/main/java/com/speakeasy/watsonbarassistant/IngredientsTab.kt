package com.speakeasy.watsonbarassistant


import android.Manifest.permission.RECORD_AUDIO
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
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
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneHelper
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream
import com.ibm.watson.developer_cloud.android.library.audio.utils.ContentType
import com.ibm.watson.developer_cloud.http.HttpMediaType
import com.ibm.watson.developer_cloud.service.security.IamOptions
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions
import kotlinx.android.synthetic.main.fragment_ingredient_tab.*

class IngredientsTab : Fragment() {

    private val fireStore = FirebaseFirestore.getInstance()

    private lateinit var menuAnimOpen: Animation
    private lateinit var menuAnimClose: Animation
    private lateinit var menuAnimRotateOut: Animation
    private lateinit var menuAnimRotateBack: Animation

    private var isAddMenuOpen: Boolean = false
    private var viewAdapter: IngredientsAdapter? = null
    private var speechService: SpeechToText

    private var listening = false
    private var capture: MicrophoneInputStream? = null
    private var microphoneHelper: MicrophoneHelper? = null

    init {
        val options = IamOptions.Builder().apiKey(StT_API_KEY).build()
        speechService = SpeechToText(options)
        speechService.endPoint = StT_ENDPOINT
    }

    fun setStopListening() {
        listening = false
    }

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
            when {
                isAddMenuOpen -> closeMenus()
                else -> openMenus()
            }
        }
        addViaTextButton.setOnClickListener {
            closeMenus()
            ingredientInputView.visibility = View.VISIBLE
            ingredientInputView.setOnEditorActionListener { _, actionId, _ ->

                return@setOnEditorActionListener when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        val name = ingredientInputView.text.toString()
                        val ingredient = Ingredient(name)
                        addIngredient(ingredient)
                        ingredientInputView.selectAll()
                        ingredientInputView.setText("")
                        true
                    }
                    else -> false
                }
            }
        }
        
        ingredientInputView.setOnTouchListener { _, event ->
            if(ingredientInputView.hasFocus()) {
                addMenuButton.hide()
            } else {
                addMenuButton.show()
            }
            ingredientInputView.onTouchEvent(event)
        }

        addViaCameraButton.setOnClickListener { Toast.makeText(context, "Camera support to be added!", Toast.LENGTH_SHORT).show() }
        addViaVoiceButton.setOnClickListener {
            val permission = activity?.baseContext?.let { it1 -> ContextCompat.checkSelfPermission(it1, RECORD_AUDIO) }
            if(permission != PackageManager.PERMISSION_GRANTED) {
                makeRequestForAudioPermission()
            } else {
                recordMessage()
            }
        }
        val itemDecorator = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        ingredients_recycler_view.addItemDecoration(itemDecorator)

        setupSwipeHandler()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            RECORD_REQUEST_CODE -> {
                if(grantResults.count() == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.d("Speech To Text", "Permission denied")
                } else {
                    Log.d("Speech To Text", "Permission granted")
                    recordMessage()
                }
            }
        }
    }

    private fun makeRequestForAudioPermission() {
        activity?.let { ActivityCompat.requestPermissions(it, arrayOf(RECORD_AUDIO), RECORD_REQUEST_CODE) }
    }

    private fun recordMessage() {
        Log.d("Speech To Text", "Record message")
        if(!listening) {
            microphoneHelper = MicrophoneHelper(activity)
            capture  = microphoneHelper?.getInputStream(true)
            Thread(Runnable {
                try {
                    speechService.recognizeUsingWebSocket(getRecognizeOptions(), MicrophoneRecognizeDelegate(this))
                } catch (e: Exception) {
                    Log.d("Speech To Text", "Failed to launch")
                    e.printStackTrace()
                }
            }).start()
            listening = true
            Toast.makeText(activity?.baseContext,"Listening....Click to Stop", Toast.LENGTH_LONG).show()
        } else {
            try {
                microphoneHelper?.closeInputStream()
                listening = false
                Toast.makeText(activity?.baseContext,"Stopped Listening....Click to Start", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getRecognizeOptions(): RecognizeOptions {
        return RecognizeOptions.Builder()
          .interimResults(true)
          .contentType(ContentType.OPUS.toString())
          // .inactivityTimeout(5)
          .audio(capture)
          .contentType(HttpMediaType.AUDIO_RAW + "; rate=" + 16000)
          .build()
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

             override fun onSwiped(p0: RecyclerView.ViewHolder, direction: Int) {
                 val position = p0.adapterPosition
                 viewAdapter?.removeAt(position)
             }
         }
         val itemTouchHelper = ItemTouchHelper(swipeHandler)
         itemTouchHelper.attachToRecyclerView(ingredients_recycler_view)
     }

    private fun addIngredientToFireStore(ingredient: Ingredient) {
        val mainMenu = (activity as? MainMenu)
        val uid = mainMenu?.currentUser?.uid ?: return
        fireStore.collection(MAIN_COLLECTION).document(uid)
                .collection(INGREDIENT_COLLECTION).add(ingredient).addOnSuccessListener { _ ->
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
}

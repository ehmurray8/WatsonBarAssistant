
package com.speakeasy.watsonbarassistant.activity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ShareCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.support.v7.graphics.Palette
import android.support.v7.widget.DividerItemDecoration
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.speakeasy.watsonbarassistant.*
import com.speakeasy.watsonbarassistant.adapter.IngredientAdapter
import com.speakeasy.watsonbarassistant.extensions.toast
import com.speakeasy.watsonbarassistant.speech.HandleTtS
import com.speakeasy.watsonbarassistant.speech.TextToSpeech
import kotlinx.android.synthetic.main.activity_recipe_detail.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import kotlin.concurrent.thread

class RecipeDetail : AppCompatActivity() {

    private var authorization = FirebaseAuth.getInstance()
    private var fireStore = FirebaseFirestore.getInstance()
    private var mediaPlayer: MediaPlayer? = null
    private var favorited = false
    private var appBarExpanded = true
    private var drinkBitmap: Bitmap? = null
    private var recipeTitle: String? = null
    private var lastReadTime = -1L
    private var collapsedMenu: Menu? = null

    private var favoriteIds = listOf<String>()
    get() {
        return synchronized(BarAssistant.favoritesList) {BarAssistant.favoritesList.map { it.imageId }}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)
        val recipe = intent.getSerializableExtra("Recipe") as? DiscoveryRecipe
        setSupportActionBar(toolbarRecipeDetail)

        button_favorite.alpha = 0.35.toFloat()

        if(recipe != null) {
            loadImage(baseContext, drink_detail_image, recipe)

            recipeTitle = recipe.title

            description_content.text = recipe.description

            val viewAdapter = IngredientAdapter(recipe.ingredientList.map { Ingredient(it) },
                    recipe.normalIngredients, applicationContext)
            val viewManager = AutoLinearLayoutManager(this)

            detailIngredients.isNestedScrollingEnabled = false
            detailIngredients.apply {
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = viewAdapter
            }
            val itemDecorator = DividerItemDecoration(applicationContext, DividerItemDecoration.VERTICAL)
            detailIngredients.addItemDecoration(itemDecorator)

            addTags(recipe)

            val favoriteAnim = AnimationUtils.loadAnimation(baseContext, R.anim.anim_favorite)

            if(favoriteIds.contains(recipe.imageId)) {
                favorited = true
                button_favorite.startAnimation(favoriteAnim)
                button_favorite.isChecked = true
            }

            addReadListener(recipe)
            parallaxSetup(recipe)

            button_favorite.setOnClickListener {
                if (favorited) {
                    favorited = false
                    synchronized(BarAssistant.favoritesList) {
                        BarAssistant.favoritesList.removeIf { favorite ->
                            favorite.imageId == recipe.imageId
                        }
                    }
                } else {
                    favorited = true
                    button_favorite.startAnimation(favoriteAnim)
                    if (!favoriteIds.contains(recipe.imageId)) {
                        synchronized(BarAssistant.favoritesList) {
                            BarAssistant.favoritesList.add(recipe)
                        }
                    }
                }
            }
            thread {
                addToRecentlyViewed(recipe)
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.detail_menu, menu)
        collapsedMenu = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.shareMenuButton -> {
                share()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun share() {
        val imagePath = File(filesDir, "images")
        imagePath.mkdir()
        val imageFile = File(imagePath.path, "share.jpg")

        if(drinkBitmap == null) {
            toast("Loading share image, please try again.")
            return
        }
        try {
            imageFile.createNewFile()
            val outputStream = FileOutputStream(imageFile)
            drinkBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val outputUri = FileProvider.getUriForFile(this, "com.speakeasy.watsonbarassistant.provider", imageFile)
        val intent = ShareCompat.IntentBuilder.from(this)
                .setStream(outputUri).setType("image/*").intent.setAction(Intent.ACTION_SEND).setDataAndType(outputUri, "image/*").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.putExtra(Intent.EXTRA_TEXT, "Check out, $recipeTitle, I found it on BarAssistant. Download it here $DOWNLOAD_LINK")
        startActivity(Intent.createChooser(intent, "Share Image"))
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (appBarExpanded) {
            collapsedMenu?.getItem(0)?.setIcon(R.drawable.ic_share_white_24dp)
        } else {
            collapsedMenu?.getItem(0)?.setIcon(R.drawable.ic_share_black_24dp)
            collapsedMenu?.add("Read Description")?.setIcon(R.drawable.ic_mic_black_24dp)
                    ?.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        }
        return super.onPrepareOptionsMenu(collapsedMenu)
    }

    private fun parallaxSetup(recipe: DiscoveryRecipe) {
        thread {
            try {
                val url = URL(Uri.parse(recipe.recipeImageUriString).toString())
                val connection = url.openConnection() as? HttpURLConnection
                connection?.doInput = true
                connection?.connect()
                val inputStream = connection?.inputStream
                drinkBitmap = BitmapFactory.decodeStream(inputStream)

                val bitmap = drinkBitmap
                if (bitmap != null) {
                    Palette.from(bitmap).generate {
                        val vibrantColor = it.getVibrantColor(baseContext.getColor(R.color.colorPrimary))
                        collapsingToolbar.setContentScrimColor(vibrantColor)
                        collapsingToolbar.setStatusBarScrimColor(baseContext.getColor(R.color.black_trans80))
                    }
                    Unit
                }
            } catch (_: MalformedURLException) {}
        }
        collapsingToolbar.title = recipe.title
        appBarLayout.addOnOffsetChangedListener { _, verticalOffset ->
            if (Math.abs(verticalOffset) > 200) {
                appBarExpanded = false
                invalidateOptionsMenu()
            } else {
                appBarExpanded = true
                invalidateOptionsMenu()
            }
            thread {
                addToRecentlyViewed(recipe)
            }
        }
    }

     private fun addToRecentlyViewed(recipe: DiscoveryRecipe) {
         val currentTime = System.currentTimeMillis()
         synchronized(BarAssistant.lastViewedRecipes) {
             synchronized(BarAssistant.lastViewedTimes) {
                 if (BarAssistant.lastViewedRecipes.values.map { it.toFireStoreRecipe() }.contains(recipe.toFireStoreRecipe())) {
                     val keysToRemove = BarAssistant.lastViewedRecipes.filter { it.value.toFireStoreRecipe() == recipe.toFireStoreRecipe() }.map { it.key }
                     keysToRemove.forEach {
                         BarAssistant.lastViewedRecipes.remove(it)
                         BarAssistant.lastViewedTimes.remove(it)
                     }
                 }
                 BarAssistant.lastViewedTimes.add(currentTime)
                 BarAssistant.lastViewedRecipes[currentTime] = recipe

                 BarAssistant.lastViewedTimes.sortByDescending { it }
                 if (BarAssistant.lastViewedTimes.count() > MAX_LAST_VIEWED) {
                     BarAssistant.lastViewedTimes.removeAt(BarAssistant.lastViewedTimes.count() - 1)
                 }
                 synchronized(BarAssistant.recipes) {
                     BarAssistant.recipes[1].clear()
                     BarAssistant.recipes[1].addAll(listOfNotNull(*BarAssistant.lastViewedTimes
                             .map { BarAssistant.lastViewedRecipes[it] }.toTypedArray()))
                 }
             }
         }
    }

    private fun addTags(recipe: DiscoveryRecipe) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        tagContainerDetail.removeAllViews()
        var tags = recipe.getTags()
        if(tags.count() > 4) tags = recipe.getTags().subList(0, 4)
        tags.forEach {
            val tag = inflater.inflate(R.layout.recipe_tag, tagContainerDetail, false) as TextView
            val drawable = tag.background as GradientDrawable
            drawable.setColor(it.getColor())
            tag.text = it.title
            tagContainerDetail.addView(tag)
        }
    }

    private fun clearMediaPlayer() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onPause() {
        super.onPause()
        clearMediaPlayer()
        val barAssistant = application as? BarAssistant
        barAssistant?.updateFavoriteFireStore(authorization, fireStore)
        barAssistant?.storeRecentlyViewedAndFavorites(authorization, fireStore)
    }

    private fun addReadListener(recipe: DiscoveryRecipe) {
        readForMe.setOnClickListener {
            if(lastReadTime == -1L || System.currentTimeMillis() - lastReadTime > 60_000) {
                clearMediaPlayer()
                mediaPlayer = MediaPlayer()
                val textToSpeech = TextToSpeech(HandleTtS(), mediaPlayer ?: return@setOnClickListener)
                textToSpeech.execute(recipe.title + ". " + recipe.description)
            }
            lastReadTime = System.currentTimeMillis()
        }
    }
}

package com.speakeasy.watsonbarassistant.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.speakeasy.watsonbarassistant.BarAssistant
import com.speakeasy.watsonbarassistant.R
import com.speakeasy.watsonbarassistant.SHARED_PREFERENCES_SETTINGS
import kotlinx.android.synthetic.main.activity_user_profile.*


class UserProfile : AppCompatActivity() {

    private val authorization: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        val email = BarAssistant.userInfo?.email ?: ""
        val firstName = BarAssistant.userInfo?.firstName ?: ""
        val lastName = BarAssistant.userInfo?.lastName ?: ""
        val fullName = "$firstName $lastName"
        profile_name.text = fullName
        profile_email.text = email
        profile_username.text = BarAssistant.userInfo?.username ?: ""
        sign_out.setOnClickListener {
            signOut()
        }
        change_password.setOnClickListener {
            authorization.sendPasswordResetEmail(email)
        }
    }

    private fun signOut() {
        synchronized(BarAssistant.recipes) {
            BarAssistant.recipes.forEach { it.clear() }
        }
        synchronized(BarAssistant.lastViewedRecipes) {
            BarAssistant.lastViewedRecipes.clear()
        }
        synchronized(BarAssistant.lastViewedTimes) {
            BarAssistant.lastViewedTimes.clear()
        }
        AuthUI.getInstance().signOut(this).addOnCompleteListener {
            val intent = Intent(this, Login::class.java)
            val preferences = getSharedPreferences(SHARED_PREFERENCES_SETTINGS, Context.MODE_PRIVATE)
            val editor = preferences.edit()
            editor.clear()
            editor.apply()
            startActivity(intent)
            finish()
        }
    }
}

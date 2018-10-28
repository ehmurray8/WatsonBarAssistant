package com.speakeasy.watsonbarassistant.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.speakeasy.watsonbarassistant.BarAssistant
import com.speakeasy.watsonbarassistant.R
import com.speakeasy.watsonbarassistant.SHARED_PREFERENCES_SETTINGS
import com.speakeasy.watsonbarassistant.extensions.toast
import kotlinx.android.synthetic.main.activity_user_profile.*


class UserProfile : AppCompatActivity() {

    private var authorization = FirebaseAuth.getInstance()
    private var currentUser = authorization.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        profile_name.text = currentUser?.displayName
        profile_email.text = currentUser?.email
        sign_out.setOnClickListener {
            signOut()
        }
        val email = currentUser?.email
        if(email != null && email == "") {
            add_email.visibility = View.VISIBLE
            add_email.setOnClickListener {
                addEmailDialog()
            }
        }
        change_password.setOnClickListener {
            if(email != null) {
                if(email != "") {
                    authorization.sendPasswordResetEmail(email)
                } else {
                    applicationContext.toast("Please add an email address.")
                }
            }
        }
    }

    private fun addEmailDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Email Address")
        val editText = EditText(this)
        builder.setView(editText)

        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            val newEmail = editText.text.toString()
            if(newEmail != "") {
                currentUser?.updateEmail(newEmail)?.addOnSuccessListener {
                    applicationContext.toast("Successfully updated email.")
                }?.addOnFailureListener {
                    reAuthenticateUser(newEmail)
                }
                profile_email.text = newEmail
            }
        }

        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun reAuthenticateUser(newEmail: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Verify Password")
        val editText = EditText(this)
        editText.transformationMethod = PasswordTransformationMethod.getInstance()
        builder.setView(editText)

        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            val authCredential = PhoneAuthProvider
                    .getCredential(currentUser?.phoneNumber ?: "", editText.text.toString())
            currentUser?.reauthenticate(authCredential)?.addOnSuccessListener {
                currentUser?.updateEmail(newEmail)?.addOnSuccessListener { _ ->
                    applicationContext.toast("Successfully updated email.")
                }?.addOnFailureListener { _ ->
                    applicationContext.toast("Failed to add email.")
                    profile_email.text = ""
                }
            }?.addOnFailureListener {
                applicationContext.toast("Failed to sign in to your account.")
                profile_email.text = ""
            }
        }

        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
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

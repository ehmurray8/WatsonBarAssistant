package com.speakeasy.watsonbarassistant

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
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
                    Toast.makeText(applicationContext, "Please add an email address.", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this, "Successfully updated email.", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this, "Successfully updated email.", Toast.LENGTH_SHORT).show()
                }?.addOnFailureListener { _ ->
                    Toast.makeText(this, "Failed to add email.", Toast.LENGTH_SHORT).show()
                    profile_email.text = ""
                }
            }?.addOnFailureListener {
                Toast.makeText(this, "Failed to sign in to your account.", Toast.LENGTH_SHORT).show()
                profile_email.text = ""
            }
        }

        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }

        builder.show()


    }

    private fun signOut() {
        AuthUI.getInstance().signOut(this).addOnCompleteListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }
    }
}

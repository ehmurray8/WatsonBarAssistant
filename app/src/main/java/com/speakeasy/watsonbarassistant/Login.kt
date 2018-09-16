package com.speakeasy.watsonbarassistant

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import net.hockeyapp.android.CrashManager
import net.hockeyapp.android.metrics.MetricsManager
import java.util.*

const val RC_SIGN_IN = 123

class Login : AppCompatActivity() {

    private var authorization = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        MetricsManager.register(application)
        if(authorization.currentUser == null) {
            createSignInIntent()
        } else {
            showMainMenu()
        }
    }

    override fun onResume() {
        super.onResume()
        checkForCrashes()
    }

    private fun checkForCrashes() {
        CrashManager.register(this)
    }

    private fun createSignInIntent() {
        val providers = Arrays.asList(AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.PhoneBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build())

        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                .setLogo(R.mipmap.ic_old_fashioned).setAvailableProviders(providers).build(),
                RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == RESULT_OK) {
                showMainMenu()
            } else {
                if(response != null) {
                    Toast.makeText(this, "Login failed.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showMainMenu() {
        val intent = Intent(this, MainMenu::class.java)
        startActivity(intent)
        finish()
    }
}

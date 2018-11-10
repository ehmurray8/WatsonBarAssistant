package com.speakeasy.watsonbarassistant.activity

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.EditText
import com.google.firebase.firestore.FirebaseFirestore
import com.speakeasy.watsonbarassistant.*
import kotlinx.android.synthetic.main.activity_user_info.*
import java.util.*

class UserInfoActivity : AppCompatActivity() {

    private val fireStore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)

        birthdayPicker.maxDate = Date().time

        birthdayPicker.isNestedScrollingEnabled = false

        val minDateCalendar = GregorianCalendar(1900, 1, 1)
        birthdayPicker.minDate = minDateCalendar.time.time

        val name = intent.getStringExtra("Name")
        val email = intent.getStringExtra("Email")
        val uid = intent.getStringExtra("UID")

        if(name != null && name != "") {
            val names = name.split(" ")
            firstNameEditText.text.insert(0, names[0])
            if(names.count() > 1) {
                lastNameEditText.text.insert(0, names[1])
            }
        }

        if(email != null && email != "") {
            emailEditText.text.insert(0, email)
        }

        saveUserInfo.setOnClickListener {
            saveUserInfo(uid)
        }
    }

    private fun saveUserInfo(uid: String) {
        val firstName = firstNameEditText.text.toString()
        val lastName = lastNameEditText.text.toString()
        val email = emailEditText.text.toString()
        val username = usernameEditText.text.toString()

        val year = birthdayPicker.year
        val month = birthdayPicker.month
        val day = birthdayPicker.dayOfMonth
        val birthday = "${month+1}/$day/$year"
        val birthdayCalendar = GregorianCalendar(year, month, day)
        val oldEnoughCalendar = GregorianCalendar()
        oldEnoughCalendar.add(Calendar.YEAR, -21)

        var valid = true
        valid = checkProperty(firstNameEditText, firstName) && valid
        valid = checkProperty(lastNameEditText, lastName) && valid
        valid = checkProperty(emailEditText, email) && valid
        valid = checkProperty(usernameEditText, username) && valid

        BarAssistant.allUsers.forEach {
            if(it.username == username) {
                valid = false
                usernameEditText.error = "Username is already taken"
                return
            }
        }

        if(birthdayCalendar > oldEnoughCalendar) {
            valid = false
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Sorry you have to be 21 to use this app.").setTitle("Age Restriction")
            val dialog: AlertDialog? = builder.create()
            dialog?.show()
        }
        if(valid) {
            val userInfo = UserInfo(firstName, lastName, username, email, birthday)
            fireStore.collection(APP_COLLECTION).document(uid).collection(USER_COLLECTION).document("main").set(userInfo)
            fireStore.collection(ALL_USERS_COLLECTION).document(MAIN_DOCUMENT).get().addOnSuccessListener {
                val storedIds = it.get(ALL_USERS_LIST) as? ArrayList<*>
                val requestIds = mutableListOf<String>()
                storedIds?.forEach { requestId ->
                    (requestId as? String)?.let { rid ->
                        requestIds.add(rid)
                    }
                }
                requestIds.add(uid)
                val newRequests = mapOf(ALL_USERS_LIST to requestIds)
                fireStore.collection(ALL_USERS_COLLECTION).document(MAIN_DOCUMENT).set(newRequests)
            }
            finish()
        }
    }

    private fun checkProperty(editText: EditText, value: String): Boolean {
        if(value == "") {
            editText.error = "This field cannot be blank"
            return false
        }
        return true
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you would like to exit the app?").setTitle("User Info")
        builder.setPositiveButton("Yes") { _, _ -> finishAffinity() }
        builder.setCancelable(true)
        val dialog: AlertDialog? = builder.create()
        dialog?.show()
    }
}

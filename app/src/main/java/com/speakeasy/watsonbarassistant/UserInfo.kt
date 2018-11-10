package com.speakeasy.watsonbarassistant

import java.io.Serializable

data class UserInfo(val firstName: String="", val lastName: String="", val username: String="",
                    val email: String="", val birthday: String=""): Serializable {

    var userId: String? = null
}
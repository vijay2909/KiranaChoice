package com.app.kiranachoice.models

data class User(
    var sequence: Long? = null,
    var phoneNumber: String? = null,
    var imageUrl: String? = null,
    var name: String? = null,
    var email: String? = null,
    var deviceToken: String? = null
)
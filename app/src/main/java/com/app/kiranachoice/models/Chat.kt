package com.app.kiranachoice.models

data class Chat(
    var msg : String? = null,
    var time : Long = 0L,
    var imageUrl : String? = null,
    var sender : String = "",
    var read : Boolean = false
)
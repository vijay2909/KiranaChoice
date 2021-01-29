package com.app.kiranachoice.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText


class OTPTextWatcher(private val etNext: EditText, private val etPrev: EditText) : TextWatcher {


    override fun afterTextChanged(editable: Editable?) {
        val text = editable.toString()
        if (text.length == 1) {
            etNext.requestFocus()
        } else if (text.isEmpty()) {
            etPrev.requestFocus()
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }
}
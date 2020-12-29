package com.app.kiranachoice.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.kiranachoice.data.User
import com.app.kiranachoice.utils.USER_REFERENCE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * This repository class do all the network stuff related to user
 * e.g. getUserDetails, editUserDetails and so on...
 * */
class UserRepository(val mAuth: FirebaseAuth, val dbFire: FirebaseFirestore) {

    private var _user = MutableLiveData<User>()
    val user: LiveData<User> get() = _user

    suspend fun getUserDetails() {
        withContext(Dispatchers.IO) {
            mAuth.currentUser?.let { user ->
                dbFire.collection(USER_REFERENCE).document(user.uid).get()
                    .addOnSuccessListener { snapShot ->
                        if (snapShot.exists()) {
                            _user.postValue(snapShot.toObject(User::class.java))
                        }
                    }
            }
        }
    }
}
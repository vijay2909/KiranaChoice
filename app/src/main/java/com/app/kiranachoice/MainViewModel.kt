package com.app.kiranachoice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.kiranachoice.models.User
import com.app.kiranachoice.utils.USER_REFERENCE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private var mAuth : FirebaseAuth? = null
    private var dbFire : FirebaseFirestore? = null

    private var _user = MutableLiveData<User>()
    val user : LiveData<User> get() = _user

    init {
        mAuth = FirebaseAuth.getInstance()
        dbFire = FirebaseFirestore.getInstance()
    }

    fun getUserDetails(){
        viewModelScope.launch(Dispatchers.IO){
            mAuth?.currentUser?.let { user ->
                dbFire?.collection(USER_REFERENCE)?.document(user.uid)
                    ?.get()
                    ?.addOnSuccessListener {snapShot ->
                        if (snapShot.exists()){
                            _user.postValue(snapShot.toObject(User::class.java))
                        }
                    }
            }
        }
    }
}
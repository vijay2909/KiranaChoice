package com.app.kiranachoice

import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.kiranachoice.repositories.DataRepository
import com.app.kiranachoice.utils.USER_IMAGE_REFERENCE
import com.app.kiranachoice.utils.USER_REFERENCE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel @ViewModelInject constructor(private val dataRepository: DataRepository) : ViewModel() {
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val dbFire: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var storage: FirebaseStorage? = null
    private var dbRef: FirebaseDatabase? = null


    val user = dataRepository.user

    var userName: String? = null
    var email: String? = null

    var fileExtension: String? = null
    private var _imageUrl = MutableLiveData<String>()
    val imageUrl: LiveData<String> get() = _imageUrl

    init {
        storage = FirebaseStorage.getInstance()
        dbRef = FirebaseDatabase.getInstance()

        getUserDetails()
    }

    private fun getUserDetails() = viewModelScope.launch {
        dataRepository.getUserDetails()
    }

    private var _currentProgress = MutableLiveData<Int>()
    val currentProgress: LiveData<Int> get() = _currentProgress

    fun updateImage(uri: Uri?) {
        viewModelScope.launch(Dispatchers.IO) {
            if (uri != null) {
                val ref: StorageReference? = storage?.getReference(USER_IMAGE_REFERENCE)

                val pathRef: StorageReference? =
                    ref?.child("${user.value?.phoneNumber} ${System.currentTimeMillis()}.$fileExtension")

                val uploadTask = pathRef?.putFile(uri)

                uploadTask?.addOnProgressListener {
                    val progress: Double =
                        100.0 * it.bytesTransferred / it.totalByteCount
                    _currentProgress.postValue(progress.toInt())
                }

                uploadTask?.addOnSuccessListener {
                    pathRef.downloadUrl.addOnSuccessListener { uri ->
                        _imageUrl.postValue(uri.toString())
                        if (user.value?.imageUrl != null) {
                            storage?.getReferenceFromUrl(user.value?.imageUrl.toString())?.delete()
                        }
                    }
                }
            }
        }
    }

    private var _onDetailsUpdate = MutableLiveData<Boolean>()
    val onDetailsUpdate: LiveData<Boolean> get() = _onDetailsUpdate

    fun saveData() {
        val userDetails = mapOf(
            "name" to userName,
            "email" to email,
            "imageUrl" to imageUrl.value
        )

        mAuth.currentUser?.let { user ->
            dbFire.collection(USER_REFERENCE).document(user.uid)
                .update(userDetails)
                .addOnSuccessListener {
                    getUserDetails()
                    _onDetailsUpdate.postValue(true)
                }
        }
    }

    fun onDetailsUpdated() {
        _onDetailsUpdate.value = false
    }

}


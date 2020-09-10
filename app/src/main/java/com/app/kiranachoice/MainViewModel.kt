package com.app.kiranachoice

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.kiranachoice.db.CartDao
import com.app.kiranachoice.db.CartDatabase
import com.app.kiranachoice.db.CartItem
import com.app.kiranachoice.models.ProductModel
import com.app.kiranachoice.models.User
import com.app.kiranachoice.repositories.CartRepo
import com.app.kiranachoice.utils.PRODUCT_REFERENCE
import com.app.kiranachoice.utils.USER_IMAGE_REFERENCE
import com.app.kiranachoice.utils.USER_REFERENCE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : ViewModel() {
    private var mAuth: FirebaseAuth? = null
    private var dbFire: FirebaseFirestore? = null
    private var storage: FirebaseStorage? = null
    private var dbRef: FirebaseDatabase? = null

    private val database: CartDatabase
    private val cartDao: CartDao
    private val cartRepo: CartRepo

    val allCartItems : LiveData<List<CartItem>>

    private var _user = MutableLiveData<User>()
    val user: LiveData<User> get() = _user

    var userName: String? = null

    var fileExtension: String? = null
    private var _imageUrl = MutableLiveData<String>()
    val imageUrl: LiveData<String> get() = _imageUrl

    init {
        mAuth = FirebaseAuth.getInstance()
        dbFire = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        dbRef = FirebaseDatabase.getInstance()

        database = CartDatabase.getInstance(application)
        cartDao = database.cartDao
        cartRepo = CartRepo(cartDao)

        allCartItems = cartRepo.allCartItems
    }

    fun getUserDetails() {
        viewModelScope.launch(Dispatchers.IO) {
            mAuth?.currentUser?.let { user ->
                dbFire?.collection(USER_REFERENCE)?.document(user.uid)
                    ?.get()
                    ?.addOnSuccessListener { snapShot ->
                        if (snapShot.exists()) {
                            _user.postValue(snapShot.toObject(User::class.java))
                        }
                    }
            }
        }
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
            "imageUrl" to imageUrl.value
        )

        mAuth?.currentUser?.let { user ->
            dbFire?.collection(USER_REFERENCE)?.document(user.uid)
                ?.update(userDetails)
                ?.addOnSuccessListener {
                    getUserDetails()
                    _onDetailsUpdate.postValue(true)
                }
        }
    }

    fun onDetailsUpdated() {
        _onDetailsUpdate.value = false
    }


    private var fakeList = ArrayList<ProductModel>()
    private var _resultList = MutableLiveData<List<ProductModel>>()
    val resultList: LiveData<List<ProductModel>> get() = _resultList

    fun getResultFromProducts(userInput: String) {
        dbRef?.getReference(PRODUCT_REFERENCE)
            ?.orderByChild("searchableText")?.startAt(userInput)?.endAt(userInput + "\uf8ff")
            ?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    fakeList.clear()
                    snapshot.children.forEach {
                        it.getValue(ProductModel::class.java)
                            ?.let { productModel -> fakeList.add(productModel) }
                    }
                    _resultList.postValue(fakeList)
                }

                override fun onCancelled(error: DatabaseError) {}

            })
    }


}
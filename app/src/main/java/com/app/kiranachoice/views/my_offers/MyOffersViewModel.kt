package com.app.kiranachoice.views.my_offers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.kiranachoice.data.OfferModel
import com.app.kiranachoice.utils.OFFER_REFERENCE
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MyOffersViewModel @Inject constructor() : ViewModel() {

    private var dbRef: FirebaseDatabase = FirebaseDatabase.getInstance()

    init {

        getOffers()
    }

    private var fakeOffersList = ArrayList<OfferModel>()
    private var _offersList = MutableLiveData<List<OfferModel>>()
    val offersList: LiveData<List<OfferModel>> get() = _offersList


    private fun getOffers() {
        dbRef.getReference(OFFER_REFERENCE)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        fakeOffersList.clear()
                        snapshot.children.forEach {
                            it.getValue(OfferModel::class.java)?.let { offerModel ->
                                fakeOffersList.add(offerModel)
                            }
                        }
                        _offersList.postValue(fakeOffersList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

}
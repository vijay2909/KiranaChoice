package com.app.kiranachoice.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.app.kiranachoice.data.db.AppDatabase
import com.app.kiranachoice.data.db.asDomainModel
import com.app.kiranachoice.data.ProductModel
import com.app.kiranachoice.data.ProductsList
import com.app.kiranachoice.data.SearchWord
import com.app.kiranachoice.data.asDatabaseModel
import com.app.kiranachoice.utils.PRODUCT_REFERENCE
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SearchRepository @Inject constructor(private val database: AppDatabase) {

    private val dbRef = FirebaseDatabase.getInstance()

    suspend fun refreshProducts() {
        dbRef.getReference(PRODUCT_REFERENCE)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val productList = ArrayList<ProductModel>()
                    snapshot.children.forEach { dataSnapshot ->
                        dataSnapshot.getValue(ProductModel::class.java)?.let { productModel ->
                            productList.add(productModel)
                        }
                    }
                    for (productModel in productList) {
                        Log.d("SearchRepository", "product: $productModel")
                    }
                    runBlocking {
                        withContext(Dispatchers.IO) {
                            database.databaseDao.insertSearchItems(ProductsList(productList).asDatabaseModel())
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}

            })
    }

    fun getSearchWords(searchText: String): LiveData<List<SearchWord>> =
        Transformations.map(database.databaseDao.getSearchWords("%$searchText%")) {
            it.asDomainModel()
        }

    val allSearchWords : LiveData<List<SearchWord>> = Transformations.map(database.databaseDao.getAllSearchWords()){
        it.asDomainModel()
    }

}
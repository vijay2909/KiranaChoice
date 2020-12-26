package com.app.kiranachoice.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CartItem::class, SearchItem::class], version = 2, exportSchema = false)
abstract class CartDatabase : RoomDatabase() {

    abstract val cartDao: CartDao

    companion object {

        @Volatile
        private var INSTANCE: CartDatabase? = null

        fun getInstance(context: Context): CartDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CartDatabase::class.java,
                    "Cart Database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
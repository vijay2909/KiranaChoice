package com.app.kiranachoice.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [CartItem::class, SearchItem::class, BannerImage::class, CategoryItem::class, ProductItem::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class CartDatabase : RoomDatabase() {

    abstract val databaseDao: DatabaseDao

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
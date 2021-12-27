package com.app.kiranachoice.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.kiranachoice.data.database_models.*

@Database(entities = [CartItem::class, SearchItem::class, BannerImage::class, CategoryItem::class, ProductItem::class, SubCategoryItem::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract val databaseDao: DatabaseDao
}
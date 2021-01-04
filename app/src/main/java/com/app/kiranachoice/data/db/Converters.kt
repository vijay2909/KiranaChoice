package com.app.kiranachoice.data.db

import androidx.room.TypeConverter
import com.app.kiranachoice.data.AboutProductModel
import com.app.kiranachoice.data.PackagingSizeModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class Converters {
    @TypeConverter
    fun fromString(value: String?): List<AboutProductModel> {
        val listType: Type = object : TypeToken<List<AboutProductModel>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromArrayList(list: List<AboutProductModel>): String? {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun fromValue(value: String?): List<PackagingSizeModel> {
        val listType: Type = object : TypeToken<List<PackagingSizeModel>>(){}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<PackagingSizeModel>): String? {
        return Gson().toJson(list)
    }


    @TypeConverter
    fun fromSearchValue(value: String?): List<String> {
        val listType: Type = object : TypeToken<List<String>>(){}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromSearchList(list: List<String>): String? {
        return Gson().toJson(list)
    }

}
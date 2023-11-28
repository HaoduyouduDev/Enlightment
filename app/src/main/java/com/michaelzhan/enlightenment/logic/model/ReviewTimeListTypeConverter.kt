package com.michaelzhan.enlightenment.logic.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ReviewTimeListTypeConverter {
    @TypeConverter
    fun jsonToModel(json: String): ArrayList<Long> {
        val typeToken = object : TypeToken<ArrayList<Long>>() {}.type
        return Gson().fromJson(json, typeToken)
    }

    @TypeConverter
    fun modelToJson(data: ArrayList<Long>): String {
        return Gson().toJson(data)
    }
}
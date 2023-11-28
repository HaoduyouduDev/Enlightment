package com.michaelzhan.enlightenment.logic.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SubTitlesTypeConverter {
    @TypeConverter
    fun jsonToModel(json: String): List<Pair<Int, String>> {
        val typeToken = object : TypeToken<List<Pair<Int, String>>>() {}.type
        return Gson().fromJson(json, typeToken)
    }

    @TypeConverter
    fun modelToJson(data: List<Pair<Int, String>>): String {
        return Gson().toJson(data)
    }
}
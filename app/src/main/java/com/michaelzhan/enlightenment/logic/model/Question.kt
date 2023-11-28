package com.michaelzhan.enlightenment.logic.model

import android.media.Image
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Entity
@TypeConverters(SubTitlesTypeConverter::class)
data class Question(var title: String, val createTime: Long, var sub_titles: List<Pair<Int, String>>, var proficiency: Float, var addToMemory: Boolean, val subjectId: Long) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}
package com.michaelzhan.enlightenment.logic.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey
import androidx.room.TypeConverters;

@Entity
@TypeConverters(ReviewTimeListTypeConverter::class)
data class ReviewTime(var questionId: Long, val reviewTime: ArrayList<Long>, var type: Int, val myReviewTime: ArrayList<Long> = ArrayList()) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}

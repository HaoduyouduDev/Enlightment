package com.michaelzhan.enlightenment.logic.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity
@TypeConverters(QuestionBindingTypeConverter::class)
data class ErrorBook(var name: String, var index: Int, var iconId: Long, var collect: Boolean, val createTime: Long, val bindingQuestions: ArrayList<Long>) { // save custom

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}
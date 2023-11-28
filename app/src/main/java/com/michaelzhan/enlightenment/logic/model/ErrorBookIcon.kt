package com.michaelzhan.enlightenment.logic.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ErrorBookIcon(var path: String, val createTime: Long, var isDefault: Boolean = true) { // save custom
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}
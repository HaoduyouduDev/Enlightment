package com.michaelzhan.enlightenment.logic

import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.michaelzhan.enlightenment.EnlightenmentApplication
import com.michaelzhan.enlightenment.logic.dao.ErrorBookDao
import com.michaelzhan.enlightenment.logic.dao.ErrorBookIconDao
import com.michaelzhan.enlightenment.logic.dao.QuestionDao
import com.michaelzhan.enlightenment.logic.dao.ReviewTimeDao
import com.michaelzhan.enlightenment.logic.model.ErrorBook
import com.michaelzhan.enlightenment.logic.model.ErrorBookIcon
import com.michaelzhan.enlightenment.logic.model.Question
import com.michaelzhan.enlightenment.logic.model.ReviewTime

@Database(version = 1, entities = [ErrorBook::class, ErrorBookIcon::class, Question::class, ReviewTime::class])
abstract class AppDatabase : RoomDatabase() {

    abstract fun errorBookDao(): ErrorBookDao
    abstract fun errorBookIconDao(): ErrorBookIconDao
    abstract fun questionDao(): QuestionDao
    abstract fun reviewTimeDao(): ReviewTimeDao

    companion object {

        private var instance: AppDatabase? = null

        @Synchronized
        fun getDatabase(): AppDatabase {
            Log.d("AppDatabase", "Create")
            instance?.let {
                return it
            }
            return Room.databaseBuilder(EnlightenmentApplication.context,
                AppDatabase::class.java, "app_database").allowMainThreadQueries()
                .build().apply {
                    instance = this
            }
        }
    }
}
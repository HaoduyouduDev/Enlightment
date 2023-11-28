package com.michaelzhan.enlightenment.logic.model

import com.michaelzhan.enlightenment.EnlightenmentApplication.Companion.defaultErrorIconPath
import com.michaelzhan.enlightenment.EnlightenmentApplication.Companion.defaultMoreErrorBookIconPath

val defaultErrorBookIcon = arrayListOf( // K: id, V: AbsPath
    ErrorBookIcon("$defaultErrorIconPath/Biology.png", System.currentTimeMillis(), true).apply { id=1 },
    ErrorBookIcon("$defaultErrorIconPath/Chemistry.png", System.currentTimeMillis(), true).apply { id=2 },
    ErrorBookIcon("$defaultErrorIconPath/Chinese.png", System.currentTimeMillis(), true).apply { id=3 },
    ErrorBookIcon("$defaultErrorIconPath/English.png", System.currentTimeMillis(), true).apply { id=4 },
    ErrorBookIcon("$defaultErrorIconPath/Geography.png", System.currentTimeMillis(), true).apply { id=5 },
    ErrorBookIcon("$defaultErrorIconPath/History.png", System.currentTimeMillis(), true).apply { id=6 },
    ErrorBookIcon("$defaultErrorIconPath/Math.png", System.currentTimeMillis(), true).apply { id=7 },
    ErrorBookIcon("$defaultErrorIconPath/Physics.png", System.currentTimeMillis(), true).apply { id=8 },
    ErrorBookIcon("$defaultErrorIconPath/Politics.png", System.currentTimeMillis(), true).apply { id=9 }
)

val defaultErrorBookMoreIcon = arrayListOf(
    ErrorBookIcon("$defaultMoreErrorBookIconPath/AI.png", System.currentTimeMillis(), true).apply { id=10 },
    ErrorBookIcon("$defaultMoreErrorBookIconPath/Art.png", System.currentTimeMillis(), true).apply { id=11 },
    ErrorBookIcon("$defaultMoreErrorBookIconPath/Book.png", System.currentTimeMillis(), true).apply { id=12 },
    ErrorBookIcon("$defaultMoreErrorBookIconPath/Computer.png", System.currentTimeMillis(), true).apply { id=13 },
    ErrorBookIcon("$defaultMoreErrorBookIconPath/Economy.png", System.currentTimeMillis(), true).apply { id=14 },
    ErrorBookIcon("$defaultMoreErrorBookIconPath/Electron.png", System.currentTimeMillis(), true).apply { id=15 },
    ErrorBookIcon("$defaultMoreErrorBookIconPath/Farming.png", System.currentTimeMillis(), true).apply { id=16 },
    ErrorBookIcon("$defaultMoreErrorBookIconPath/Law.png", System.currentTimeMillis(), true).apply { id=17 },
    ErrorBookIcon("$defaultMoreErrorBookIconPath/Maintenance.png", System.currentTimeMillis(), true).apply { id=18 },
    ErrorBookIcon("$defaultMoreErrorBookIconPath/Manage.png", System.currentTimeMillis(), true).apply { id=19 },
    ErrorBookIcon("$defaultMoreErrorBookIconPath/Martial.png", System.currentTimeMillis(), true).apply { id=20 },
    ErrorBookIcon("$defaultMoreErrorBookIconPath/Teach.png", System.currentTimeMillis(), true).apply { id=21 }



)

val defaultErrorBook = arrayListOf(
    ErrorBook("语文", 0, 3, true, System.currentTimeMillis(), ArrayList()).apply { id=1 },
    ErrorBook("数学", 1, 7, true, System.currentTimeMillis(), ArrayList()).apply { id=2 },
    ErrorBook("英语", 2, 4, true, System.currentTimeMillis(), ArrayList()).apply { id=3 },
    ErrorBook("历史", 3, 6, true, System.currentTimeMillis(), ArrayList()).apply { id=4 },
    ErrorBook("地理", 4, 5, true, System.currentTimeMillis(), ArrayList()).apply { id=5 },
    ErrorBook("化学", 6, 2, true, System.currentTimeMillis(), ArrayList()).apply { id=6 },
    ErrorBook("政治", 7, 9, true, System.currentTimeMillis(), ArrayList()).apply { id=7 },
    ErrorBook("生物", 8, 1, true, System.currentTimeMillis(), ArrayList()).apply { id=8 }
)

const val A_DAY = 1000L*60L*60L*24L
const val A_WEEK = A_DAY * 7
const val A_MONTH = A_DAY * 30
const val A_YEAR = A_DAY * 366

val autoStage = longArrayOf(
    A_DAY,  //day
    A_DAY * 2L,  // 2days
    A_DAY * 4L,  // 4days
    A_WEEK,  // a week
    A_DAY * 15L, // 15days
    A_MONTH, // a month
    A_MONTH * 3L, //3 months
    A_MONTH * 6L, //6 months
    A_YEAR, //a year
)
// https://zhuanlan.zhihu.com/p/343910476

const val AUTO_MAX_REVIEW = 9
const val DAY_MAX_REVIEW = 366
const val WEEK_MAX_REVIEW = 52
const val MONTH_MAX_REVIEW = 12

class QuestionSubTitleType {
    companion object {
        const val CONTENT = 1
        const val WHY = 2
        const val ANSWER = 3
        const val USER_CUSTOM = 4
    }
}

class ReviewType {
    companion object {
        const val AUTO = 1
        const val DAY = 2
        const val WEEK = 3
        const val MONTH = 4
        const val USER_CUSTOM = 5
    }
}
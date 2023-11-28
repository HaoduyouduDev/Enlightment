package com.michaelzhan.enlightenment

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import com.michaelzhan.enlightenment.logic.Repository
import com.michaelzhan.enlightenment.logic.model.defaultErrorBook
import com.michaelzhan.enlightenment.logic.model.defaultErrorBookIcon
import com.michaelzhan.enlightenment.logic.model.defaultErrorBookMoreIcon
import com.tencent.bugly.crashreport.CrashReport
import kotlinx.coroutines.*
import java.io.File

class EnlightenmentApplication : Application(){
    private val tag = "AppApplication"
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        lateinit var assetPath: String
        lateinit var userDataPath: String
        lateinit var tempPath: String

        lateinit var defaultErrorIconPath: String
        lateinit var defaultMoreErrorBookIconPath: String

        lateinit var userIconPath: String
        lateinit var userQuestionsImagesPath: String
        lateinit var userQuestionsRichTextPath: String

        lateinit var userReviewTempName: String
        lateinit var appConfigName: String
        lateinit var faDianUrl: String
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext

        CrashReport.initCrashReport(context, "1720ed8bfc", false);

        assetPath = context.filesDir.absolutePath + "/assets"
        userDataPath = context.filesDir.absolutePath + "/user"
        tempPath = context.filesDir.absolutePath + "/temp"

        defaultErrorIconPath = "$assetPath/errorBookIcon/default"
        defaultMoreErrorBookIconPath = "$assetPath/errorBookIcon/more"

        userIconPath = "$userDataPath/icons"
        userQuestionsImagesPath = "$userDataPath/userQuestions/images"
        userQuestionsRichTextPath = "$userDataPath/userQuestions/richText"

        userReviewTempName = "reviewTemp"
        appConfigName = "appConfig"

        faDianUrl = "https://afdian.net/a/Enlightenment"
        GlobalScope.launch {
            FileUtils.copyAssetsToSD("", assetPath, arrayListOf("model"))
            for (i in arrayListOf(userIconPath, userQuestionsImagesPath, userQuestionsRichTextPath)){
                Log.d(tag, "test $i")
                val mFile = File(i)
                if (!mFile.exists()) {
                    mFile.mkdirs()
                }
            }
        }
        GlobalScope.launch {
            if (Repository.getErrorBooksIconNum() == 0) {
                for (icon in defaultErrorBookIcon + defaultErrorBookMoreIcon) {
                    Log.e(tag, "add default")
                    Repository.addErrorBookIcon(icon)
                }
            }
            if (Repository.getErrorBooksNum() == 0) {
                for (errorBook in defaultErrorBook) {
                    Log.e(tag, "add default")
                    Repository.addErrorBook(errorBook)
                }
            }
            Log.d(tag, "test")
//            for (errorBook in AppDatabase.getDatabase().errorBookDao().loadAllErrorBooks().filter { b -> b.name == "test" }) {
//                AppDatabase.getDatabase().errorBookDao().deleteErrorBook(errorBook)
//                "Delete"
//            }
        }
    }
}
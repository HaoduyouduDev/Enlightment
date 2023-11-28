package com.michaelzhan.enlightenment

import android.graphics.Color
import com.michaelzhan.enlightenment.logic.model.Question
import java.util.regex.Matcher
import java.util.regex.Pattern

fun htmlToText(strHtml: String): String {
    var txtcontent = strHtml.replace("<br>", " ").replace("<img(.*?)>".toRegex(), "[Image]").replace("</?[^>]+>".toRegex(), "")
    txtcontent = txtcontent.replace("\\s+|\t|\r".toRegex(), " ")
    return txtcontent
}

fun getImagesPath(strHtml: String): ArrayList<String> {
    val mArrayList = ArrayList<String>()
    val pattern = Pattern.compile("src=\".*?\"")
    val m: Matcher = pattern.matcher(strHtml)
    while (m.find()) {
        val s: String = m.group()
        mArrayList.add(s.replace("src=\"", "").replace("\"", ""))
    }
    return mArrayList
}

fun getProficiencyTextAndColor(mQuestion: Question): Pair<String, Int> {
    return if (mQuestion.proficiency >= 0.8f) {
        Pair("熟练", Color.parseColor("#4CAF50"))
    }else if (mQuestion.proficiency in 0.4f..0.8f) {
        Pair("不是很熟练", Color.parseColor("#FFC107"))
    }else {
        Pair("不熟练", Color.parseColor("#FF5722"))
    }
}
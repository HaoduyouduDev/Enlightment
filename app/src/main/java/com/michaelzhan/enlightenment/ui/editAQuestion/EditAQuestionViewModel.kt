package com.michaelzhan.enlightenment.ui.editAQuestion

import android.util.Log
import androidx.lifecycle.ViewModel
import com.michaelzhan.enlightenment.logic.Repository
import com.michaelzhan.enlightenment.logic.model.Question
import com.michaelzhan.enlightenment.logic.model.ReviewTime

class EditAQuestionViewModel: ViewModel() {
    private val tag = "EditAQuestionViewModel"
    val tittleList = ArrayList<TittlesAdapter.Tittle>()
    var mQuestion: Question? = null
    var realComplete = false

    fun getQuestionFromId(data: Long) = Repository.getQuestionFromId(data)
    fun addSubTitle(arg1: Int, arg2: String) {
        if (mQuestion != null) {
            Repository.addQuestionSubTitle(mQuestion!!, arg1, arg2)
            tittleList.add(TittlesAdapter.Tittle(arg2, arg1, true))
        }else {
            Log.e(tag, "cannot add question's title, because mQuestion is null.")
        }
    }

    fun editQuestionTitle(newName: String) {
        if (mQuestion != null) {
            Repository.editQuestionName(mQuestion!!, newName)
            mQuestion?.title = newName
        }else {
            Log.e(tag, "cannot edit question's title, because mQuestion is null.")
        }
    }

    fun deleteUserCustomQuestionTitle(arg1: Int) {
        if (mQuestion != null) {
            Repository.deleteQuestionTitle(mQuestion!!, arg1).let {
                mQuestion!!.sub_titles = it
                tittleList.forEach { aTittle ->
                    if (aTittle.type == arg1) {
                        aTittle.isOpen = false
                    }
                }
            }
        }else {
            Log.e(tag, "cannot delete question's title, because mQuestion is null.")
        }
    }

    fun editReviewTime(type: Int, value: Long? = null) {
        if (mQuestion != null) {
            Repository.editReviewTimeTypeByQuestionId(mQuestion!!.id, type, value)
        }
    }

    fun getReviewTimeFromQuestionId() = Repository.getReviewTimeFromQuestionId(mQuestion!!.id)

    fun changeMemoryFromQuestion(isOpen: Boolean) = Repository.changeMemoryFromQuestion(mQuestion!!, isOpen)

    fun addReviewTime(data: ReviewTime) = Repository.addReviewTime(data)

    fun delReviewTime() {
        getReviewTimeFromQuestionId()?.let {
            Repository.deleteReviewTime(it)
        }
    }

    fun editQuestionProficiency(data: Float) = Repository.editQuestionProficiency(mQuestion!!, data)
}
package com.michaelzhan.enlightenment.ui.showAQuestion

import androidx.lifecycle.ViewModel
import com.michaelzhan.enlightenment.logic.Repository
import com.michaelzhan.enlightenment.logic.model.Question

class ShowQuestionViewModel: ViewModel() {
    var mQuestion: Question? = null
    val tittleList = ArrayList<TittlesAdapter.Tittle>()
    private val tag = "ShowQuestionViewModel"

    fun getQuestionFromId(data: Long) = Repository.getQuestionFromId(data)
}
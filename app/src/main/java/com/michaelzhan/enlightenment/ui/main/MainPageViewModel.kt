package com.michaelzhan.enlightenment.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.michaelzhan.enlightenment.logic.Repository
import com.michaelzhan.enlightenment.logic.model.ErrorBook
import com.michaelzhan.enlightenment.logic.model.ErrorBookIcon
import com.michaelzhan.enlightenment.logic.model.ReviewTime
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainPageViewModel : ViewModel() {
    val tag = "MainPageViewModel"

    val bookList = ArrayList<ErrorBook>()
    val errorBooksLiveData: LiveData<List<ErrorBook>> = Repository.getAllErrorBooks()
    val errorBooksIconLiveData: LiveData<List<ErrorBookIcon>> = Repository.getAllErrorBooksIcon()
    val reviewTimeLiveData = Repository.getAllReviewTime()
    val reviewTime = ArrayList<ReviewTime>()
    var spanCount = 0

    fun getErrorBookIconFromId(id: Long) = Repository.getErrorBookIconFromId(id)
}
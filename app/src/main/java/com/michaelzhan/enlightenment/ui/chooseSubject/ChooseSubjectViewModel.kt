package com.michaelzhan.enlightenment.ui.chooseSubject

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.michaelzhan.enlightenment.EnlightenmentApplication
import com.michaelzhan.enlightenment.FileUtils
import com.michaelzhan.enlightenment.logic.AppDatabase
import com.michaelzhan.enlightenment.logic.Repository
import com.michaelzhan.enlightenment.logic.model.ErrorBook
import com.michaelzhan.enlightenment.logic.model.ErrorBookIcon
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ChooseSubjectViewModel : ViewModel() {
    private val tag = "ChooseSubjectViewModel"
    val subjectList = ArrayList<ErrorBook>()
    val subjectLiveData: LiveData<List<ErrorBook>> = Repository.getAllErrorBooks()
    val allIconLiveData: LiveData<List<ErrorBookIcon>> = Repository.getAllErrorBooksIcon()
    val allIcon = ArrayList<ErrorBookIcon>()
    var spanCount = 2
    var spanCountInDialog = 3
    var mMode = ChooseSubjectMode.NORMAL

    fun getSubjectIconFromId(id: Long) = Repository.getErrorBookIconFromId(id)
    fun delIcon(icon: ErrorBookIcon) {
        allIcon.remove(icon)
        FileUtils.delete(icon.path)
        Repository.deleteIcon(icon)
    }
    fun addIcon(data: ErrorBookIcon) {
        val result = Repository.addErrorBookIcon(data)
//        allIcon.add(allIcon.size-1, data.apply { id = result })
    }
    fun addErrorBook(data: ErrorBook) {
        Repository.addErrorBook(data)
    }
    fun renameErrorBook(data: ErrorBook, name: String) = Repository.renameErrorBook(data, name)

    fun deleteErrorBook(data: ErrorBook) = Repository.deleteErrorBook(data)

    fun getAllIcon() = Repository.getAllErrorBooksIcon()
}
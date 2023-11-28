package com.michaelzhan.enlightenment.logic

import android.util.Log
import androidx.lifecycle.liveData
import com.michaelzhan.enlightenment.logic.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

object Repository {

    private const val tag = "Repository"

    private val appDataBase = AppDatabase.getDatabase()
    private val errorBookDao = appDataBase.errorBookDao()
    private val errorBookIconDao = appDataBase.errorBookIconDao()
    private val questionDao = appDataBase.questionDao()
    private val reviewTimeDao = appDataBase.reviewTimeDao()

    fun getAllErrorBooks() = errorBookDao.loadAllErrorBooks()

    fun getAllErrorBooksIcon() = errorBookIconDao.loadAllIcons()

    fun deleteErrorBook(book: ErrorBook) {
        for (i in book.bindingQuestions) {
            questionDao.loadQuestionFromId(i)?.let {
                deleteQuestion(it, false)
            }
        }
        errorBookDao.deleteErrorBook(book)
    }

    fun deleteIcon(icon: ErrorBookIcon) {
        errorBookIconDao.deleteIcon(icon)
    }

    fun getErrorBookIconFromId(id: Long) = errorBookIconDao.loadIconsFromId(id)

    fun getErrorBooksIconNum() = errorBookIconDao.getSize()

    fun getErrorBooksNum() = errorBookDao.getSize()

    fun getErrorBookFromId(id: Long) = errorBookDao.loadErrorBookFromId(id)

    fun getAllQuestions() = questionDao.loadAllQuestions()

    fun getQuestionFromId(id: Long) = questionDao.loadQuestionFromId(id)

    fun addErrorBook(data: ErrorBook) {
        errorBookDao.insertErrorBook(data)
    }

    fun addErrorBookIcon(data: ErrorBookIcon) {
        errorBookIconDao.insertIcon(data)
    }

    fun addQuestion(data: Question): Long {
        val questionId = questionDao.insertQuestion(data)
        errorBookDao.loadErrorBookFromId(data.subjectId).let {
            if (it != null) {
                errorBookDao.updateErrorBook(it.apply {
                    bindingQuestions.add(questionId)
                })
            }
        }
        return questionId
    }

    fun addQuestionSubTitle(data: Question, subTitleType: Int, subTitlePath: String) {
        questionDao.updateQuestion(data.apply {
            sub_titles = (sub_titles as ArrayList).apply {
                add(Pair(subTitleType, subTitlePath))
            }
        })
    }

    fun renameErrorBook(data: ErrorBook, mName: String) {
        errorBookDao.updateErrorBook(data.apply {
            name = mName
        })
    }

    fun editQuestionName(data: Question, newName: String) {
        questionDao.updateQuestion(data.apply {
            title = newName
        })
    }

    fun deleteQuestion(data: Question, deleteBookIds: Boolean = true) {
        if (data.subjectId != -1L && deleteBookIds) {
            val mBook = errorBookDao.loadErrorBookFromId(data.subjectId)
            if (mBook != null) {
                errorBookDao.updateErrorBook(
                    mBook.apply {
                        if (bindingQuestions.contains(data.id)) {
                            bindingQuestions.remove(data.id)
                        }
                    }
                )
            }
        }
        reviewTimeDao.loadReviewTimeFromQuestionId(data.id)?.let {
            reviewTimeDao.deleteReviewTime(
                it
            )
        }
        questionDao.deleteQuestion(data)
    }

    fun deleteQuestionTitle(data: Question, type: Int): ArrayList<Pair<Int, String>> {
        val mTitles = ArrayList<Pair<Int, String>>()
        questionDao.updateQuestion(
            data.apply {
                mTitles.addAll(data.sub_titles)
                sub_titles.forEach {
                    if (it.first == type) {
                        mTitles.remove(it)
                    }
                }
                data.sub_titles = mTitles
            }
        )
        return mTitles
    }
    
    fun addReviewTime(data: ReviewTime){
        reviewTimeDao.insertReviewTime(data)
    }
    
    fun editReviewTimeTypeByQuestionId(questionId: Long, mType: Int, value: Long? = null) {
        reviewTimeDao.loadReviewTimeFromQuestionId(questionId)?.let {
            Log.d(tag, "!= null 1")
            reviewTimeDao.updateReviewTime(it.apply {
                type = mType
                reviewTime.clear()
                val timeNow = System.currentTimeMillis()
                when (mType) {
                    ReviewType.AUTO -> {
                        for (i in 0 until AUTO_MAX_REVIEW) {
                            reviewTime.add(timeNow + autoStage[i])
                        }
                    }
                    ReviewType.DAY -> {
                        for (i in 0 .. DAY_MAX_REVIEW) {
                            reviewTime.add(timeNow + A_DAY * i)
                            Log.d(tag, "run 2")
                        }
                    }
                    ReviewType.WEEK -> {
                        for (i in 0 .. WEEK_MAX_REVIEW) {
                            reviewTime.add(timeNow + A_WEEK * i)
                        }
                    }
                    ReviewType.MONTH -> {
                        for (i in 0 .. MONTH_MAX_REVIEW) {
                            reviewTime.add(timeNow + A_MONTH * i)
                        }
                    }
                    ReviewType.USER_CUSTOM -> {
                        reviewTime.add(value!!)
                    }
                }
            })
        }
    }

    fun getReviewTimeFromQuestionId(questionId: Long) = reviewTimeDao.loadReviewTimeFromQuestionId(questionId)

    fun changeMemoryFromQuestion(data: Question, isOpen: Boolean) {
        questionDao.updateQuestion(
            data.apply {
                addToMemory = isOpen
            }
        )
    }

    fun deleteReviewTime(data: ReviewTime) = reviewTimeDao.deleteReviewTime(data)

    fun editQuestionProficiency(mQuestion: Question, data: Float) {
        questionDao.updateQuestion(
            mQuestion.apply {
                proficiency = data
            }
        )
    }

    fun changeQuestionProficiency(q: Question, p: Float) {
        questionDao.updateQuestion(
            q.apply {
                proficiency = p
            }
        )
    }

    fun getAllReviewTime() = reviewTimeDao.loadAllReviewTime()

//    private fun <T> fire(context: CoroutineContext, block: () -> Result<T>) =
//        liveData<Result<T>>(context) {
//            val result = try {
//                block()
//            } catch (e: Exception) {
//                Result.failure<T>(e)
//            }
//            emit(result)
//        }
}
package com.michaelzhan.enlightenment.ui.getQuestion

import androidx.lifecycle.ViewModel
import com.michaelzhan.enlightenment.YoloV7Ncnn
import com.michaelzhan.enlightenment.logic.Repository
import com.michaelzhan.enlightenment.logic.model.Question
import com.michaelzhan.enlightenment.ui.view.ChooseQuestionsView

class GetQuestionViewModel: ViewModel() {
    private val tag = "GetQuestionViewModel"
    val yolov7ncnn: YoloV7Ncnn = YoloV7Ncnn()
    var isEdit = false
    val questionBoxList = ArrayList<ChooseQuestionsView.QuestionBox>()
    var subjectId = -1L
    var isScan = false

    fun addQuestion(data: Question) = Repository.addQuestion(data)
}
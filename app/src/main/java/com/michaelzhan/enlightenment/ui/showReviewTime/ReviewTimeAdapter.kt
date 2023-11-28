package com.michaelzhan.enlightenment.ui.showReviewTime

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.kongzue.dialogx.dialogs.MessageDialog
import com.michaelzhan.enlightenment.*
import com.michaelzhan.enlightenment.logic.model.A_DAY
import com.michaelzhan.enlightenment.logic.model.Question
import com.michaelzhan.enlightenment.logic.model.ReviewTime
import com.michaelzhan.enlightenment.logic.model.ReviewType
import com.michaelzhan.enlightenment.ui.path2BitmapFromPath
import com.michaelzhan.enlightenment.ui.showAQuestion.ShowAQuestion
import com.michaelzhan.enlightenment.ui.view.LittleTitle
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ReviewTimeAdapter(private val activity: AppCompatActivity, private val ReviewTimeList: List<QuestionWithReviewTime>):
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val viewModel by lazy { ViewModelProviders.of(activity).get(ShowReviewTimeViewModel::class.java) }
    private val calendar = Calendar.getInstance();
    private val tag = "ReviewTimeAdapter"

    private inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val mTV: TextView = view.findViewById(R.id.review_time_list_question_content_text)
        val backgroundImg: ImageView = view.findViewById(R.id.review_time_list_question_image_background)
        val tittle: LittleTitle = view.findViewById(R.id.review_time_list_question_tittle)

        val seeQuestionButton: TextView = view.findViewById(R.id.review_time_list_question_see_question)
        val delQuestionButton: TextView = view.findViewById(R.id.review_time_list_question_delete_question)

        val proficiencyText: TextView = view.findViewById(R.id.review_time_list_question_proficiency_text)
        val nextReviewTimeTips: TextView = view.findViewById(R.id.review_time_list_question_next_review_time_tip)
        val subjectTextTips: TextView = view.findViewById(R.id.review_time_list_question_subject_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review_time_list_question, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.seeQuestionButton.setOnClickListener {
            activity.startActivity(Intent(activity, ShowAQuestion::class.java).apply {
                putExtra("questionId", ReviewTimeList[viewHolder.adapterPosition].question.id)
                putExtra("canEdit", false)
            })
        }
        viewHolder.delQuestionButton.setOnClickListener {
            MessageDialog.show("Tips", "确认要取消提醒吗", "Yes", "No").setOkButton { dialog, v ->
                val mSize = ReviewTimeList.size
                viewModel.deleteReviewTime(ReviewTimeList[viewHolder.adapterPosition], viewHolder.adapterPosition)
                activity.getSharedPreferences(EnlightenmentApplication.userReviewTempName, Context.MODE_PRIVATE).edit {
                    putInt("lastReviewPos", -1)
                }
                if (mSize == 1) {
                    activity.finish()
                }
                false
            }
        }
        return viewHolder
    }

    override fun getItemCount() = ReviewTimeList.size

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val question = ReviewTimeList[position]
        if (holder is ViewHolder) {
            val sb = StringBuilder()
            question.question.sub_titles.forEach {
                sb.append(FileUtils.readInternal(it.second))
            }
            val imagePaths = getImagesPath(
                sb.toString()
            )
            if (imagePaths.isNotEmpty()) {
                path2BitmapFromPath(imagePaths[0]).let {
                    val lp = holder.backgroundImg.layoutParams
                    if (it.height > 500) {
                        lp.height = 500
                    }else {
                        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    }
                    holder.backgroundImg.layoutParams = lp
                    holder.backgroundImg.setImageBitmap(it)
                }
            }

            holder.tittle.text = question.question.title
            holder.mTV.text = htmlToText(FileUtils.readInternal(question.question.sub_titles[0].second))
            GlobalScope.launch {
                val name = viewModel.getSubjectNameFromId(question.question.subjectId)
                holder.subjectTextTips.post {
                    holder.subjectTextTips.text = name
                }
            }
            var text = ""
            val prefs = activity.getSharedPreferences(EnlightenmentApplication.userReviewTempName, Context.MODE_PRIVATE)
            val mY = prefs.getInt("y", -1)
            val mM = prefs.getInt("m", -1)
            val mD = prefs.getInt("d", -1)
            calendar.set(mY, mM-1, mD, 0, 0, 0)
            val todayTime = calendar.timeInMillis

            val timeNow = System.currentTimeMillis()
            val mS = SimpleDateFormat("yyyy/MM/dd")

            var i = 0; var isFind = false
            while (i < question.reviewTime.reviewTime.size) {
                if (question.reviewTime.reviewTime[i] > timeNow) {
                    calendar.timeInMillis = question.reviewTime.reviewTime[i]
                    isFind = true
                    text = "下次复习时间: ${mS.format(calendar.time)}"
                }

                if(question.reviewTime.reviewTime[i] in todayTime..todayTime + A_DAY) {
                    isFind = true
                    text = "今天复习"
                }

                if (true) { // question.reviewTime.type != ReviewType.USER_CUSTOM
                    text += " ${
                        when(question.reviewTime.type) {
                            ReviewType.AUTO -> "自动"
                            ReviewType.DAY -> "每天"
                            ReviewType.WEEK -> "每周"
                            ReviewType.MONTH -> "每月"
                            ReviewType.USER_CUSTOM -> "单次"
                            else -> ""
                        }
                    }提醒"
                }

                if (isFind) break

                i++
            }
            if (!isFind) {
                text = "已完成复习"
            }

            holder.nextReviewTimeTips.text = text

            getProficiencyTextAndColor(question.question).let {
                holder.proficiencyText.text = it.first
                holder.proficiencyText.setTextColor(it.second)
            }
        }
    }

    data class QuestionWithReviewTime(val question: Question, val reviewTime: ReviewTime)
}
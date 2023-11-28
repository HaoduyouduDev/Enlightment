package com.michaelzhan.enlightenment.ui.questionsList

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.kongzue.dialogx.dialogs.MessageDialog
import com.michaelzhan.enlightenment.*
import com.michaelzhan.enlightenment.logic.Repository
import com.michaelzhan.enlightenment.logic.model.Question
import com.michaelzhan.enlightenment.ui.path2BitmapFromPath
import com.michaelzhan.enlightenment.ui.showAQuestion.ShowAQuestion
import com.michaelzhan.enlightenment.ui.view.LittleTitle

class QuestionsAdapter(private val activity: AppCompatActivity, private val QuestionList: List<Question>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val viewModel by lazy { ViewModelProviders.of(activity).get(QuestionListViewModel::class.java) }
    private val tag = "QuestionsAdapter2"

    private inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val mTV: TextView = view.findViewById(R.id.question_list_content_text)
        val backgroundImg: ImageView = view.findViewById(R.id.question_list_question_image_background)
        val title: LittleTitle = view.findViewById(R.id.question_list_question_tittle)

        val proficiencyText: TextView = view.findViewById(R.id.question_list_question_proficiency_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_question_list_question, parent, false)
        val viewHolder = ViewHolder(view)
        view.setOnClickListener {
            activity.startActivity(Intent(activity, ShowAQuestion::class.java).apply {
                putExtra("questionId", QuestionList[viewHolder.adapterPosition].id)
            })
        }
        view.setOnLongClickListener {
            MessageDialog.show("Tips", "确定要删除题目吗？", "Yes", "No").setOkButton { dialog, v ->
                val mSize = QuestionList.size
                viewModel.deleteQuestion(QuestionList[viewHolder.adapterPosition], viewHolder.adapterPosition)
                if (mSize == 1) {
                    activity.finish()
                }
                false
            }
            true
        }
        return viewHolder
    }

    override fun getItemCount() = QuestionList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val question = QuestionList[position]
        if (holder is ViewHolder) {
            val sb = StringBuilder()
            question.sub_titles.forEach {
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

            holder.title.text = question.title
            holder.mTV.text = htmlToText(FileUtils.readInternal(question.sub_titles[0].second))


            getProficiencyTextAndColor(question).let {
                holder.proficiencyText.text = it.first
                holder.proficiencyText.setTextColor(it.second)
            }

        }
    }


}
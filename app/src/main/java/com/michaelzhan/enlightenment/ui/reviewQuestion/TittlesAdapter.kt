package com.michaelzhan.enlightenment.ui.reviewQuestion

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.michaelzhan.enlightenment.FileUtils
import com.michaelzhan.enlightenment.R
import com.michaelzhan.enlightenment.logic.model.QuestionSubTitleType
import com.michaelzhan.enlightenment.ui.showImage.ShowImage
import com.michaelzhan.enlightenment.ui.view.LittleTitleWithADot
import com.scrat.app.richtext.RichEditText

class TittlesAdapter(private val activity: FragmentActivity, private val TittleList: List<Tittle>):
        RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val viewModel by lazy { ViewModelProviders.of(activity).get(ReviewQuestionViewModel::class.java) }
    private val tag = "TittleAdapter3"

    private inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val richTextView: RichEditText = view.findViewById(R.id.edit_a_question_tittle_rich_text)
        val dotView: LittleTitleWithADot = view.findViewById(R.id.edit_a_question_tittle_dot)
    }

    data class Tittle(val richTextPath: String, val type: Int)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_edit_a_question_tittle, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.richTextView.setBackgroundColor(Color.TRANSPARENT)
        viewHolder.richTextView.setOnImageClickListener {
            activity.startActivity(Intent(activity, ShowImage::class.java).apply {
                putExtra("imagePath", it)
            })
        }
        return viewHolder
    }

    override fun getItemCount() = TittleList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            val tittle = TittleList[position]
            holder.dotView.setDotState(true, false)
            holder.dotView.canChangeByUser(false)
            when (tittle.type) {
                QuestionSubTitleType.CONTENT -> {
                    holder.dotView.setText("题目")
                }
                QuestionSubTitleType.WHY -> {
                    holder.dotView.setText("错因")
                }
                QuestionSubTitleType.ANSWER -> {
                    holder.dotView.setText("答案")
                }
                QuestionSubTitleType.USER_CUSTOM -> {
                    holder.dotView.setText("其他")
                }
            }

            if (tittle.richTextPath.isNotEmpty()) {
                val content = FileUtils.readInternal(tittle.richTextPath)
                if (content.isNotEmpty()) {
                    holder.richTextView.fromHtml(FileUtils.readInternal(tittle.richTextPath))
                }else {
                    holder.richTextView.fromHtml("没有答案。")
                }
            }

            holder.richTextView.setCanEdit(false)
        }
    }
}
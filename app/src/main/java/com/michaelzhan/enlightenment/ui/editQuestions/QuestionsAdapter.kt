package com.michaelzhan.enlightenment.ui.editQuestions

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.kongzue.dialogx.dialogs.MessageDialog
import com.michaelzhan.enlightenment.EnlightenmentApplication
import com.michaelzhan.enlightenment.FileUtils
import com.michaelzhan.enlightenment.R
import com.michaelzhan.enlightenment.logic.model.Question
import com.michaelzhan.enlightenment.logic.model.QuestionSubTitleType
import com.michaelzhan.enlightenment.ui.editAQuestion.EditAQuestion
import com.michaelzhan.enlightenment.ui.view.LittleTitleWithADot
import com.scrat.app.richtext.RichEditText
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class QuestionsAdapter(private val activity: AppCompatActivity, private val QuestionList: List<Question>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val viewModel by lazy { ViewModelProviders.of(activity).get(EditQuestionsViewModel::class.java) }
    private val tag = "QuestionsAdapter"

    companion object {
        private const val ITEM_TYPE_CONTENT = 1
        private const val ITEM_TYPE_BOTTOM = 2
    }

    private inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val richTextViewContent: RichEditText = view.findViewById(R.id.edit_questions_question_rich_text_content_viewer)
        val richTextViewAnswer: RichEditText = view.findViewById(R.id.edit_questions_question_rich_text_answer_viewer)
        val dotContent: LittleTitleWithADot = view.findViewById(R.id.edit_questions_question_content_dot)
        val dotAnswer: LittleTitleWithADot = view.findViewById(R.id.edit_questions_question_answer_dot)
//        val tipText: TextView = view.findViewById(R.id.edit_questions_question_tips)
        val deleteButton: ImageView = view.findViewById(R.id.edit_questions_question_del_button)
    }

    private inner class FooterViewHolder(view: View): RecyclerView.ViewHolder(view) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        if (viewType == ITEM_TYPE_CONTENT) {
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_edit_questions_question, parent, false)
            val viewHolder = ViewHolder(view)
            view.setOnClickListener {
                activity.startActivity(Intent(activity, EditAQuestion::class.java).apply {
                    putExtra("questionId", QuestionList[viewHolder.adapterPosition].id)
                })
            }
            viewHolder.deleteButton.setOnClickListener {
                MessageDialog.show("Tips", "确定要删除题目吗", "Yes", "No").setOkButton { dialog, v ->
                    GlobalScope.launch {
                        viewModel.deleteQuestion(QuestionList[viewHolder.adapterPosition], viewHolder.adapterPosition)
                    }
                    false
                }.setCancelButton { dialog, v ->
                    false
                }
            }
            return viewHolder
        }else{
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_edit_questions_question_add, parent, false)
            val viewHolder = FooterViewHolder(view)
            view.setOnClickListener {
                (activity as EditQuestions).getImage()
//                val imgFilePath = EnlightenmentApplication.userQuestionsImagesPath + File.separator + System.currentTimeMillis().toString() + ".png"
//                cropPhoto(Uri.fromFile(File(imgFilePath)))
            }
            return viewHolder
        }
    }

    override fun getItemCount() = QuestionList.size + 1


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
//            if (position != 0) {
//                holder.tipText.visibility = View.GONE
//            }else {
//                holder.tipText.visibility = View.VISIBLE
//            }
            val question = QuestionList[position]
            var mHtml = ""
            var mAnswerHtml = ""
            question.sub_titles.forEach { subTitle ->
                if (subTitle.first == QuestionSubTitleType.CONTENT) {
                    mHtml = FileUtils.readInternal(subTitle.second)
                }
                if (subTitle.first == QuestionSubTitleType.ANSWER) {
                    mAnswerHtml = FileUtils.readInternal(subTitle.second)
                }
            }
            holder.dotContent.canChangeByUser(false)
            holder.dotAnswer.canChangeByUser(false)
            if (mAnswerHtml.isEmpty()) {
                holder.dotAnswer.setDotState(false)
                holder.richTextViewAnswer.visibility = View.GONE
            }else {
                holder.dotAnswer.setDotState(true)
                holder.richTextViewAnswer.fromHtml(mAnswerHtml)
                holder.richTextViewAnswer.visibility = View.VISIBLE
            }

            holder.richTextViewContent.fromHtml(mHtml)
            Log.d(tag, "adapter onBindViewHolder, $question")
            holder.richTextViewContent.setCanEdit(false)
            holder.richTextViewAnswer.setCanEdit(false)
        }else if (holder is FooterViewHolder) {

        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) {
            ITEM_TYPE_BOTTOM
        } else {
            ITEM_TYPE_CONTENT
        }
    }

    private fun cropPhoto(uri: Uri) {
        val options: UCrop.Options = UCrop.Options()
        // 修改标题栏颜色
        options.setToolbarColor(Color.parseColor("#FF9ADEFD"))
        // 修改状态栏颜色
//        options.setStatusBarColor(resources.getColor(R.color.teal_700))
        // 隐藏底部工具
        options.setHideBottomControls(true)
        // 图片格式
        options.setCompressionFormat(Bitmap.CompressFormat.PNG)
        // 设置图片压缩质量
        options.setCompressionQuality(100)
        // 是否让用户调整范围(默认false)，如果开启，可能会造成剪切的图片的长宽比不是设定的
        // 如果不开启，用户不能拖动选框，只能缩放图片
        options.setFreeStyleCropEnabled(true)
        // 设置图片压缩质量
        options.setCompressionQuality(100)
        // 不显示网格线
        options.setShowCropGrid(false)
        // 设置源uri及目标uri
        UCrop.of(
            uri,
            Uri.fromFile(
                File(
                    EnlightenmentApplication.userQuestionsImagesPath + File.separator +
                            System.currentTimeMillis().toString() + ".png"
                )
            )
        ) // 长宽比
            .withOptions(options)
            .start(activity)
    }
}
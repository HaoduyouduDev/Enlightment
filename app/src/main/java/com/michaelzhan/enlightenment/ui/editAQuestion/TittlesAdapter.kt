package com.michaelzhan.enlightenment.ui.editAQuestion

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.kongzue.dialogx.dialogs.MessageDialog
import com.kongzue.dialogx.dialogs.PopTip
import com.michaelzhan.enlightenment.EnlightenmentApplication
import com.michaelzhan.enlightenment.FileUtils
import com.michaelzhan.enlightenment.R
import com.michaelzhan.enlightenment.logic.model.QuestionSubTitleType
import com.michaelzhan.enlightenment.ui.HiddenAnimUtils
import com.michaelzhan.enlightenment.ui.view.LittleTitleWithADot
import com.scrat.app.richtext.RichEditText
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class TittlesAdapter(private val activity: AppCompatActivity, private val TittleList: List<Tittle>):
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val viewModel by lazy { ViewModelProviders.of(activity).get(EditAQuestionViewModel::class.java) }
    private var mFocusEditor: RichEditText? = null
    private var mFocusEditorPath: String = ""
    private var lastSelectionStart = -1
    private val tag = "TittlesAdapter"

    companion object {
        private const val ITEM_TYPE_CONTENT = 1
        private const val ITEM_TYPE_BOTTOM = 2
    }

    private inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val richTextView: RichEditText = view.findViewById(R.id.edit_a_question_tittle_rich_text)
        val dotView: LittleTitleWithADot = view.findViewById(R.id.edit_a_question_tittle_dot)
        var richTextHeight = 0
    }

    private inner class FooterViewHolder(view: View) :RecyclerView.ViewHolder(view) {
        // TODO
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        if (viewType == ITEM_TYPE_CONTENT) {
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_edit_a_question_tittle, parent, false)
            val viewHolder = ViewHolder(view)
            viewHolder.richTextView.addTextChangedListener {
//                if (viewHolder.richTextView.text?.isNotEmpty() != false) {
//
//                }
                viewHolder.richTextView.post {
                    viewHolder.richTextHeight = viewHolder.richTextView.height
                    Log.d(tag, "textChange height = ${viewHolder.richTextHeight}")
                }

                save(TittleList[viewHolder.adapterPosition].richTextPath, viewHolder.richTextView.toHtml())
            }
            viewHolder.richTextView.setOnFocusChangeListener { mView, b ->
                if (b) {
                    mFocusEditor = (mView as RichEditText)
                    mFocusEditorPath = TittleList[viewHolder.adapterPosition].richTextPath
                    Log.d("lastStart", lastSelectionStart.toString())
                }
            }

            viewHolder.richTextView.setOnSelectionChangedListener { start, end ->
                if (start != -1) {
                    lastSelectionStart = start
                }
            }

            viewHolder.dotView.setOnDotChangeListener {
                if (TittleList[viewHolder.adapterPosition].type == QuestionSubTitleType.USER_CUSTOM) {
                    MessageDialog.show("Tips", "确定要删除自定义标题吗？", "Yes", "No").setOkButton { dialog, v ->
                        GlobalScope.launch {
                            if (mFocusEditor == viewHolder.richTextView) {
                                mFocusEditor = null
                            }
                            viewModel.deleteUserCustomQuestionTitle(QuestionSubTitleType.USER_CUSTOM)
                            viewModel.tittleList.remove(TittleList[viewHolder.adapterPosition])
                            activity.runOnUiThread {
                                notifyItemRemoved(viewHolder.adapterPosition)
                            }
                        }
                        false
                    }.setCancelButton { dialog, v ->
                        HiddenAnimUtils.newInstance(viewHolder.richTextView, viewHolder.richTextHeight).toggle(true)
                        viewHolder.dotView.setDotState(true, false)
                        false
                    }
                }else {
                    save(TittleList[viewHolder.adapterPosition].richTextPath, "")
                }
                HiddenAnimUtils.newInstance(viewHolder.richTextView, viewHolder.richTextHeight).toggle(it)
                TittleList[viewHolder.adapterPosition].isOpen = it
            }

            return viewHolder
        }else {
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_edit_a_question_tittle_add, parent, false)
            val viewHolder = FooterViewHolder(view)
            view.setOnClickListener {
                if (!checkHas()) {
                    if (viewModel.mQuestion != null) {
                        GlobalScope.launch {
                            viewModel.addSubTitle(QuestionSubTitleType.USER_CUSTOM, EnlightenmentApplication.userQuestionsRichTextPath + File.separator + System.currentTimeMillis().toString() + "_UserCustom.dt")
                            activity.runOnUiThread {
                                notifyItemInserted(TittleList.size)
                            }
                        }
                    }
                }else {
                    PopTip.show("you already add")
                    Log.d(tag, "test")
                }
            }
            return viewHolder
        }
    }

    override fun getItemCount() = TittleList.size + 1

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            val tittle = TittleList[position]
//            HiddenAnimUtils.newInstance(holder.richTextView).toggle(tittle.isOpen)
            holder.dotView.setDotState(tittle.isOpen, false)
            when(tittle.type) {
                QuestionSubTitleType.CONTENT -> {
                    holder.dotView.canChangeByUser(false)
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
                }
            }

            holder.richTextView.post {
                holder.richTextHeight = holder.richTextView.height
                Log.d(tag, "new height =  ${holder.richTextHeight}")
                holder.richTextView.visibility = if(tittle.isOpen) View.VISIBLE else View.GONE
            }
            // TODO Question 内容不能为空，要判断
        }else if (holder is FooterViewHolder) {
            // None Action
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) {
            ITEM_TYPE_BOTTOM
        }else {
            ITEM_TYPE_CONTENT
        }
    }

    fun setBold() {
        if (mFocusEditor != null) {
            mFocusEditor?.bold(!mFocusEditor!!.contains(RichEditText.FORMAT_BOLD))
            save(mFocusEditorPath, mFocusEditor!!.toHtml())
        }
    }

    fun setItalic() {
        if (mFocusEditor != null) {
            mFocusEditor?.italic(!mFocusEditor!!.contains(RichEditText.FORMAT_ITALIC))
            save(mFocusEditorPath, mFocusEditor!!.toHtml())
        }
    }

    fun setUnderline() {
        if (mFocusEditor != null) {
            mFocusEditor?.underline(!mFocusEditor!!.contains(RichEditText.FORMAT_UNDERLINED))
            save(mFocusEditorPath, mFocusEditor!!.toHtml())
        }
    }

    fun setStrikethrough() {
        if (mFocusEditor != null) {
            mFocusEditor?.strikethrough(!mFocusEditor!!.contains(RichEditText.FORMAT_STRIKETHROUGH))
            save(mFocusEditorPath, mFocusEditor!!.toHtml())
        }
    }

    fun setBullet() {
        if (mFocusEditor != null) {
            mFocusEditor?.bullet(!mFocusEditor!!.contains(RichEditText.FORMAT_BULLET))
            save(mFocusEditorPath, mFocusEditor!!.toHtml())
        }
    }

    fun setQuote() {
        if (mFocusEditor != null) {
            mFocusEditor?.quote(!mFocusEditor!!.contains(RichEditText.FORMAT_QUOTE))
            save(mFocusEditorPath, mFocusEditor!!.toHtml())
        }
    }

    fun insertImage(path: String) {
        if (lastSelectionStart != -1) {
            mFocusEditor?.setSelection(lastSelectionStart)
        }else {
            mFocusEditor?.setSelection(0)
        }
        mFocusEditor?.image(path)
    }

    private fun checkHas(): Boolean {
        for (i in TittleList) {
            if (i.type == QuestionSubTitleType.USER_CUSTOM) {
                return true
            }
        }
        return false
    }

    data class Tittle(val richTextPath: String, val type: Int, var isOpen: Boolean)

    private fun save(path: String, content: String) {
        FileUtils.writeInternal(path, content)
    }
}
package com.michaelzhan.enlightenment.ui.chooseSubject

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.setPadding
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.dialogs.InputDialog
import com.kongzue.dialogx.dialogs.MessageDialog
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.DialogLifecycleCallback
import com.kongzue.dialogx.interfaces.OnBindView
import com.michaelzhan.enlightenment.R
import com.michaelzhan.enlightenment.dp2Px
import com.michaelzhan.enlightenment.isPad
import com.michaelzhan.enlightenment.logic.model.ErrorBook
import com.michaelzhan.enlightenment.ui.getQuestion.GetQuestion
import com.michaelzhan.enlightenment.ui.path2BitmapFromPath
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class SubjectsAdapter(private val activity: AppCompatActivity, private val SubjectsList: List<ErrorBook>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val viewModel by lazy { ViewModelProviders.of(activity).get(ChooseSubjectViewModel::class.java) }
    private val tag = "SubjectAdapter"
    private var maxTextLen:Float = 0f
    private var padding:Int = 0
    private lateinit var mIconAdapter: IconAdapter

    companion object {
        private const val ITEM_TYPE_CONTENT = 1
        private const val ITEM_TYPE_BOTTOM = 2
    }

    private inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val subjectName: TextView = view.findViewById(R.id.chooseSubject_subject_subjectName)
        val subjectImage: ImageView = view.findViewById(R.id.chooseSubject_subject_subjectImage)
        val lRoot: LinearLayout = view.findViewById(R.id.chooseSubject_subject_lRoot)
        val bottomTips: TextView = view.findViewById(R.id.chooseSubject_subject_bottom_tips)
    }

    private inner class FooterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.chooseSubject_subject_subjectImage)
        val buttonText: TextView = view.findViewById(R.id.chooseSubject_subject_subjectName)
        val lRoot: LinearLayout = view.findViewById(R.id.chooseSubject_subject_lRoot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_choosesubject_subject, parent, false)
        if (viewType == ITEM_TYPE_CONTENT) {
            val viewHolder = ViewHolder(view)
            view.setOnClickListener {
                if (viewModel.mMode != ChooseSubjectMode.EDIT_SUBJECTS) {
                    activity.finish()
                    activity.startActivity(Intent(activity, GetQuestion::class.java).apply {
                        putExtra("subjectId", SubjectsList[viewHolder.adapterPosition].id)
                    })
                }else {
                    InputDialog.show("Tips", "请输入新的学科名称", "Ok", "Cancel", SubjectsList[viewHolder.adapterPosition].name).setOkButton { dialog, v, inputStr ->
                        if (inputStr.isNotEmpty()) {
                            GlobalScope.launch {
                                viewModel.renameErrorBook(SubjectsList[viewHolder.adapterPosition], inputStr)
                            }
                        }
                        false
                    }
                }
            }

            view.setOnLongClickListener {
                if (viewModel.mMode == ChooseSubjectMode.EDIT_SUBJECTS) {
                    MessageDialog.show("Tips", "确认要删除学科吗？\n错题本中的错题也会一起被删除。", "Yes", "No").setOkButton { dialog, v ->
                        GlobalScope.launch {
                            viewModel.deleteErrorBook(SubjectsList[viewHolder.adapterPosition])
                        }
                        false
                    }
                }
                true
            }

            return viewHolder
        }else {
            val viewHolder = FooterViewHolder(view)
            view.setOnClickListener {
                InputDialog("Tips", "请输入新的科目标题", "确定", "取消", "")
                    .setCancelable(false)
                    .setOkButton { baseDialog, baseView, inputStr ->
                        if (inputStr.isNotEmpty()) {
                            viewModel.spanCountInDialog = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !activity.isInMultiWindowMode) {
                                when (activity.resources.configuration.orientation) {
                                    Configuration.ORIENTATION_LANDSCAPE -> 5
                                    Configuration.ORIENTATION_PORTRAIT -> 3
                                    else -> 4
                                }
                            }else{
                                3
                            }

                            CustomDialog.build()
                                .setCancelable(false)
                                .setCustomView(object : OnBindView<CustomDialog>(R.layout.dialog_choose_subject_add_subject_choose_img) {
                                    @SuppressLint("NotifyDataSetChanged")
                                    override fun onBind(dialog: CustomDialog, v: View) {
                                        val mOKButton = v.findViewById<TextView>(R.id.dialog_choose_subject_add_subject_subject_choose_img_ok)
                                        val mListView = v.findViewById<RecyclerView>(R.id.dialog_choose_subject_add_subject_subject_choose_img_icon_list)
                                        mIconAdapter = IconAdapter(activity, viewModel.allIcon)
                                        mIconAdapter.onChooseIcon { icon ->
                                            GlobalScope.launch {
                                                viewModel.addErrorBook(ErrorBook(inputStr, Int.MAX_VALUE, icon.id, true, System.currentTimeMillis(), ArrayList()))
                                                activity.runOnUiThread {
                                                    dialog.dismiss()
                                                    baseDialog.dismiss()
                                                }
                                            }
                                        }
                                        mListView.apply {
                                            layoutManager = GridLayoutManager(activity, viewModel.spanCountInDialog)
                                            adapter = mIconAdapter
                                        }
                                        mOKButton.setOnClickListener {
                                            dialog.dismiss()
                                            baseDialog.dismiss()
                                        }
                                    }
                                }).setDialogLifecycleCallback(object : DialogLifecycleCallback<CustomDialog>() {
                                    override fun onDismiss(dialog: CustomDialog?) {
                                        // dismiss
                                    }
                                }).show()
                        }else {
                            PopTip.show("标题不能为空");
                        }

                        inputStr.isEmpty()
                    }
                    .show()
            }
            return viewHolder
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            val subject = SubjectsList[position]
            holder.subjectName.text = subject.name
            if (viewModel.mMode == ChooseSubjectMode.EDIT_SUBJECTS) {
                holder.bottomTips.visibility = View.VISIBLE
                holder.bottomTips.text = "(${subject.bindingQuestions.size} 题)"
            }else {
                holder.bottomTips.visibility = View.GONE
            }
            GlobalScope.launch {
                val mIcon = viewModel.getSubjectIconFromId(subject.iconId)
                if (mIcon != null) {
                    holder.subjectImage.post {
                        holder.subjectImage.setImageBitmap(path2BitmapFromPath(mIcon.path))
                    }
                }
            }
        }else if (holder is FooterViewHolder){
            holder.buttonText.text = "Add"
            holder.imageView.setImageDrawable(AppCompatResources.getDrawable(activity, R.drawable.ic_vector_drawable_add_or_more))
            holder.imageView.setPadding(30)
        }

        if (activity.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            var lRoot: LinearLayout? = null
            var lTV: TextView? = null
            var lIV: ImageView? = null
            if (holder is ViewHolder) {
                lRoot = holder.lRoot
                lTV = holder.subjectName
                lIV = holder.subjectImage
            }else if (holder is FooterViewHolder) {
                lRoot = holder.lRoot
                lTV = holder.buttonText
                lIV = holder.imageView
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && activity.isInMultiWindowMode || !isPad()){
                lTV!!.textSize = 16f
                val lp = (lTV.layoutParams as LinearLayout.LayoutParams)
                lp.marginStart = 3
                lTV.layoutParams = lp
                lIV!!.setPadding(30)
            }else {
                lRoot!!.setPadding(lRoot.paddingLeft,lRoot.paddingTop,lRoot.paddingRight,20+80)
            }

            maxTextLen = 0f
            if (true) { // maxTextLen == 0f
                val mp = lTV!!.paint
                SubjectsList.forEach { b ->
                    mp.measureText(b.name).let {
                        if (it > maxTextLen) {
                            maxTextLen = it
                        }
                    }
                }
                if (maxTextLen < dp2Px(175) && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !activity.isInMultiWindowMode && isPad())) padding = 100
                else padding = 50
                Log.d(tag, padding.toString())
            }

            val lp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            if (viewModel.spanCount == 2){
                if (position%2 == 0) {
                    lp.gravity = Gravity.END
                    lp.marginEnd = padding
                }else {
                    lp.gravity = Gravity.START
                    lp.marginStart = padding
                }
            }
            lRoot!!.layoutParams = lp
            lTV!!.layoutParams.width = maxTextLen.toInt()
        }
    }

    override fun getItemViewType(position: Int): Int {
        //头部view
        return if (position == itemCount - 1) {
            ITEM_TYPE_BOTTOM
        } else {
            ITEM_TYPE_CONTENT
        }
    }

    override fun getItemCount() = SubjectsList.size + 1

    private fun getLength(str: String): Int {
        var len = 0
        for (element in str) {
            len = if (element >= 32.toChar() && element <= 127.toChar()) { // 半角
                len + 1
            } else { // 全角
                len + 2
            }
        }
        return len
    }

    fun getIconAdapter(): IconAdapter? {
        return if (::mIconAdapter.isInitialized) {
            mIconAdapter
        }else {
            null
        }
    }
}
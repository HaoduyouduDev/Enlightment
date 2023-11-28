package com.michaelzhan.enlightenment.ui.chooseSubject

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.setPadding
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.kongzue.dialogx.dialogs.MessageDialog
import com.kongzue.dialogx.dialogs.PopTip
import com.michaelzhan.enlightenment.R
import com.michaelzhan.enlightenment.dp2Px
import com.michaelzhan.enlightenment.isPad
import com.michaelzhan.enlightenment.ui.makeToast
import com.michaelzhan.enlightenment.logic.model.ErrorBookIcon
import com.michaelzhan.enlightenment.ui.path2BitmapFromPath
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class IconAdapter(private val activity: AppCompatActivity, private val IconList: List<ErrorBookIcon>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val viewModel by lazy { ViewModelProviders.of(activity).get(ChooseSubjectViewModel::class.java) }
    private val tag = "IconAdapter"
    private lateinit var mAction: (ErrorBookIcon) -> Unit

    companion object {
        private const val ITEM_TYPE_CONTENT = 1
        private const val ITEM_TYPE_BOTTOM = 2
    }

    private inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconImage: ImageView = view.findViewById<ImageView>(R.id.item_choose_subject_icon_icon_img)
    }

    private inner class FooterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val addImage: ImageView = view.findViewById<ImageView>(R.id.item_choose_subject_icon_icon_img)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_choose_subject_icon, parent, false)
        if (viewType == ITEM_TYPE_CONTENT) {
            val viewHolder = ViewHolder(view)
            view.setOnLongClickListener {
                MessageDialog.show("Tips", "确定删除图标吗？").setMaxWidth(dp2Px(400).toInt()).setCancelButton("No").setOkButton("Yes") { dialog, v ->
                    val mIcon = IconList[viewHolder.adapterPosition]
                    if (!mIcon.isDefault) {
                        GlobalScope.launch {
                            viewModel.delIcon(mIcon)
                            activity.runOnUiThread {
                                notifyItemRemoved(viewHolder.adapterPosition)
                            }
                        }
                    }else {
                        PopTip.show("You can't delete default icon.")
                    }
                    false
                }
                true
            }
            view.setOnClickListener {
                if (::mAction.isInitialized) {
                    mAction(IconList[viewHolder.adapterPosition])
                }
            }
            if (!isPad()) {
                viewHolder.iconImage.setPadding(10)
            }
            return viewHolder
        }else {
            val viewHolder = FooterViewHolder(view)
            view.setOnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                // 指定只显示照片
                intent.type = "image/*"
                activity.startActivityForResult(intent, 2)
            }
            return viewHolder
        }
    }

    override fun getItemCount() = IconList.size + 1
    private fun realItemCount() = IconList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            val icon = IconList[position]
            path2BitmapFromPath(icon.path).let {
                holder.iconImage.setImageBitmap(it)
            }
        }else if(holder is FooterViewHolder) {
            holder.addImage.setImageDrawable(AppCompatResources.getDrawable(activity, R.drawable.ic_vector_drawable_add_or_more))
            holder.addImage.setPadding(30)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) {
            ITEM_TYPE_BOTTOM
        } else {
            ITEM_TYPE_CONTENT
        }
    }

    fun onGetImageCallBack(path: String?) {
        if (path == null) {
            "Created fail".makeToast()
        }else {
            GlobalScope.launch {
                viewModel.addIcon(ErrorBookIcon(path, System.currentTimeMillis(), false))
                activity.runOnUiThread {
                    notifyItemInserted(realItemCount())
                }
            }
        }
    }

    fun onChooseIcon(f: (ErrorBookIcon) -> Unit){
        mAction = f
    }
}
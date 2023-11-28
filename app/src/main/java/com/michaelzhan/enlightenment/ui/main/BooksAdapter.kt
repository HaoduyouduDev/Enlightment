package com.michaelzhan.enlightenment.ui.main

import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.kongzue.dialogx.dialogs.PopTip
import com.michaelzhan.enlightenment.logic.model.ErrorBook
import com.michaelzhan.enlightenment.R
import com.michaelzhan.enlightenment.isPad
import com.michaelzhan.enlightenment.ui.path2BitmapFromPath
import com.michaelzhan.enlightenment.ui.questionsList.QuestionsList
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BooksAdapter(private val activity: AppCompatActivity, private val booksList: List<ErrorBook>) :
        RecyclerView.Adapter<BooksAdapter.ViewHolder>() {

    private val viewModel by lazy { ViewModelProviders.of(activity).get(MainPageViewModel::class.java) }
    private val tag = "BooksAdapter"

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bookImage: ImageView = view.findViewById(R.id.mainPage_errorBook_bookImage)
        val bookName: TextView = view.findViewById(R.id.mainPage_errorBook_bookName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mainpage_errorbook, parent, false)
        val viewHolder = ViewHolder(view)
        view.setOnClickListener {
            if (booksList[viewHolder.adapterPosition].bindingQuestions.isNotEmpty()) {
                activity.startActivity(Intent(activity, QuestionsList::class.java).apply {
                    putExtra("subjectId", booksList[viewHolder.adapterPosition].id)
                })
            }else {
                PopTip.show("你还没有添加错题")
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = booksList[position]
        holder.bookName.text = book.name
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && activity.isInMultiWindowMode || !isPad()) {
            holder.bookImage.setPadding(80,0,80,0)
        }
        GlobalScope.launch {
            val mIcon = viewModel.getErrorBookIconFromId(book.iconId)
            if (mIcon != null) {
                holder.bookImage.post {
                    holder.bookImage.setImageBitmap(path2BitmapFromPath(mIcon.path))
                }
            }
        }
    }

    override fun getItemCount() = booksList.size
}
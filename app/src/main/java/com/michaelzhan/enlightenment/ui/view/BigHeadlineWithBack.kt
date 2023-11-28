package com.michaelzhan.enlightenment.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.michaelzhan.enlightenment.R
import com.michaelzhan.enlightenment.isPad
import com.michaelzhan.enlightenment.ui.makeToast

@SuppressLint("Recycle")
class BigHeadlineWithBack(context: Context, attrs: AttributeSet): LinearLayout(context, attrs) {

    private var view: View
    private lateinit var mListener: () -> Unit

    init {
        view = LayoutInflater.from(context).inflate(R.layout.layout_big_headline_with_back, this)
        val ta = context.obtainStyledAttributes(attrs, R.styleable.BigHeadlineWithBack)

        val customText = ta.getString(R.styleable.BigHeadlineWithBack_text)
        if (customText != null) setText(customText)

        view.findViewById<ImageView>(R.id.big_headline_with_back_backButton).setOnClickListener {
            if (::mListener.isInitialized) {
                mListener()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && (context as AppCompatActivity).isInMultiWindowMode || !isPad()) {
            view.findViewById<TextView>(R.id.big_headline_with_back_tittleText).textSize = 33f
        }
    }

    fun setText(arg: String) {
        view.findViewById<TextView>(R.id.big_headline_with_back_tittleText).text = arg
    }
    fun getText() = view.findViewById<TextView>(R.id.big_headline_with_back_tittleText).text.toString()
    fun setOnBackClickListener(listener: () -> Unit) {
        mListener = listener
    }
}
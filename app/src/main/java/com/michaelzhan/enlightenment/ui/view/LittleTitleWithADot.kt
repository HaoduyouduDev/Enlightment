package com.michaelzhan.enlightenment.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.lihang.ShadowLayout
import com.michaelzhan.enlightenment.R

@SuppressLint("Recycle")
class LittleTitleWithADot(context: Context, attrs: AttributeSet): LinearLayout(context, attrs) {

    private var view: View
    private lateinit var mAction: (Boolean) -> Unit
    private var isLight = false
    private val dotLightColor = Color.parseColor("#367CCC")
    private val dotDarkColor = Color.parseColor("#7A7B7C")
    private var canChange = true

    init {
        view = LayoutInflater.from(context).inflate(R.layout.layout_little_title_with_a_dot, this)
        val ta = context.obtainStyledAttributes(attrs, R.styleable.LittleTitleWithADot)

        val customText = ta.getString(R.styleable.LittleTitleWithADot_text)
        if (customText != null) setText(customText)

        view.setOnClickListener {
            if (canChange) {
                setDotState(!isLight, true)
            }
        }
        val isLight = ta.getBoolean(R.styleable.LittleTitleWithADot_dotIsLight, false)
        setDotState(isLight, false)
    }

    fun getText() = view.findViewById<TextView>(R.id.little_tittle_with_a_dot_text).text.toString()

    fun setText(arg: String) {
        view.findViewById<TextView>(R.id.little_tittle_with_a_dot_text).text = arg
    }

    fun canChangeByUser(arg: Boolean) {
        canChange = arg
    }

    fun setOnDotChangeListener(listener: (Boolean) -> Unit) {
        mAction = listener
    }

    fun setDotState(isLight: Boolean, isUser: Boolean = true) {
        this.isLight = isLight
        val dotView = view.findViewById<ShadowLayout>(R.id.little_tittle_with_a_dot_dot)
        if (isLight) {
            dotView.setLayoutBackground(dotLightColor)
        }else {
            dotView.setLayoutBackground(dotDarkColor)
        }
        if (::mAction.isInitialized && isUser) {
            mAction(isLight)
        }
    }

    fun isLighting() = isLight

}
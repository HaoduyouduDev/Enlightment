package com.michaelzhan.enlightenment.ui.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import com.michaelzhan.enlightenment.R


class CustomButton: androidx.appcompat.widget.AppCompatButton {

    private lateinit var ta: TypedArray
    private val radius: Float by lazy { ta.getDimension(R.styleable.CustomButton_radius, 8f) };
    private val tag = "CustomButton"
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        ta = context.obtainStyledAttributes(attrs, R.styleable.CustomButton)
    }

    override fun draw(canvas: Canvas) {
        val saveCount = canvas.save()
        canvas.clipPath(Path().apply {addRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), radius, radius, Path.Direction.CW)})
        super.draw(canvas)
        canvas.restoreToCount(saveCount)
    }
}
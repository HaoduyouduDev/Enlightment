package com.michaelzhan.enlightenment.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import androidx.annotation.RequiresApi
import com.michaelzhan.enlightenment.R
import kotlin.math.max

@SuppressLint("SetTextI18n")
class LittleTitle : androidx.appcompat.widget.AppCompatTextView{
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        if (typeface == Typeface.DEFAULT) setTypeface(null,Typeface.BOLD)
        setTextColor(getColor(R.color.title))
        text = "$text "
        textSize = 22f
    }

    override fun draw(canvas: Canvas?) {
        val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint.style = Paint.Style.FILL
        mPaint.color = getColor(R.color.mainColor)
        val p1 = PointF(0f,(height/8f)*5f);val p2 = PointF(width.toFloat(), (height/10f)*9.8f)
        val radius = (p2.y-p1.y)/2f
        canvas?.drawRoundRect(p1.x, p1.y, p2.x, p2.y,
            radius, radius, mPaint)
        super.draw(canvas)
    }

    private fun getColor(colorId: Int): Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        context.getColor(colorId)
    }else {
        context.resources.getColor(colorId)
    }

//    @RequiresApi(Build.VERSION_CODES.Q)
//    private fun getTextBounds() : Rect {
//        val paint = Paint()
//        paint.typeface = typeface
//        paint.textSize = textSize
//        val rect = Rect()
//        paint.getTextBounds(text, 0, text.length, rect)
//        return rect
//    }

}
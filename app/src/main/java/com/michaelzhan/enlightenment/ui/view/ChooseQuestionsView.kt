package com.michaelzhan.enlightenment.ui.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Scroller
import com.michaelzhan.enlightenment.R
import com.michaelzhan.enlightenment.ui.getBitmapFromVectorDrawable


class ChooseQuestionsView(context: Context, attrs: AttributeSet): View(context, attrs) {
    private val tag = "ChooseQuestionsView"
    private var mBitmap: Bitmap? = null
    private var mObj = ArrayList<QuestionBox>()
    private val mPaint = Paint()
    private val mStrokePaint = Paint()
    private val mFillPaint = Paint()
    private val mTipsPaint = Paint()
    private val mScroller = Scroller(context)
    private val deleteButtonSize = 50
    private val resizeButtonSize = 60

    private lateinit var mListener: (ArrayList<QuestionBox>) -> Unit
    private lateinit var mListener2: () -> Unit

    private var mDownX = 0f
    private var mDonwY = 0f
    private var move_x = 0f
    private var move_y = 0f
    private var finalX = 0
    private var finalY = 0

    private var isDragging = false
    private var isResizing = false
    private var isCreating = false
    private var isShowingTips = false
    private var lastDown: Long = 0L
    private var minSize = 100

    private val roundPx = 15f

    private var sw = 1f
    private val focusBoxColor = Color.parseColor("#1A004895")
    private val defaultBoxColor = Color.parseColor("#0D008EFF")
    private val strokeColor = Color.parseColor("#FF7A7A")

    init {
        mStrokePaint.strokeWidth = 6f
        mStrokePaint.style = Paint.Style.STROKE
        mStrokePaint.color = strokeColor
        mTipsPaint.alpha = 200
    }

    fun setImage(bitmap: Bitmap) {
        mBitmap = bitmap
        sw = width.toFloat()/mBitmap!!.width.toFloat()
        Log.d(tag, "sw = $sw")
        Log.d(tag, "width = $width")
        invalidate()
    }

    fun addObj(obj: QuestionBox? = null) {
        if (obj != null) {
            mObj.add(obj.apply {
                topLeft.x *= sw
                topLeft.y *= sw

                bottomEnd.x *= sw
                bottomEnd.y *= sw
            })
        }else {
            cleanAllFocus()
            isShowingTips = true
            isCreating = true
        }
        invalidate()
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        if (canvas != null) {
            if (mBitmap != null) {
                canvas.drawBitmap(
                    mBitmap!!,
                    Rect(0, 0, mBitmap!!.width, mBitmap!!.height),
                    Rect(0, 0, (mBitmap!!.width*sw).toInt(), (mBitmap!!.height*sw).toInt()),
                    mPaint)
                for (i in mObj) {
                    canvas.drawRoundRect(
                        RectF(
                            i.topLeft.x, i.topLeft.y,
                            i.bottomEnd.x, i.bottomEnd.y)
                        , roundPx, roundPx,mStrokePaint)

                    canvas.drawRoundRect(
                        RectF(
                            (i.topLeft.x), (i.topLeft.y),
                            (i.bottomEnd.x), (i.bottomEnd.y))
                        , roundPx, roundPx, mFillPaint.apply {
                            color = if (i.beFocus) {
                                focusBoxColor
                            }else {
                                defaultBoxColor
                            }
                        })

                    if (i.beFocus && !isDragging && !isResizing) {
                        getBitmapFromVectorDrawable(R.drawable.ic_vector_drawable_choose_question_del).let {
                            canvas.drawBitmap(it,
                                Rect(
                                    0, 0, it.width, it.height
                                )
                                , Rect(
                                    (i.bottomEnd.x - deleteButtonSize/2).toInt(), (i.topLeft.y - deleteButtonSize/2).toInt(),
                                    (i.bottomEnd.x + deleteButtonSize/2).toInt(), (i.topLeft.y + deleteButtonSize/2).toInt())
                                , Paint()
                            )
                        }
                    }
                    if (i.beFocus && !isResizing) {
                        getBitmapFromVectorDrawable(R.drawable.ic_vector_drawable_choose_question_resize).let {
                            canvas.drawBitmap(it,
                                Rect(
                                    0, 0, it.width, it.height
                                )
                                , Rect(
                                    (i.bottomEnd.x - resizeButtonSize/2).toInt(), (i.bottomEnd.y - resizeButtonSize/2).toInt(),
                                    (i.bottomEnd.x + resizeButtonSize/2).toInt(), (i.bottomEnd.y + resizeButtonSize/2).toInt()
                                ), Paint()
                            )
                        }
                    }
                    // topx topy bottomx bottomy
                }
                if (isShowingTips) {
                    canvas.drawRect(
                        Rect(0, 0, width, height + mScroller.currY),
                        Paint().apply {
                            color = Color.parseColor("#34000000")
                        }
                    )
                    getBitmapFromVectorDrawable(R.drawable.ic_vector_drawable_choose_question_drag_tip).let {
                        val mPaddingWidth = width/5*2f
                        val centerPos = PointF(width/2f,height/2f)
                        val mSw = ((width.toFloat() - mPaddingWidth)/2f) / it.width
                        canvas.drawBitmap(it,
                            Rect(
                                0, 0, it.width, it.height
                            )
                            , Rect(
                                (centerPos.x - (it.width * mSw)/2).toInt(),
                                (centerPos.y - (it.height * mSw)/2 + mScroller.currY).toInt(),
                                (centerPos.x + (it.width * mSw)/2).toInt(),
                                (centerPos.y + (it.height * mSw)/2 + mScroller.currY).toInt()
                                )
                            , mTipsPaint
                        )
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                //标志着第一个手指按下
                mDownX = x //获取按下时x坐标值
                mDonwY = y //获取按下时y坐标值

                lastDown = System.currentTimeMillis()

                if (!isDragging) {
                    val mFocus = getFocus()
                    if (mFocus != null) {
                        if (posInResizeButton(mFocus, x, y + mScroller.currY)) {
                            isResizing = true
                            setFocus(mFocus)
                        } else if (!posInBox(mFocus, x, y + mScroller.currY) && !posInDeleteButton(mFocus, x, y + mScroller.currY)) {
                            cleanFocus(mFocus)
                            if (::mListener.isInitialized) {
                                mListener(exportObjs())
                            }
                        }
                    }
                }

                if (!isDragging && !isResizing) {
                    for (i in mObj) {
                        if (posInBox(i, x, y+mScroller.currY) && !isCreating) {
                            setFocus(i)
                            i.controlPos.x = x - i.topLeft.x
                            i.controlPos.y = (y + mScroller.currY) - i.topLeft.y
                            isDragging = true
                        }

                    }
                }

                if (isCreating) {
                    mObj.add(
                        QuestionBox().apply {
                            topLeft.x = x
                            topLeft.y = y + mScroller.currY
                            beFocus = true
                        }
                    )
                }

                isShowingTips = false

                invalidate()
            }

            MotionEvent.ACTION_MOVE -> {
                if (mBitmap != null && !isDragging && !isResizing && !isCreating) {
                    if (mBitmap!!.height*sw > height) {
                        //按住一点手指开始移动
//                move_x = mDownX - x //计算当前已经移动的x轴方向的距离
                        move_y = mDonwY - y //计算当前已经移动的y轴方向的距离
                        if (mScroller.finalY < 0) {
                            if (move_y > 0) {
                                mScroller.startScroll(finalX, finalY, 0, move_y.toInt(), 0)
                            }
                        }else if (mBitmap!!.height*sw - height < mScroller.finalY) {
                            if (move_y < 0) {
                                mScroller.startScroll(finalX, finalY, 0, move_y.toInt(), 0)
                            }
                        }else {
                            mScroller.startScroll(finalX, finalY, 0, move_y.toInt(), 0)
                        }
//                        if (!(mScroller.finalY < 0 || mBitmap!!.height - height < mScroller.finalY)) {
//                            //开始滚动动画
//                            //第一个参数：x轴开始位置
//                            //第二个参数：y轴开始位置
//                            //第三个参数：x轴偏移量
//                            //第四个参数：y轴偏移量
//                            mScroller.startScroll(finalX, finalY, 0, move_y.toInt(), 0)
//                        }
//                        if (mScroller.finalY < 0) {
//                            finalY = 0
//                        }else if (mBitmap!!.height - height < mScroller.finalY) {
//                            finalY = mBitmap!!.height - height
//                        }
                        invalidate() //目的是重绘view，是的执行computeScroll方法
                    }
                }else if (isDragging) {
                    val focus = getFocus()
                    if (y >= height/6*5 && !(mBitmap!!.height*sw - height < mScroller.currY - 20)) {
                        mScroller.startScroll(mScroller.currX, mScroller.currY, 0, 20, 0)
                    }else if (y <= height/6*1 && !(mScroller.currY - 20 < 0 || mScroller.currY < 0)) {
                        mScroller.startScroll(mScroller.currX, mScroller.currY, 0, -20, 0)
                    }
                    if (focus != null) {
                        val mWidth = focus.bottomEnd.x - focus.topLeft.x
                        val mHeight = focus.bottomEnd.y - focus.topLeft.y

                        focus.topLeft.x = x - focus.controlPos.x
                        focus.topLeft.y = (y + mScroller.currY) - focus.controlPos.y

                        focus.bottomEnd.x = x + (mWidth - focus.controlPos.x)
                        focus.bottomEnd.y = (y + mScroller.currY) + (mHeight - focus.controlPos.y)
                    }
                    invalidate()
                }else if (isResizing || isCreating) {
                    val focus = getFocus()
                    if (y >= height/6*5 && !(mBitmap!!.height*sw - height < mScroller.currY - 20)) {
                        mScroller.startScroll(mScroller.currX, mScroller.currY, 0, 20, 0)
                    }else if (y <= height/6*1 && !(mScroller.currY - 20 < 0 || mScroller.currY < 0)) {
                        mScroller.startScroll(mScroller.currX, mScroller.currY, 0, -20, 0)
                    }
                    if (focus != null) {
                        focus.bottomEnd.x = x
                        focus.bottomEnd.y = y + mScroller.currY
                        if (!isCreating) {
                            if (focus.bottomEnd.x - focus.topLeft.x < minSize) {
                                focus.bottomEnd.x = focus.topLeft.x + minSize
                            }
                            if (focus.bottomEnd.y - focus.topLeft.y < minSize) {
                                focus.bottomEnd.y = focus.topLeft.y + minSize
                            }
                        }
                        invalidate()
                    }
                }


                for (i in mObj) {
                    Log.d(tag, "obj x=${i.topLeft.x}, y=${i.topLeft.y}")
                }
            }

            MotionEvent.ACTION_UP -> {
                finalX = mScroller.finalX
                finalY = mScroller.finalY

                val mFocus = getFocus()
                if (System.currentTimeMillis() - lastDown < 500L) {
                    if (mFocus != null && !isCreating) {
                        if (posInDeleteButton(mFocus, x, y + mScroller.currY)) {
                            mObj.remove(mFocus)
                        }
                    }
                }

                if (isCreating) {
                    if (mFocus != null) {
                        if (mFocus.bottomEnd.x - mFocus.topLeft.x < minSize ||
                                mFocus.bottomEnd.y - mFocus.topLeft.y < minSize) {
                            mObj.remove(mFocus)
                        }else {
                            if (::mListener.isInitialized) {
                                mListener(exportObjs())
                            }
                        }
                    }
                    if (::mListener2.isInitialized) {
                        mListener2()
                    }
                }

                if (isDragging || isResizing) {
                    if (::mListener.isInitialized) {
                        mListener(exportObjs())
                    }
                }


                isCreating = false
                isDragging = false
                isResizing = false

                invalidate()
            }
        }
        return true
    }

    override fun computeScroll() {
        super.computeScroll()
        if (mScroller.computeScrollOffset()) { //判断滚动是否完成，true说明滚动尚未完成，false说明滚动已经完成
            scrollTo(mScroller.currX, mScroller.currY) //将view直接移动到当前滚动的位置
            invalidate() //触发view重绘
        }
    }

    private fun posInBox(obj: QuestionBox, x: Float, y: Float) = obj.topLeft.x <= x && obj.topLeft.y <= y && obj.bottomEnd.x >= x && obj.bottomEnd.y >= y
    private fun posInDeleteButton(obj: QuestionBox, x: Float, y: Float) = (obj.bottomEnd.x - deleteButtonSize/2) <= x && (obj.topLeft.y - deleteButtonSize/2) <= y && (obj.bottomEnd.x + deleteButtonSize/2) >= x && (obj.topLeft.y + deleteButtonSize/2) >= y
    private fun posInResizeButton(obj: QuestionBox, x: Float, y: Float) = (obj.bottomEnd.x - resizeButtonSize/2) <= x && (obj.bottomEnd.y - resizeButtonSize/2) <= y && (obj.bottomEnd.x + resizeButtonSize/2) >= x && (obj.bottomEnd.y + resizeButtonSize/2) >= y
    private fun exportObjs(): ArrayList<QuestionBox>{
        val mNewList = ArrayList<QuestionBox>()
        for (i in mObj) {
            val newObj = QuestionBox().apply {
                topLeft.x = i.topLeft.x / sw
                topLeft.y = i.topLeft.y / sw
                bottomEnd.x = i.bottomEnd.x / sw
                bottomEnd.y = i.bottomEnd.y / sw
                beFocus = i.beFocus
            }
            mNewList.add(newObj)
            Log.d(tag + "QB export", newObj.toString())
        }
        return mNewList
    }
    private fun getFocus(): QuestionBox? {
        var mFocus: QuestionBox? = null
        for (i in mObj) {
            if (i.beFocus) {
                mFocus = i
            }
        }
        return mFocus
    }
    private fun cleanFocus(obj: QuestionBox) {
        obj.beFocus = false
    }

    private fun cleanAllFocus() {
        for (i in mObj) {
            i.beFocus = false
        }
    }

    private fun setFocus(obj: QuestionBox) {
        cleanAllFocus()
        obj.beFocus = true
    }

    fun isCreating() = isCreating

    fun cancelCreating() {
        isCreating = false
        isShowingTips = false
        invalidate()
    }

    fun setBoxChangeListener(func: (ArrayList<QuestionBox>) -> Unit) {
        mListener = func
        mListener(exportObjs())
    }

    fun setCreateCompleteListener(func: () -> Unit) {
        mListener2 = func
    }

    class QuestionBox {
        val topLeft = PointF()
        val bottomEnd = PointF()
        val controlPos = PointF()
        var beFocus = false
        override fun toString(): String {
            return "$topLeft and $bottomEnd"
        }
    }
}
package com.michaelzhan.enlightenment.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.FrameLayout
import android.widget.ScrollView
import androidx.core.animation.addListener
import androidx.core.animation.doOnEnd
import com.michaelzhan.enlightenment.EnlightenmentApplication

// Thanks for jianshu @因为我的心 ! (code)

class HiddenAnimUtils private constructor(
    private val hideView: View,
    private val viewHeight: Int,
    private val scrollView: ScrollView?
) {
//    private val mHeight: Int
    private var animation: RotateAnimation? = null

    init {
    }

    fun toggle(arg: Boolean) {
        startAnimation()
        if (arg) {
            openAnim(hideView) //布局铺开
        }else {
            closeAnimate(hideView) //布局隐藏
        }

//        if (View.VISIBLE == hideView.visibility) {
//            closeAnimate(hideView) //布局隐藏
//        } else {
//            openAnim(hideView) //布局铺开
//        }
    }

    private fun startAnimation() {
        animation = if (View.VISIBLE == hideView.visibility) {
            RotateAnimation(
                180f,
                0f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            )
        } else {
            RotateAnimation(
                0f,
                180f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            )
        }
        animation!!.duration = 30
        animation!!.interpolator = LinearInterpolator()
        animation!!.repeatMode = Animation.REVERSE
        animation!!.fillAfter = true
    }

    private fun openAnim(v: View) {
        v.visibility = View.VISIBLE
        val animator = createDropAnimator(v, 0, viewHeight)
        animator.start()
    }

    private fun closeAnimate(view: View) {
        val origHeight = view.height
        val animator = createDropAnimator(view, origHeight, 0)
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                view.visibility = View.GONE
            }
        })
        animator.start()
    }

    private fun createDropAnimator(v: View, start: Int, end: Int): ValueAnimator {
        val animator = ValueAnimator.ofInt(start, end)
        animator.addUpdateListener { arg0 ->
            val value = arg0.animatedValue as Int
            val layoutParams = v.layoutParams
            layoutParams.height = value
            v.layoutParams = layoutParams

            scrollView?.post {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN)
            }
        }
        animator.doOnEnd {
            val layoutParams = v.layoutParams
            layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT
            v.layoutParams = layoutParams
        }
        return animator
    }

    companion object {
        fun newInstance(
            hideView: View,
            height: Int,
            scrollView: ScrollView? = null
        ): HiddenAnimUtils {
            return HiddenAnimUtils(hideView, height, scrollView)
        }
    }
}

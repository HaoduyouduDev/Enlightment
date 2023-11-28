package com.michaelzhan.enlightenment

import android.content.Context
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import com.michaelzhan.enlightenment.EnlightenmentApplication.Companion.context
import kotlin.math.sqrt


fun px2Dp(px: Int): Float {
    val displayMetrics: DisplayMetrics = EnlightenmentApplication.context.resources.displayMetrics
    return (px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
}

fun dp2Px(dp: Int): Float {
    val displayMetrics: DisplayMetrics = EnlightenmentApplication.context.resources.displayMetrics
    return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
}

fun isPad(): Boolean {
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display: Display = wm.defaultDisplay
    val dm = DisplayMetrics()
    display.getMetrics(dm)
    val x = Math.pow((dm.widthPixels / dm.xdpi).toDouble(), 2.0)
    val y = Math.pow((dm.heightPixels / dm.ydpi).toDouble(), 2.0)
    val screenInches = sqrt(x + y) // 屏幕尺寸
    return screenInches >= 7.0
}

fun test() {

}
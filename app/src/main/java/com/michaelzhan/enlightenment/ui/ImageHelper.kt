package com.michaelzhan.enlightenment.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.util.Log
import androidx.core.content.ContextCompat
import com.michaelzhan.enlightenment.EnlightenmentApplication
import java.io.File
import java.io.FileInputStream


fun path2BitmapFromPath(path: String): Bitmap {
    Log.d("path2BitmapFromPath", path)
    val dest = File(path)
    val fis = FileInputStream(dest)
    return BitmapFactory.decodeStream(fis)
}

fun getBitmapFromVectorDrawable(drawableId: Int): Bitmap {
    val drawable = ContextCompat.getDrawable(EnlightenmentApplication.context, drawableId)
    val bitmap = Bitmap.createBitmap(
        drawable!!.intrinsicWidth,
        drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

fun cropBitmap(bitmap: Bitmap, startx: Int, starty: Int, width: Int, height: Int) =
    Bitmap.createBitmap(bitmap, startx, starty, width, height)
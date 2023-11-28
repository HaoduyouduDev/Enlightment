package com.michaelzhan.enlightenment.ui

import android.os.Handler
import android.widget.Toast
import com.michaelzhan.enlightenment.EnlightenmentApplication

fun CharSequence.makeToast(howLong: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(EnlightenmentApplication.context, this, howLong).show()
}


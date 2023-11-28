package com.michaelzhan.enlightenment.ui.showImage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.michaelzhan.enlightenment.databinding.ActivityShowImageBinding
import com.michaelzhan.enlightenment.ui.path2BitmapFromPath

class ShowImage : AppCompatActivity() {

    private val binding by lazy { ActivityShowImageBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        intent.getStringExtra("imagePath")?.let {
            path2BitmapFromPath(it).let { it2 ->
                binding.photoView.setImageBitmap(
                    it2
                )
            }
        }

    }
}
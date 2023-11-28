package com.michaelzhan.enlightenment.ui.reviewQuestion

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.github.chengang.library.TickView
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.DialogLifecycleCallback
import com.kongzue.dialogx.interfaces.OnBindView
import com.michaelzhan.enlightenment.EnlightenmentApplication
import com.michaelzhan.enlightenment.R
import com.michaelzhan.enlightenment.databinding.ActivityReviewQuestionBinding


class ReviewQuestion : FragmentActivity() {

    private val binding by lazy { ActivityReviewQuestionBinding.inflate(layoutInflater) }
    private val viewModel by lazy { ViewModelProviders.of(this).get(ReviewQuestionViewModel::class.java) }

    private val tag = "ReviewQuestion"

    private var mAnswer: TittlesAdapter.Tittle? = null

    private var questionsList: LongArray? = null
    private var index = 0

    private lateinit var viewPager: ViewPager2

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        DialogX.init(this)

        intent.getLongArrayExtra("questionsIdList")?.let {
            viewModel.questionsIdList = it
            for (i in it) {
                Log.d(tag, "id = $i")
            }
            Log.d(tag, "size = ${it.size}")
        }

        intent.getIntExtra("index", 0).let {
            index = it
        }

        viewPager = binding.mViewPage

        val pagerAdapter = ScreenSlidePagerAdapter(this)
        viewPager.adapter = pagerAdapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                getSharedPreferences(EnlightenmentApplication.userReviewTempName, Context.MODE_PRIVATE).edit {
                    putInt(
                        "lastReviewPos", position
                    )
                }
            }
        })

        intent.getIntExtra("lastReviewPos", -1).let {
            if (it != -1) {
                PopTip.show("从上次的位置开始", "Cancel").showLong().setOnButtonClickListener { dialog, v ->
                    viewPager.currentItem = 0
                    false
                }
                viewPager.post {
                    viewPager.currentItem = it
                }
            }
        }

        binding.backButton.setOnClickListener {
//            if (viewPager.currentItem == 0) {
//                finish()
//            }else {
//                viewPager.currentItem = viewPager.currentItem - 1
//            }
            finish()
        }

        binding.nextButton.setOnClickListener {
            if (viewPager.currentItem == ((viewModel.questionsIdList?.size) ?: (viewPager.currentItem + 1)) - 1) {
                ok()
            }else {
                viewPager.currentItem = viewPager.currentItem + 1
            }
        }
    }

    override fun onBackPressed() {
        if (viewPager.currentItem == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else {
            // Otherwise, select the previous step.
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }

    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = (viewModel.questionsIdList?.size ?: 1)

        override fun createFragment(position: Int): Fragment = ReviewFragment().apply {
            setPos(position)

        }
    }

    fun ok() {
        getSharedPreferences(EnlightenmentApplication.userReviewTempName, Context.MODE_PRIVATE).edit {
            putBoolean("reviewCompleted", true)
        }
        CustomDialog.show(object : OnBindView<CustomDialog?>(R.layout.dialog_main_page_encourage) {
            override fun onBind(dialog: CustomDialog?, v: View?) {
                if (v != null) {
                    v.findViewById<TickView>(R.id.dialog_main_page_encourage_m_tick_view)?.let {
                        it.post {
                            it.toggle()
                        }
                    }
                    v.findViewById<TextView>(R.id.dialog_main_page_encourage_ok).setOnClickListener {
                        dialog?.dismiss()
                    }
                }
            }
        }).dialogLifecycleCallback = object : DialogLifecycleCallback<CustomDialog>() {
            override fun onDismiss(dialog: CustomDialog?) {
                super.onDismiss(dialog)
                finish()
            }
        }
    }
}
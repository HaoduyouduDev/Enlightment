package com.michaelzhan.enlightenment.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogx.dialogs.MessageDialog
import com.kongzue.dialogx.dialogs.PopMenu
import com.kongzue.dialogx.interfaces.OnIconChangeCallBack
import com.kongzue.dialogx.interfaces.OnMenuItemClickListener
import com.michaelzhan.enlightenment.EnlightenmentApplication
import com.michaelzhan.enlightenment.FileUtils
import com.michaelzhan.enlightenment.R
import com.michaelzhan.enlightenment.databinding.ActivityMainPageBinding
import com.michaelzhan.enlightenment.getImagesPath
import com.michaelzhan.enlightenment.logic.Repository
import com.michaelzhan.enlightenment.logic.model.A_DAY
import com.michaelzhan.enlightenment.ui.chooseSubject.ChooseSubject
import com.michaelzhan.enlightenment.ui.chooseSubject.ChooseSubjectMode
import com.michaelzhan.enlightenment.ui.makeToast
import com.michaelzhan.enlightenment.ui.path2BitmapFromPath
import com.michaelzhan.enlightenment.ui.reviewQuestion.ReviewQuestion
import com.michaelzhan.enlightenment.ui.showReviewTime.ShowReviewTime
import java.util.*


class MainPage : AppCompatActivity() {

    private val viewModel by lazy { ViewModelProviders.of(this).get(MainPageViewModel::class.java) }
    private val binding by lazy { ActivityMainPageBinding.inflate(layoutInflater) }
    private lateinit var mBooksAdapter : BooksAdapter

    private val calendar: Calendar = Calendar.getInstance()

    private val year: Int get() = calendar.get(Calendar.YEAR)
    private val month: Int get() = calendar.get(Calendar.MONTH) + 1
    private val day: Int get() = calendar.get(Calendar.DAY_OF_MONTH)

    private val tag = "MainPage"

    private val reviewQuestionsId = ArrayList<Long>()
    private var lastReviewPos = -1

    private var isReviewCompleted = false

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        DialogX.init(this)

        viewModel.spanCount = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !isInMultiWindowMode) {
            when (resources.configuration.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> 5
                Configuration.ORIENTATION_PORTRAIT -> 4
                else -> 4
            }
        }else{
            3
        }

        binding.booksList.isNestedScrollingEnabled = false

        binding.writeErrorBook.setOnClickListener {
            startActivity(Intent(this, ChooseSubject::class.java))
        }

        verifyStoragePermissions(this)

        initErrorBooks()

        viewModel.errorBooksLiveData.observe(this, Observer { result ->
            Log.e(tag,"observe room data from error book")
            Log.e(tag, result.toString())
            if (result != null) {
                viewModel.bookList.clear()
                viewModel.bookList.addAll(result.filter { book -> book.collect })
                mBooksAdapter.notifyDataSetChanged()

                if (viewModel.bookList.isEmpty()) binding.noDataMask.visibility = View.VISIBLE
                else binding.noDataMask.visibility = View.GONE
            } else {
                resources.getText(R.string.tips_cannot_load_errorBooks).makeToast()
            }
        })

        viewModel.reviewTimeLiveData.observe(this) { result ->

            viewModel.reviewTime.clear()
            viewModel.reviewTime.addAll(result)
            reviewQuestionsId.clear()

            val mC = Calendar.getInstance(Locale.CHINA)
            mC.set(year, month-1, day, 0, 0, 0)
            val startTime = mC.timeInMillis
            val endTime = mC.timeInMillis + A_DAY

            Log.d(tag, "start time = $startTime")
            Log.d(tag, "end time = $endTime")

            result.forEach { root ->
                root.reviewTime.forEach {
                    Log.d(tag, "The review time item is $it")
                    if (it in startTime..endTime) {
                        reviewQuestionsId.add(root.questionId)
                    }
                }
            }

            if (reviewQuestionsId.isNotEmpty()) {
                refreshText()
                if (!isReviewCompleted) {
                    binding.noDataImage.setImageResource(R.drawable.ic_vector_drawable_todo_line)
//                    binding.reviewQuestionButton.visibility = View.VISIBLE
                    binding.reviewCardView.isClickable = true
                    binding.reviewBackground.alpha = 0.3f
                    Repository.getQuestionFromId(reviewQuestionsId[0])?.let {
                        val mHtmlSb = StringBuilder()
                        it.sub_titles.forEach { item ->
                            mHtmlSb.append(FileUtils.readInternal(item.second))
                        }
                        val mImagesPath = getImagesPath(mHtmlSb.toString())
                        if (mImagesPath.isNotEmpty()) {
                            path2BitmapFromPath(mImagesPath[0])?.let { it2 ->
                                binding.reviewBackground.setImageBitmap(it2)
                            }
                        }
                    }
                }else {
                    refreshText2()
                    binding.noDataImage.setImageResource(R.drawable.ic_vector_drawable_ok)
//                    binding.reviewQuestionButton.visibility = View.INVISIBLE
                    binding.reviewCardView.isClickable = false
                    binding.reviewBackground.setImageResource(0)
                }
            }else {
                binding.noDataImage.setImageResource(R.drawable.ic_vector_drawable_nodata)
                binding.reviewTips.text = "今天没有复习内容"
//                binding.reviewQuestionButton.visibility = View.INVISIBLE
                binding.reviewCardView.isClickable = false
                binding.reviewBackground.setImageResource(0)
            }
        }

        binding.editSubjectButton.setOnClickListener {
            startActivity(Intent(this, ChooseSubject::class.java).apply {
                putExtra("mode", ChooseSubjectMode.EDIT_SUBJECTS)
            })
        }

//        binding.reviewQuestionButton.setOnClickListener {
//            val mLongArray = LongArray(reviewQuestionsId.size)
//            for (i in 0 until reviewQuestionsId.size) {
//                mLongArray[i] = reviewQuestionsId[i]
//            }
//            startActivity(Intent(this, ReviewQuestion::class.java).apply {
//                putExtra("questionsIdList", mLongArray)
//                putExtra("lastReviewPos", lastReviewPos)
//            })
//
//        }

        binding.reviewCardView.setOnClickListener {
            val mLongArray = LongArray(reviewQuestionsId.size)
            for (i in 0 until reviewQuestionsId.size) {
                mLongArray[i] = reviewQuestionsId[i]
            }
            startActivity(Intent(this, ReviewQuestion::class.java).apply {
                putExtra("questionsIdList", mLongArray)
                putExtra("lastReviewPos", lastReviewPos)
            })
        }

        binding.reviewTimeSetting.setOnClickListener {
            startActivity(Intent(this, ShowReviewTime::class.java))
        }

        refreshReviewContent()

        binding.menu.setOnClickListener {
            PopMenu.show(binding.menu, arrayOf("关于软件", "打赏"))
                .setOverlayBaseView(true)
                .setWidth(600)
                .setOnIconChangeCallBack(
                    object : OnIconChangeCallBack<PopMenu>(true) {
                        override fun getIcon(dialog: PopMenu?, index: Int, menuText: String?): Int {
                            return when (index) {
                                0 -> R.drawable.id_vector_drawable_about
                                1 -> R.drawable.ic_vector_drawable_reword
                                else -> 0
                            }
                        }
                    }
                ).setOnMenuItemClickListener(object : OnMenuItemClickListener<PopMenu> {
                    override fun onClick(dialog: PopMenu?, text: CharSequence?, index: Int): Boolean {
                        when (index) {
                            0 -> {
                                MessageDialog.show("About",
                                    "App Name: Enlightenment\n" +
                                            "Version: releases 1.0\n" +
                                            "Author: Michael Zhan\n" +
                                            "This version is for preview.\n\n" +
                                            "All copyright by author.")
                            }
                            1 -> {
//                                PopTip.show("Preview版本不支持爱发电（颠）")
                                MessageDialog.show("Tips", "即将跳转到外部网页", "OK", "Cancel").setOkButton { _, _ ->
                                    val uri = Uri.parse(EnlightenmentApplication.faDianUrl)
                                    val intent = Intent()
                                    intent.action = "android.intent.action.VIEW"
                                    intent.data = uri
                                    startActivity(intent)
                                    false
                                }
                            }
                        }
                        return false
                    }
                });
        }
    }

    private fun initErrorBooks() {
        mBooksAdapter = BooksAdapter(this, viewModel.bookList)
        binding.booksList.apply {
            layoutManager = GridLayoutManager(this@MainPage, viewModel.spanCount)
            adapter = mBooksAdapter
        }
    }

    private fun verifyStoragePermissions(activity: Activity) {
        val permissionWrite = ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (permissionWrite != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                activity, arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ,1
            )
        }
    }

    private fun refreshReviewContent() {
        val prefs = getSharedPreferences(EnlightenmentApplication.userReviewTempName, Context.MODE_PRIVATE)
        val mY = prefs.getInt("y", -1)
        val mM = prefs.getInt("m", -1)
        val mD = prefs.getInt("d", -1)
        if (mY == year && mM == month && mD == day) {
            lastReviewPos = prefs.getInt("lastReviewPos", -1)
            isReviewCompleted = prefs.getBoolean("reviewCompleted", false)
            Log.d(tag, "lastReviewPos = $lastReviewPos")
        }else {
            prefs.edit {
                putInt("y", year)
                putInt("m", month)
                putInt("d", day)
                putBoolean("reviewCompleted", false)
                putInt("lastReviewPos", -1)
            }
        }
    }

    private fun refreshText() {
        var temp = reviewQuestionsId.size
        if (lastReviewPos != -1) temp -= lastReviewPos
        if (temp == 0) temp = 1
        binding.reviewTips.text = "您有未复习的题目${temp}道"
    }

    private fun refreshText2() {
        binding.reviewTips.text = "你完成了所有复习任务"
    }

    override fun onResume() {
        super.onResume()
        refreshReviewContent()
        if (reviewQuestionsId.isNotEmpty() && !isReviewCompleted) {
            refreshText()
        }else if(isReviewCompleted) {
            refreshText2()
            binding.noDataImage.setImageResource(R.drawable.ic_vector_drawable_ok)
//            binding.reviewQuestionButton.visibility = View.INVISIBLE
            binding.reviewCardView.isClickable = false
            binding.reviewBackground.setBackgroundResource(0)
        }
    }


}
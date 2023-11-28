package com.michaelzhan.enlightenment.ui.editAQuestion

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.RadioButton
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogx.dialogs.*
import com.michaelzhan.enlightenment.*
import com.michaelzhan.enlightenment.databinding.ActivityEditAQuestionBinding
import com.michaelzhan.enlightenment.ui.makeToast
import com.michaelzhan.enlightenment.logic.model.ReviewTime
import com.michaelzhan.enlightenment.logic.model.ReviewType
import com.michaelzhan.enlightenment.ui.HiddenAnimUtils
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet


class EditAQuestion : AppCompatActivity() {
    private val tag = "EditAQuestion"
    private val viewModel by lazy { ViewModelProviders.of(this).get(EditAQuestionViewModel::class.java) }
    private val binding by lazy { ActivityEditAQuestionBinding.inflate(layoutInflater) }
    private lateinit var mTittlesAdapter: TittlesAdapter
    private val tittleTips by lazy {
        if (isPad()) {
//            " (长按编辑)"
            ""
        }else {
//            "\n(长按编辑)"
            ""
        }
    }

    private lateinit var imageUri: Uri
    private var getImgDialog: MessageDialog? = null

    private var cardHeight = 0

    private val calendar: Calendar = Calendar.getInstance()

    private var isFirst = true

    private val takePhoto = 1
    private val fromAlbum = 2

    private var isConfigOk = false

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        DialogX.init(this)
        WaitDialog.dismiss();

        imageUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", File(externalCacheDir, "temp.jpg"));

        binding.addToMemoryCurveCheckBox.isEnabled = false

        binding.tittle.setOnBackClickListener {
            if (!checkContentIsEmpty()) {
                PopTip.show("保存成功")
                finish()
            }
//            if (!checkContentIsEmpty()) {
//                MessageDialog.show("Tips", "确定要退出吗？\n题目将会被自动保存。", "OK", "Cancel").setOkButton { dialog, v ->
//                    finish()
//                    false
//                }
//            }
        }

        binding.tittle.setOnClickListener {
            InputDialog("Tips", "请输入错题标题", "确定", "取消", viewModel.mQuestion?.title ?: "Title")
                .setCancelable(false)
                .setOkButton { baseDialog, v, inputStr ->
                    if (inputStr.isNotEmpty()) {
                        GlobalScope.launch {
                            viewModel.editQuestionTitle(inputStr)
                            runOnUiThread {
                                binding.tittle.setText(inputStr + tittleTips)
                            }
                        }
                    }
                    false
                }
                .show()
        }

        initTittles()

        intent.getLongExtra("questionId", -1L).let {
            GlobalScope.launch {
                if (it != -1L) {
                    viewModel.mQuestion = viewModel.getQuestionFromId(it)
                    Log.d(tag,  "a question = " + viewModel.mQuestion.toString())
                }

                runOnUiThread {
                    if (viewModel.mQuestion != null && viewModel.tittleList.isEmpty()) {
                        Log.d(tag, "a test")
                        for (i in viewModel.mQuestion!!.sub_titles) {
                            val mTittle = TittlesAdapter.Tittle(
                                i.second, i.first, FileUtils.readInternal(i.second).isNotEmpty()
                            )
                            viewModel.tittleList.add(mTittle)
                            Log.d(tag, "add a tittle $mTittle")
                        }
                        mTittlesAdapter.notifyDataSetChanged()
                    }
                    if (viewModel.mQuestion != null) {
                        binding.tittle.setText(viewModel.mQuestion!!.title + tittleTips)

                        binding.proficiencySeekBar.progress = (viewModel.mQuestion!!.proficiency * 100f).toInt()

                        viewModel.getReviewTimeFromQuestionId()?.let {
                            when(it.type) {
                                ReviewType.AUTO -> {
                                    binding.RadioButton1.isChecked = true
                                }
                                ReviewType.DAY -> {
                                    binding.RadioButton2.isChecked = true
                                }
                                ReviewType.WEEK -> {
                                    binding.RadioButton3.isChecked = true
                                }
                                ReviewType.MONTH -> {
                                    binding.RadioButton4.isChecked = true
                                }
                                ReviewType.USER_CUSTOM -> {
                                    binding.RadioButton5.isChecked = true
                                }
                            }
                        }
                        isConfigOk = true
                    }
                }
            }
        }

        binding.RadioButton5.setOnCheckedChangeListener { compoundButton, b ->
            if (compoundButton.isChecked && isConfigOk) {
                binding.RadioGroup1.clearCheck()
                binding.RadioGroup2.clearCheck()
                showDatePickerDialog(0)
            }
        }

        binding.proficiencySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            //当滑块进度改变时
            override fun onProgressChanged(seekBar: SeekBar?, i: Int, b: Boolean) {

            }

            //当开始滑动滑块时
            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            //当结束滑动滑块时
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (viewModel.mQuestion != null) {
                    viewModel.editQuestionProficiency(binding.proficiencySeekBar.progress.toFloat()/100f)
                }
            }
        })

        binding.RadioGroup1.setOnCheckedChangeListener { radioGroup, i ->
            findViewById<RadioButton>(i)?.let {
                if (it.isChecked && viewModel.mQuestion != null) {
                    binding.RadioButton5.isChecked = false
                    binding.RadioGroup2.clearCheck()
                    when (it.id) {
                        binding.RadioButton1.id -> {
                            viewModel.editReviewTime(ReviewType.AUTO)
                        }
                        binding.RadioButton2.id -> {
                            viewModel.editReviewTime(ReviewType.DAY)
                        }
                    }
                }
            }
        }

        binding.RadioGroup2.setOnCheckedChangeListener { radioGroup, i ->
            findViewById<RadioButton>(i)?.let {
                if (it.isChecked && viewModel.mQuestion != null) {
                    binding.RadioButton5.isChecked = false
                    binding.RadioGroup1.clearCheck()
                    when (it.id) {
                        binding.RadioButton3.id -> {
                            viewModel.editReviewTime(ReviewType.WEEK)
                        }
                        binding.RadioButton4.id -> {
                            viewModel.editReviewTime(ReviewType.MONTH)
                        }
                    }
                }
            }
        }

        binding.addToMemoryCurveCheckBox.setOnCheckedChangeListener { compoundButton, b ->
            if (viewModel.mQuestion != null && binding.addToMemoryCurveCheckBox.isEnabled) {
                if (b) {
                    viewModel.addReviewTime(ReviewTime(viewModel.mQuestion!!.id, ArrayList(), ReviewType.AUTO))
                    viewModel.editReviewTime(ReviewType.AUTO)
                    binding.RadioButton1.isChecked = true
                    viewModel.realComplete = getSharedPreferences(EnlightenmentApplication.userReviewTempName, Context.MODE_PRIVATE).getBoolean("reviewCompleted", false)
                    getSharedPreferences(EnlightenmentApplication.userReviewTempName, Context.MODE_PRIVATE).edit {
                        putBoolean("reviewCompleted", false)
                    }
                }else {
                    viewModel.delReviewTime()
                    getSharedPreferences(EnlightenmentApplication.userReviewTempName, Context.MODE_PRIVATE).edit {
                        putBoolean("reviewCompleted", viewModel.realComplete)
                    }
                }
                viewModel.changeMemoryFromQuestion(b)
                HiddenAnimUtils.newInstance(binding.selectReminderTimeCardView, cardHeight, binding.mScrollView).toggle(b)
            }
        }

        binding.memoryDoubt.setOnClickListener {
            PopTip.show("记忆曲线会根据艾宾浩斯遗忘曲线帮助您定时复习。" +
                    "\n如果您打开该选项，您可以看到以下5个选项：" +
                    "\n自动复习、每日复习、每周复习、每月复习、自定义时间复习。" +
                    "\n除了自定义时间复习，选择其他4个选项会在一年内定时让您复习，产生对错题的长期记忆。", "OK").noAutoDismiss()
                .setButton { popTip, v -> //点击“撤回”按钮回调
                    false
                }

        }

        binding.openProficiencyCheckBox.isClickable = false

        binding.OKButton.setOnClickListener {
            if (!checkContentIsEmpty()) {
                finish()
            }
        }


        binding.tittle.post {
            val prefs = getSharedPreferences(EnlightenmentApplication.appConfigName, Context.MODE_PRIVATE)
            val usedSet: Set<String> = HashSet(prefs.getStringSet("guideUseSet", setOf<String>()))
            val typeTitle = "EditAQuestion-Title"
            if (usedSet?.contains(typeTitle) != true) {
                GuideDialog.build().setAlign(CustomDialog.ALIGN.RIGHT_TOP).setAutoUnsafePlacePadding(false).setBaseViewMarginTop(0).setTipImage(R.drawable.bg_tips_of_click_title).show()
                prefs.edit {
                    putStringSet("guideUseSet", (usedSet ?: setOf<String>()) + setOf(typeTitle))
                }
            }
        }
    }

    private fun initTittles() {
        mTittlesAdapter = TittlesAdapter(this, viewModel.tittleList)

        binding.tittleList.apply {
            layoutManager = LinearLayoutManager(context).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            adapter = mTittlesAdapter
        }
        binding.tittleList.isNestedScrollingEnabled = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            takePhoto -> {
                if (resultCode == Activity.RESULT_OK) {
                    cropPhoto(imageUri)
                }
            }

            fromAlbum -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    data.data?.let { uri ->
                        cropPhoto(uri)
                    }
                }
            }

            UCrop.REQUEST_CROP -> {
//                if (intent == null) {
//                    return
//                }
//                val bundle = intent.extras
//                if (bundle != null) {
//                    val image = bundle.getParcelable<Bitmap>("data")
//                    if (image != null) {
//                        "!= null".makeToast()
//                        mTittlesAdapter.insertImage(saveImage(image))
//                        getImgDialog?.dismiss()
//                    }
//                }
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        mTittlesAdapter.insertImage(UCrop.getOutput(data)!!.path!!)
                        getImgDialog?.dismiss()
                    }
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (isFirst) {
            cardHeight = binding.selectReminderTimeCardView.height

            if (viewModel.mQuestion != null) {
                val isAdd = viewModel.mQuestion!!.addToMemory
                binding.addToMemoryCurveCheckBox.isChecked = isAdd
                if (!isAdd) {
                    binding.selectReminderTimeCardView.visibility = View.GONE
                }else {
                    binding.selectReminderTimeCardView.visibility = View.VISIBLE
                }
            }
            binding.addToMemoryCurveCheckBox.isEnabled = true
            isFirst = false
        }
    }

    private fun getBitmapFromUri(uri: Uri) = contentResolver.openFileDescriptor(uri, "r")?.use {
        BitmapFactory.decodeFileDescriptor(it.fileDescriptor)
    }

    private fun showLoadingFloat() {
        WaitDialog.show("Please Wait!")
    }

    private fun closeLoadingFloat() {
        Handler(Looper.getMainLooper()).postDelayed(Runnable { WaitDialog.dismiss() }, 500)
    }

    /**
     * 加粗
     */
    fun setBold(v: View?) {
        mTittlesAdapter.setBold()
    }

    /**
     * 斜体
     */
    fun setItalic(v: View?) {
        mTittlesAdapter.setItalic()
    }

    /**
     * 下划线
     */
    fun setUnderline(v: View?) {
        mTittlesAdapter.setUnderline()
    }

    /**
     * 删除线
     */
    fun setStrikethrough(v: View?) {
        mTittlesAdapter.setStrikethrough()
    }

    /**
     * 序号
     */
    fun setBullet(v: View?) {
        mTittlesAdapter.setBullet()
    }

    /**
     * 引用块
     */
    fun setQuote(v: View?) {
        mTittlesAdapter.setQuote()
    }

    /**
     * 插入图片
     */
    fun insertImg(v: View?) {
        MessageDialog.show("Tips", "请选择题目图片", "拍照", "相册", "取消").setOkButton { baseDialog, v ->
            getImgDialog = baseDialog
            // 相机
            val intent = Intent("android.media.action.IMAGE_CAPTURE")
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(intent, takePhoto)
            true
        }.setCancelButton { dialog, v ->
            getImgDialog = dialog
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, fromAlbum)
            true
        }.setOtherButton { dialog, v ->
            dialog.dismiss()
            false
        }.setCancelable(false)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d(tag, "back")
            if (checkContentIsEmpty()) {
                return true
            }

        }
        return super.dispatchKeyEvent(event)
    }

    private fun checkContentIsEmpty(): Boolean {
        if (viewModel.tittleList.isNotEmpty()) {
            if (FileUtils.readInternal(viewModel.tittleList[0].richTextPath).isEmpty()) {
                PopTip.show("题目正文不能为空", "仍要退出").setOnButtonClickListener { dialog, v ->
                    if (viewModel.mQuestion != null) {
                        FileUtils.writeInternal(viewModel.mQuestion!!.sub_titles[0].second, "你还没有编写题目主干...")
                    }
                    finish()
                    false
                }
                return true
            }
        }
        return false
    }

    private fun cropPhoto(uri: Uri) {
        val options: UCrop.Options = UCrop.Options()
        // 修改标题栏颜色
        options.setToolbarColor(Color.parseColor("#FF9ADEFD"))
        // 修改状态栏颜色
//        options.setStatusBarColor(resources.getColor(R.color.teal_700))
        // 隐藏底部工具
        options.setHideBottomControls(true)
        // 图片格式
        options.setCompressionFormat(Bitmap.CompressFormat.PNG)
        // 设置图片压缩质量
        options.setCompressionQuality(100)
        // 是否让用户调整范围(默认false)，如果开启，可能会造成剪切的图片的长宽比不是设定的
        // 如果不开启，用户不能拖动选框，只能缩放图片
        options.setFreeStyleCropEnabled(true)
        // 设置图片压缩质量
        options.setCompressionQuality(100)
        // 不显示网格线
        options.setShowCropGrid(false)
        // 设置源uri及目标uri
        UCrop.of(
            uri,
            Uri.fromFile(
                File(
                    EnlightenmentApplication.userQuestionsImagesPath + File.separator +
                            System.currentTimeMillis().toString() + ".png"
                )
            )
        ) // 长宽比
            .withOptions(options)
            .start(this)
    }

    private fun showDatePickerDialog(
        themeResId: Int
    ) {
        // 直接创建一个DatePickerDialog对话框实例，并将它显示出来
        val mDialog = DatePickerDialog(
            this@EditAQuestion,
            themeResId,
            null
            ,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH))
        mDialog.setOnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            val mCalendar = Calendar.getInstance()
            mCalendar.set(year, monthOfYear, dayOfMonth, 0, 0)

            if (year <= calendar.get(Calendar.YEAR) && monthOfYear <= calendar.get(Calendar.MONTH) && dayOfMonth < calendar.get(Calendar.DAY_OF_MONTH)){
                PopTip.show("不能设置比现在更晚的提醒时间")
                binding.RadioButton1.isChecked = true
            }else {
                viewModel.editReviewTime(ReviewType.USER_CUSTOM, mCalendar.timeInMillis)
            }
        }

        mDialog.setOnCancelListener {
            binding.RadioButton1.isChecked = true
        }

        mDialog.show()
    }
}
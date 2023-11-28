package com.michaelzhan.enlightenment.ui.editQuestions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogx.dialogs.MessageDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.michaelzhan.enlightenment.EnlightenmentApplication
import com.michaelzhan.enlightenment.FileUtils
import com.michaelzhan.enlightenment.databinding.ActivityEditQuestionsBinding
import com.michaelzhan.enlightenment.logic.model.Question
import com.michaelzhan.enlightenment.logic.model.QuestionSubTitleType
import com.yalantis.ucrop.UCrop
import java.io.File
import kotlin.concurrent.thread


class EditQuestions : AppCompatActivity() {
    private val tag = "EditQuestions"
    private val viewModel by lazy { ViewModelProviders.of(this).get(EditQuestionsViewModel::class.java) }
    private val binding by lazy { ActivityEditQuestionsBinding.inflate(layoutInflater) }
    private lateinit var mQuestionsAdapter: QuestionsAdapter

    private val takePhoto = 1
    private val fromAlbum = 2

    private lateinit var imageUri: Uri
    private var getImgDialog: MessageDialog? = null

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        DialogX.init(this)
        WaitDialog.dismiss();

        intent.getLongArrayExtra("questionsId")?.let {
            viewModel.firstRequestQuestions(it)
        }

        intent?.getLongExtra("subjectId", -1L)?.let {
            if (it != -1L) {
                viewModel.subjectId = it
            }
        }

        initQuestions()

        viewModel.addPos = -1
        viewModel.deletePos = -1

        viewModel.questionsLiveData.observe(this) { questionList ->
            viewModel.questionsList.clear()
            viewModel.questionsList.addAll(questionList)
            Log.d(tag, questionList.toString())
            if (viewModel.deletePos != -1) {
                mQuestionsAdapter.notifyItemRemoved(viewModel.deletePos)
            }else if(viewModel.addPos != -1) {
                mQuestionsAdapter.notifyItemInserted(viewModel.addPos)
            }else {
                mQuestionsAdapter.notifyDataSetChanged()
            }
        }

        binding.title.setOnBackClickListener {
            if (viewModel.questionsList.isNotEmpty()) {
                MessageDialog.show("Tips", "确定要退出吗？\n修改的题目会被自动保存。", "Yes" ,"No").setOkButton { dialog, v ->
                    finish()
                    false
                }.setCancelButton { dialog, v ->
                    false
                }
            }else {
                finish()
            }
        }
    }

    private fun initQuestions() {
        mQuestionsAdapter = QuestionsAdapter(this, viewModel.questionsList)
        binding.questionsList.apply {
            layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.VERTICAL }
            adapter = mQuestionsAdapter
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        mQuestionsAdapter.notifyDataSetChanged()
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
                if (resultCode == Activity.RESULT_OK) {
                    val suchTextPath = EnlightenmentApplication.userQuestionsRichTextPath + File.separator + System.currentTimeMillis().toString()
                    if (data != null) {

                        FileUtils.writeInternal(
                            suchTextPath + "_Content.dt", "<img width=\"100%\" src=\"${UCrop.getOutput(data)!!.path!!}\">"
                        )

                        viewModel.addQuestion(Question(
                            "Tittle",
                            System.currentTimeMillis(),
                            arrayListOf(
                                Pair(QuestionSubTitleType.CONTENT, suchTextPath + "_Content.dt"),
                                Pair(QuestionSubTitleType.WHY, "_Why.dt"),
                                Pair(QuestionSubTitleType.ANSWER, "_Answer.dt")
                            ),
                            1.0f,
                            false,
                            viewModel.subjectId
                        ), viewModel.questionsList.size)

                        getImgDialog?.dismiss()
                    }
                }
            }
        }
    }

    fun getImage() {
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
}
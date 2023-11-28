package com.michaelzhan.enlightenment.ui.getQuestion

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProviders
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogx.dialogs.FullScreenDialog
import com.kongzue.dialogx.dialogs.MessageDialog
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.kongzue.dialogx.interfaces.OnBackPressedListener
import com.kongzue.dialogx.interfaces.OnBindView
import com.michaelzhan.enlightenment.EnlightenmentApplication
import com.michaelzhan.enlightenment.FileUtils
import com.michaelzhan.enlightenment.R
import com.michaelzhan.enlightenment.databinding.ActivityGetQuestionBinding
import com.michaelzhan.enlightenment.logic.model.Question
import com.michaelzhan.enlightenment.logic.model.QuestionSubTitleType
import com.michaelzhan.enlightenment.ui.cropBitmap
import com.michaelzhan.enlightenment.ui.editAQuestion.EditAQuestion
import com.michaelzhan.enlightenment.ui.editQuestions.EditQuestions
import com.michaelzhan.enlightenment.ui.makeToast
import com.michaelzhan.enlightenment.ui.path2BitmapFromPath
import com.michaelzhan.enlightenment.ui.view.ChooseQuestionsView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class GetQuestion : AppCompatActivity() {

    private val viewModel by lazy { ViewModelProviders.of(this).get(GetQuestionViewModel::class.java)}
    private val binding by lazy { ActivityGetQuestionBinding.inflate(layoutInflater) }


    private val takePhoto = 1
    private val fromAlbum = 2
    private lateinit var imageUri: Uri
    private lateinit var outputImage: File

    private val tag = "GetQuestion"
    private val imgName = "output_image.jpg"

    private var first = true // temp

    lateinit var getImgDialog: MessageDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        DialogX.init(this)

        if (!viewModel.isEdit) {
            MessageDialog.show("Tips", "请选择题目图片", "拍照", "相册", "取消").setOkButton { baseDialog, v ->
                getImgDialog = baseDialog
                outputImage = File(externalCacheDir, "temp.jpg")
                if (outputImage.exists()) {
                    outputImage.delete()
                }
                outputImage.createNewFile()

                imageUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", outputImage);
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
                finish()
                false
            }.setCancelable(false)
        }
        intent.getLongExtra("subjectId", -1L).let {
            if (it != -1L) {
                viewModel.subjectId = it
            }else if (viewModel.subjectId == -1L) { // 如果缓存也没有
                PopTip("Cannot get subject id")
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            takePhoto -> {
                if (resultCode == Activity.RESULT_OK) {
                    // 将拍摄的照片显示出来
                    GlobalScope.launch {
                        showLoadingFloat()
                        val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
                        onGetImageCallBack(saveBitmap(imgName, rotateIfRequired(bitmap)))
                    }
                }
            }
            fromAlbum -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    data.data?.let { uri ->
                        // 将选择的照片显示
                        val bitmap = getBitmapFromUri(uri)
                        if (bitmap != null) {
                            GlobalScope.launch {
                                showLoadingFloat()
                                onGetImageCallBack(saveBitmap(imgName, bitmap))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getBitmapFromUri(uri: Uri) = contentResolver.openFileDescriptor(uri, "r")?.use {
        BitmapFactory.decodeFileDescriptor(it.fileDescriptor)
    }

    private fun rotateIfRequired(bitmap: Bitmap): Bitmap {
        val exif = ExifInterface(outputImage.path)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270)
            else -> bitmap
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degree: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        bitmap.recycle()
        return rotatedBitmap
    }

    private fun saveBitmap(name: String?, bm: Bitmap):Boolean {
        Log.d("Save Bitmap", "Ready to save picture")
        //指定我们想要存储文件的地址
        val TargetPath: String = externalCacheDir!!.absolutePath + "/"
        Log.d("Save Bitmap", "Save Path=$TargetPath")
        //判断指定文件夹的路径是否存在
        if (!File(TargetPath).exists()) {
            Log.d("Save Bitmap", "TargetPath isn't exist")
            return false
        } else {
            //如果指定文件夹创建成功，那么我们则需要进行图片存储操作
            val saveFile = File(TargetPath, name)
            try {
                val saveImgOut = FileOutputStream(saveFile)
                // compress - 压缩的意思
                bm.compress(Bitmap.CompressFormat.JPEG, 100, saveImgOut)
                //存储完成后需要清除相关的进程
                saveImgOut.flush()
                saveImgOut.close()
                Log.d("Save Bitmap", "The picture is save to your phone!")
                return true
            } catch (ex: IOException) {
                ex.printStackTrace()
                return false
            }
        }
    }

    private fun showLoadingFloat() {
        WaitDialog.show("Please Wait!")
    }

    private fun closeLoadingFloat() {
        Handler(Looper.getMainLooper()).postDelayed(Runnable { WaitDialog.dismiss() }, 500)
    }

    private fun onGetImageCallBack(isSuccessful: Boolean) {
        closeLoadingFloat()
        if (::getImgDialog.isInitialized) {
            getImgDialog.dismiss()
        }
        if (!isSuccessful) return
        viewModel.isEdit = true
        showEditDialog()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (viewModel.isEdit && first) {
            showEditDialog()
        }
    }

    private fun showEditDialog() {
        first = false
        FullScreenDialog.show(object : OnBindView<FullScreenDialog?>(R.layout.dialog_get_question_choose_question) {
            @SuppressLint("SetTextI18n", "CutPasteId")
            override fun onBind(dialog: FullScreenDialog?, v: View?) {
                if (v != null) {
                    val chooseView = v.findViewById<ChooseQuestionsView>(R.id.dialog_get_question_choose_question_choose_view)
                    val img = path2BitmapFromPath(File(externalCacheDir, imgName).absolutePath)
                    val scanLineView = v.findViewById<ImageView>(R.id.dialog_get_question_choose_question_scan_line)
                    scanLineView.visibility = View.INVISIBLE
                    GlobalScope.launch {
                        val buttons = arrayListOf(
                            v.findViewById<TextView>(R.id.dialog_get_question_choose_question_ok_button),
                            v.findViewById<TextView>(R.id.dialog_get_question_choose_question_more_action)
                        )

                        if (!viewModel.isScan) {
                            runOnUiThread {
                                for (i in buttons) {
                                    i.isClickable = false
                                }
                            }

                            val retInit: Boolean = viewModel.yolov7ncnn.loadModel(assets, 0, 0)
                            if (!retInit) {
                                runOnUiThread {
                                    "yolov7ncnn Init failed".makeToast()
                                }
                                return@launch
                            }

                            for (i in 0..10) {
                                WaitDialog.show("识别中...", 0.1f*i);
                                delay(75)
                            }

                            val objs = viewModel.yolov7ncnn.Detect(img, false)
                            if (objs != null) {
                                viewModel.questionBoxList.clear()
                                for (i in objs) {
                                    if (i != null) {
                                        val mBox = ChooseQuestionsView.QuestionBox().apply {
                                            topLeft.x = i.x
                                            topLeft.y = i.y
                                            bottomEnd.x = i.x + i.w
                                            bottomEnd.y = i.y + i.h
                                        }
                                        chooseView.post {
                                            chooseView.addObj(mBox)
                                            viewModel.questionBoxList.add(mBox)
                                        }
                                    }
                                }
                            }

                            runOnUiThread {
                                for (i in buttons) {
                                    i.isClickable = true
                                }
                            }

                            delay(100)
                            TipDialog.dismiss()

                            viewModel.isScan = true
                        }
                    }
                    Log.d(tag, "bing.root.width = ${binding.root.width}")
                    chooseView.post {
                        chooseView.setImage(img)

                        // TODO: Gt Model Result -> chooseView

                        for (i in viewModel.questionBoxList) {
                            chooseView.addObj(i)
                        }


                        chooseView.setBoxChangeListener{ objs ->
                            viewModel.questionBoxList.clear()
                            viewModel.questionBoxList.addAll(objs)
                        }
                        chooseView.setCreateCompleteListener {
                            v.findViewById<TextView>(R.id.dialog_get_question_choose_question_more_action).text = "添加选区"
                        }
                    }

                    v.findViewById<TextView>(R.id.dialog_get_question_choose_question_ok_button).setOnClickListener {
                        GlobalScope.launch {
                            WaitDialog.show("Please Wait...");
                            val questionsIds = LongArray(viewModel.questionBoxList.size)
                            val img = path2BitmapFromPath(File(externalCacheDir, imgName).absolutePath)
                            for (i in 0 until viewModel.questionBoxList.size) {
                                val imgFilePath = EnlightenmentApplication.userQuestionsImagesPath + File.separator + System.currentTimeMillis().toString() + ".png"
                                val suchTextPath = EnlightenmentApplication.userQuestionsRichTextPath + File.separator + System.currentTimeMillis().toString()

                                questionsIds[i] = viewModel.addQuestion(
                                    Question(
                                        "Title",
                                        System.currentTimeMillis(),
                                        listOf(
                                            Pair(QuestionSubTitleType.CONTENT, suchTextPath + "_Content.dt"),
                                            Pair(QuestionSubTitleType.WHY, suchTextPath + "_Why.dt"),
                                            Pair(QuestionSubTitleType.ANSWER, suchTextPath + "_Answer.dt")
                                        ),
                                        1.0f,
                                        false,
                                        viewModel.subjectId
                                    )
                                )
                                viewModel.questionBoxList[i].let {
                                    val x = it.topLeft.x.toInt().let { it2 ->
                                        if (it2 < 0) 0 else if (it2 > img.width) img.width else it2
                                    }
                                    val y = it.topLeft.y.toInt().let { it2 ->
                                        if (it2 < 0) 0 else if (it2 > img.height) img.height else it2
                                    }
                                    val width = (it.bottomEnd.x-it.topLeft.x).toInt().let { it2 ->
                                        if (x + it2 > img.width) img.width - x else it2
                                    }
                                    val height = (it.bottomEnd.y-it.topLeft.y).toInt().let { it2 ->
                                        if (y + it2 > img.height) img.height - y else it2
                                    }
                                    FileUtils.saveBitmap(
                                        cropBitmap(img, x, y, width, height),
                                        imgFilePath
                                    )
                                    FileUtils.writeInternal(
                                        suchTextPath + "_Content.dt", "<img width=\"100%\" src=\"$imgFilePath\">"
                                    )
                                }
                            }
                            if (viewModel.questionBoxList.size > 1) {
                                startActivity(Intent(this@GetQuestion, EditQuestions::class.java).apply {
                                    finish()
                                    putExtra("questionsId", questionsIds)
                                    putExtra("subjectId", viewModel.subjectId)
                                })
                            }else if (viewModel.questionBoxList.size == 1) {
                                startActivity(Intent(this@GetQuestion, EditAQuestion::class.java).apply {
                                    finish()
                                    putExtra("questionId", questionsIds[0])
                                })
                            }else { // Empty
                                WaitDialog.dismiss();
                                PopTip.show("请先框选至少一道题目");
                            }
                        }
                    }
                    v.findViewById<TextView>(R.id.dialog_get_question_choose_question_close_button).setOnClickListener {
                        quit()
                    }
                    v.findViewById<TextView>(R.id.dialog_get_question_choose_question_more_action).setOnClickListener {
                        if (!chooseView.isCreating()) {
                            chooseView.addObj()
                            v.findViewById<TextView>(R.id.dialog_get_question_choose_question_more_action).text = "取消"
                        }else {
                            chooseView.cancelCreating()
                            v.findViewById<TextView>(R.id.dialog_get_question_choose_question_more_action).text = "添加选区"
                        }
                    }
                }
            }
        }).setAllowInterceptTouch(false).onBackPressedListener = OnBackPressedListener<FullScreenDialog> {
            quit()
            false
        }
    }

    private fun quit() {
        if (viewModel.questionBoxList.size != 0) {
            MessageDialog.show("Tips", "确定要退出吗？\n还有题目未保存", "Yes", "No").setOkButton { dialog, v ->
                finish()
                false
            }
        }else {
            finish()
        }
    }
}
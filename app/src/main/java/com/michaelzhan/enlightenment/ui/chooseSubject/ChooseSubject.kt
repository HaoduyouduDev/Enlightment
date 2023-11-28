package com.michaelzhan.enlightenment.ui.chooseSubject

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.kongzue.dialogx.DialogX
import com.michaelzhan.enlightenment.EnlightenmentApplication
import com.michaelzhan.enlightenment.databinding.ActivityChooseSubjectBinding
import com.michaelzhan.enlightenment.ui.makeToast
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ChooseSubject : AppCompatActivity() {

    private val viewModel by lazy { ViewModelProviders.of(this).get(ChooseSubjectViewModel::class.java) }
    private val binding by lazy { ActivityChooseSubjectBinding.inflate(layoutInflater) }
    private lateinit var mSubjectsAdapter: SubjectsAdapter
    private val editTips = "管理错题本"

    private val tag = "ChooseSubject"

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        DialogX.init(this)

        viewModel.mMode = intent.getIntExtra("mode", ChooseSubjectMode.NORMAL)

        if (viewModel.mMode == ChooseSubjectMode.EDIT_SUBJECTS) {
            binding.title.setText(editTips)
        }

        viewModel.spanCount = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !isInMultiWindowMode) {
            when (resources.configuration.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> 3
                Configuration.ORIENTATION_PORTRAIT -> 2
                else -> 3
            }
        }else{
            2
        }

        initSubjects()

        viewModel.allIconLiveData.observe(this, Observer { result ->
            if (result != null) {
                viewModel.allIcon.clear()
                viewModel.allIcon.addAll(result)
            } else {
                "Error".makeToast()
            }
        })

        viewModel.subjectLiveData.observe(this, Observer { result ->
            if (result != null) {
                viewModel.subjectList.clear()
                viewModel.subjectList.addAll(result)
                mSubjectsAdapter.notifyDataSetChanged()
            } else {
                "Can't not load subjects.".makeToast()
            }
        })

        binding.title.setOnBackClickListener {
            finish()
        }
    }

    private fun initSubjects() {
        mSubjectsAdapter = SubjectsAdapter(this, viewModel.subjectList)
        binding.subjectList.apply{
            layoutManager = GridLayoutManager(this@ChooseSubject, viewModel.spanCount)
            adapter = mSubjectsAdapter
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            2 -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    data.data?.let { uri ->
//                        // 将选择的照片显示
//                        val bitmap = getBitmapFromUri(uri)
//                        if (bitmap != null) {
//                            GlobalScope.launch {
//                                val fileName = "UserIcon${System.currentTimeMillis()}.png"
//                                if (!saveBitmapToMoreIcon(fileName, bitmap)) mSubjectsAdapter.getIconAdapter()?.onGetImageCallBack(null)
//                                else mSubjectsAdapter.getIconAdapter()?.onGetImageCallBack(EnlightenmentApplication.userIconPath + "/" + fileName)
//                            }
//                        }

                        cropPhoto(uri)
                    }
                }
            }

            UCrop.REQUEST_CROP -> {
                if (resultCode == Activity.RESULT_OK) {
                    mSubjectsAdapter.getIconAdapter()?.onGetImageCallBack(data?.let {
                        UCrop.getOutput(
                            it
                        )
                    }?.path)
                }else if (resultCode == UCrop.RESULT_ERROR) {
                    mSubjectsAdapter.getIconAdapter()?.onGetImageCallBack(null)
                }
            }
        }
    }

    private fun getBitmapFromUri(uri: Uri) = contentResolver.openFileDescriptor(uri, "r")?.use {
        BitmapFactory.decodeFileDescriptor(it.fileDescriptor)
    }

    private fun saveBitmapToMoreIcon(name: String, bm: Bitmap): Boolean {
        Log.d("Save Bitmap", "Ready to save picture")
        //指定我们想要存储文件的地址
        val targetPath: String = EnlightenmentApplication.userIconPath + "/"
        Log.d("Save Bitmap", "Save Path=$targetPath")
        //判断指定文件夹的路径是否存在
        if (!File(targetPath).exists()) {
            Log.d("Save Bitmap", "TargetPath isn't exist")
            return true
        } else {
            //如果指定文件夹创建成功，那么我们则需要进行图片存储操作
            val saveFile = File(targetPath, name)
            try {
                val saveImgOut = FileOutputStream(saveFile)
                // compress - 压缩的意思
                bm.compress(Bitmap.CompressFormat.PNG, 80, saveImgOut)
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
        // 是否让用户调整范围(默认false)，如果开启，可能会造成剪切的图片的长宽比不是设定的
        // 如果不开启，用户不能拖动选框，只能缩放图片
        options.setFreeStyleCropEnabled(false)
        // 设置图片压缩质量
        options.setCompressionQuality(70)
        // 不显示网格线
        options.setShowCropGrid(false)
        // 设置源uri及目标uri
        UCrop.of(
            uri,
            Uri.fromFile(
                File(
                    EnlightenmentApplication.userIconPath + "/" +
                            "UserIcon${System.currentTimeMillis()}.png"
                )
            )
        ) // 长宽比
            .withOptions(options)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(200, 200)
            .start(this)
    }
}
package com.michaelzhan.enlightenment.ui.showAQuestion

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.kongzue.dialogx.DialogX
import com.michaelzhan.enlightenment.FileUtils
import com.michaelzhan.enlightenment.databinding.ActivityShowAQuestionBinding
import com.michaelzhan.enlightenment.ui.editAQuestion.EditAQuestion
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ShowAQuestion : AppCompatActivity() {

    private val viewModel by lazy { ViewModelProviders.of(this).get(ShowQuestionViewModel::class.java) }
    private val binding by lazy { ActivityShowAQuestionBinding.inflate(layoutInflater) }

    private lateinit var mTittlesAdapter: TittlesAdapter

    private var canEdit = true

    private val tag = "ShowAQuestion"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        DialogX.init(this)

        initTittles()

        intent.getLongExtra("questionId", -1L).let {
            GlobalScope.launch {
                if (it != -1L) {
                    viewModel.mQuestion = viewModel.getQuestionFromId(it)
                }

                runOnUiThread {
                    if (viewModel.mQuestion != null && viewModel.tittleList.isEmpty()) {
                        refreshTitles()
                    }
                }
            }
        }

        intent.getBooleanExtra("canEdit", true).let {
            canEdit = it
        }

        if (canEdit) {
            binding.editButton.visibility = View.VISIBLE
        }else {
            binding.editButton.visibility = View.INVISIBLE
        }

        binding.editButton.setOnClickListener {
            startActivity(Intent(this, EditAQuestion::class.java).apply {
                putExtra("questionId", viewModel.mQuestion!!.id)
                putExtra("subjectId", viewModel.mQuestion!!.subjectId)
            })
        }

        binding.tittle.setOnBackClickListener {
            finish()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun refreshTitles(refreshQuestion: Boolean = false) {
        viewModel.tittleList.clear()

        if (refreshQuestion) {
            viewModel.getQuestionFromId(viewModel.mQuestion!!.id)?.let {
                viewModel.mQuestion = it
            }
        }

        for (i in viewModel.mQuestion!!.sub_titles) {
            if (FileUtils.readInternal(i.second).isNotEmpty()) {
                val mTittle = TittlesAdapter.Tittle(
                    i.second, i.first
                )
                viewModel.tittleList.add(mTittle)
            }
        }
        mTittlesAdapter.notifyDataSetChanged()
        binding.tittle.setText(viewModel.mQuestion!!.title)
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.mQuestion != null) {
            refreshTitles(true)
        }
    }

    private fun initTittles() {
        mTittlesAdapter = TittlesAdapter(
            this,
            viewModel.tittleList
        )

        binding.tittleList.apply {
            layoutManager = LinearLayoutManager(context).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            adapter = mTittlesAdapter
        }
        binding.tittleList.isNestedScrollingEnabled = false
    }
}
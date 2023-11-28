package com.michaelzhan.enlightenment.ui.showReviewTime

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.kongzue.dialogx.DialogX
import com.michaelzhan.enlightenment.databinding.ActivityShowReviewTimeBinding

class ShowReviewTime : AppCompatActivity() {

    private val viewModel by lazy { ViewModelProviders.of(this).get(ShowReviewTimeViewModel::class.java) }
    private val binding by lazy { ActivityShowReviewTimeBinding.inflate(layoutInflater) }

    private lateinit var mReviewTimeAdapter: ReviewTimeAdapter

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        DialogX.init(this)

        initReviewTime()

        viewModel.deletePos = -1
        viewModel.reviewTimeLiveData.observe(this) { reviewTimeList ->
            viewModel.reviewTimeList.clear()
            reviewTimeList.forEach {
                val question = viewModel.getQuestionFromId(it.questionId)
                if (question != null) {
                    viewModel.reviewTimeList.add(
                        ReviewTimeAdapter.QuestionWithReviewTime(
                            question,
                            it
                        )
                    )
                }

                if (viewModel.deletePos != -1) {
                    mReviewTimeAdapter.notifyItemRemoved(viewModel.deletePos)
                }else {
                    mReviewTimeAdapter.notifyDataSetChanged()
                }
            }

            if (viewModel.reviewTimeList.isEmpty()) {
                binding.noDataMask.visibility = View.VISIBLE
            }else {
                binding.noDataMask.visibility = View.INVISIBLE
            }
        }

        binding.title.setOnBackClickListener {
            finish()
        }
    }

    private fun initReviewTime() {
        mReviewTimeAdapter = ReviewTimeAdapter(this, viewModel.reviewTimeList)
        binding.reviewList.apply {
            layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.VERTICAL }
            adapter = mReviewTimeAdapter
        }
    }
}
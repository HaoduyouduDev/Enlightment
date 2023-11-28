package com.michaelzhan.enlightenment.ui.questionsList

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.kongzue.dialogx.DialogX
import com.michaelzhan.enlightenment.databinding.ActivityQuestionsListBinding
import com.michaelzhan.enlightenment.logic.Repository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.regex.Matcher
import java.util.regex.Pattern

class QuestionsList : AppCompatActivity() {
    private val binding by lazy { ActivityQuestionsListBinding.inflate(layoutInflater) }
    private val viewModel by lazy { ViewModelProviders.of(this).get(QuestionListViewModel::class.java) }
    private lateinit var mQuestionsAdapter: QuestionsAdapter

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        DialogX.init(this)

        intent.getLongExtra("subjectId", -1L).let {
            if (it != -1L) {
                viewModel.subjectId = it
                GlobalScope.launch {
                    Repository.getErrorBookFromId(it)?.let { it2 ->
                        viewModel.firstRequestQuestions(it2.bindingQuestions)
                        binding.title.setText(it2.name)
                    }
                }
            }
        }

        initQuestions()

        viewModel.addPos = -1
        viewModel.delPos = -1

        viewModel.questionsLiveData.observe(this) { questionList ->
            viewModel.questionsList.clear()
            viewModel.questionsList.addAll(questionList)
            if (viewModel.delPos != -1) {
                mQuestionsAdapter.notifyItemRemoved(viewModel.delPos)
            }else if(viewModel.addPos != -1) {
                mQuestionsAdapter.notifyItemInserted(viewModel.addPos)
            }else {
                mQuestionsAdapter.notifyDataSetChanged()
            }
        }

        binding.title.setOnBackClickListener {
            finish()
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
}
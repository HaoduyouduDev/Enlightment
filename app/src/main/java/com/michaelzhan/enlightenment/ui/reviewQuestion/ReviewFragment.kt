package com.michaelzhan.enlightenment.ui.reviewQuestion

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.michaelzhan.enlightenment.EnlightenmentApplication
import com.michaelzhan.enlightenment.FileUtils
import com.michaelzhan.enlightenment.R
import com.michaelzhan.enlightenment.databinding.FragmentReviewQuestionBinding
import com.michaelzhan.enlightenment.logic.model.Question
import com.michaelzhan.enlightenment.logic.model.QuestionSubTitleType
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ReviewFragment : Fragment() {

    private val viewModel by lazy { ViewModelProviders.of(requireActivity()).get(ReviewQuestionViewModel::class.java) }

    private var _binding: FragmentReviewQuestionBinding? = null
    // 只能在onCreateView与onDestoryView之间的生命周期里使用
    private val binding: FragmentReviewQuestionBinding get() = _binding!!

    private lateinit var mQuestion: Question
    private var mAnswer: TittlesAdapter.Tittle? = null

    private val mTag = "ReviewQuestionFragment"

    private lateinit var mTittlesAdapter: TittlesAdapter

    private val tittleList = ArrayList<TittlesAdapter.Tittle>()

    private var index = 0

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewQuestionBinding.inflate(inflater, container, false)

        viewModel.questionsIdList?.let {
            viewModel.getQuestionFromId(it[index])?.let { it2 ->
                mQuestion = it2

                requireActivity().getSharedPreferences(EnlightenmentApplication.userReviewTempName, Context.MODE_PRIVATE).edit {
                    putLong("lastReviewId", it2.id)
                }
            }
        }
//        Log.d(mTag, mQuestion.toString())
//        Log.d(mTag, index.toString())

        binding.showAnswerButton.visibility = if (viewModel.showAnswerButtonIsGone[index] == true) View.GONE else View.VISIBLE

        initTittles()

//        viewModel.getQuestionFromId(viewModel.mQuestion!!.id)?.let {
//            viewModel.mQuestion = it
//        }

        if (::mQuestion.isInitialized) {
            binding.mRatingBar.rating = mQuestion.proficiency/20f*100f
            if (tittleList.isEmpty()) {
                for (i in mQuestion.sub_titles) {
                    if (FileUtils.readInternal(i.second).isNotEmpty() || i.first == QuestionSubTitleType.ANSWER) {
                        val mTittle = TittlesAdapter.Tittle(
                            i.second, i.first
                        )
                        if (i.first != QuestionSubTitleType.ANSWER) {
                            tittleList.add(mTittle)
                        }else {
                            mAnswer = mTittle
                        }

                    }
                }
            }
        }

        mTittlesAdapter.notifyDataSetChanged()

        binding.showAnswerButton.setOnClickListener {
            if (mAnswer != null) {
                tittleList.add(mAnswer!!)
                mTittlesAdapter.notifyItemInserted(tittleList.size - 1)
            }
            it.visibility = View.GONE
            viewModel.showAnswerButtonIsGone[index] = true
        }


        binding.mRatingBar.setOnRatingBarChangeListener { ratingBar, fl, b ->
            Log.d(mTag, fl.toString())
            if (::mQuestion.isInitialized) {
                viewModel.changeProficiency(mQuestion, fl*20f/100f)
            }
        }

        binding.OKButton.visibility = if (index == ((viewModel.questionsIdList?.size) ?: (index + 1)) - 1) View.VISIBLE else View.GONE

        binding.OKButton.setOnClickListener {
            (requireActivity() as ReviewQuestion).ok()
        }

        return binding.root
    }

    private fun initTittles() {
        mTittlesAdapter = TittlesAdapter(
            requireActivity(),
            tittleList
        )

        binding.tittleList.apply {
            layoutManager = LinearLayoutManager(context).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            adapter = mTittlesAdapter
        }
        binding.tittleList.isNestedScrollingEnabled = false

    }

    fun setPos(pos: Int) {
        index = pos
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
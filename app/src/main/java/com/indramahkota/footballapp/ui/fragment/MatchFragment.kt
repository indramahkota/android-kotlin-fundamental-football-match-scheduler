package com.indramahkota.footballapp.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.indramahkota.footballapp.R
import com.indramahkota.footballapp.data.source.locale.entity.MatchEntity
import com.indramahkota.footballapp.data.source.repository.Result
import com.indramahkota.footballapp.ui.activity.DetailsMatchActivity
import com.indramahkota.footballapp.ui.activity.DetailsMatchActivity.Companion.PARCELABLE_MATCH_DATA
import com.indramahkota.footballapp.ui.adapter.MatchHorizontalAdapter
import com.indramahkota.footballapp.viewmodel.LeagueDetailsViewModel
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_match.*
import javax.inject.Inject

class MatchFragment : DaggerFragment() {
    companion object {
        private const val ARG_SECTION_FRAGMENT = "section_fragment"
        private const val ARG_SAVE_NEXT_DATA = "save_next_data"
        private const val ARG_SAVE_PREV_DATA = "save_prev_data"

        fun newInstance(fragment: String) = MatchFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_SECTION_FRAGMENT, fragment)
            }
        }
    }

    private var nextMatchsData: ArrayList<MatchEntity>? = null
    private var prevMatchsData: ArrayList<MatchEntity>? = null

    private lateinit var nextMatchAdapter: MatchHorizontalAdapter
    private lateinit var prevMatchAdapter: MatchHorizontalAdapter

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nextMatchsData = savedInstanceState?.getParcelableArrayList(ARG_SAVE_NEXT_DATA)
        prevMatchsData = savedInstanceState?.getParcelableArrayList(ARG_SAVE_PREV_DATA)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_match, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv_next_match.setHasFixedSize(true)
        rv_prev_match.setHasFixedSize(true)

        val listNextData = mutableListOf<MatchEntity>()
        nextMatchAdapter =
            MatchHorizontalAdapter(
                listNextData
            ) { matchModel ->
                run {
                    val intent = Intent(activity, DetailsMatchActivity::class.java).apply {
                        putExtra(PARCELABLE_MATCH_DATA, matchModel)
                    }
                    startActivity(intent)
                }
            }
        rv_next_match.adapter = nextMatchAdapter

        val listPrevData = mutableListOf<MatchEntity>()
        prevMatchAdapter =
            MatchHorizontalAdapter(
                listPrevData
            ) { matchModel ->
                run {
                    val intent = Intent(activity, DetailsMatchActivity::class.java).apply {
                        putExtra(PARCELABLE_MATCH_DATA, matchModel)
                    }
                    startActivity(intent)
                }
            }
        rv_prev_match.adapter = prevMatchAdapter

        if (nextMatchsData != null) {
            initializeNext(nextMatchsData)
        } else {
            getNextListData()
        }

        if (prevMatchsData != null) {
            initializePrev(prevMatchsData)
        } else {
            getPrevListData()
        }
    }

    private fun getNextListData() {
        val viewModel = activity?.let {
            ViewModelProvider(
                it,
                viewModelFactory
            ).get(LeagueDetailsViewModel::class.java)
        }
        viewModel?.newNextMatchData?.observe(
            viewLifecycleOwner,
            {
                checkState(it, 0)
            })
    }

    private fun getPrevListData() {
        val viewModel = activity?.let {
            ViewModelProvider(
                it,
                viewModelFactory
            ).get(LeagueDetailsViewModel::class.java)
        }
        viewModel?.newPrevMatchData?.observe(
            viewLifecycleOwner,
            {
                checkState(it, 1)
            })
    }

    private fun checkState(it: Result<List<MatchEntity>?>, int: Int) {
        when (it) {
            is Result.Success -> {
                if (int == 0)
                    initializeNext(it.data)
                else
                    initializePrev(it.data)
            }
            is Result.Error -> Toast.makeText(activity, it.exception.toString(), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun initializeNext(it: List<MatchEntity>?) {
        if (it.isNullOrEmpty()) {
            next_no_data.visibility = View.VISIBLE
        } else {
            next_no_data.visibility = View.INVISIBLE
        }

        nextMatchsData = it?.let { ArrayList(it) }
        it?.let { nextMatchAdapter.replace(it) }

        next_shimmer_view_container.visibility = View.GONE
    }

    private fun initializePrev(it: List<MatchEntity>?) {
        if (it.isNullOrEmpty()) {
            prev_no_data.visibility = View.VISIBLE
        } else {
            prev_no_data.visibility = View.INVISIBLE
        }

        prevMatchsData = it?.let { ArrayList(it) }
        it?.let { prevMatchAdapter.replace(it) }

        prev_shimmer_view_container.visibility = View.GONE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(ARG_SAVE_NEXT_DATA, nextMatchsData)
        outState.putParcelableArrayList(ARG_SAVE_PREV_DATA, prevMatchsData)
    }
}
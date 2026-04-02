package com.samyak.repostore.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.samyak.repostore.RepoStoreApp
import com.samyak.repostore.databinding.FragmentTrendingBinding
import com.samyak.repostore.ui.activity.DetailActivity
import com.samyak.repostore.ui.adapter.TrendingAdapter
import com.samyak.repostore.ui.viewmodel.TrendingUiState
import com.samyak.repostore.ui.viewmodel.TrendingViewModel
import com.samyak.repostore.ui.viewmodel.TrendingViewModelFactory
import kotlinx.coroutines.launch

class TrendingFragment : Fragment() {

    private var _binding: FragmentTrendingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TrendingViewModel by viewModels {
        TrendingViewModelFactory((requireActivity().application as RepoStoreApp).repository)
    }

    private lateinit var trendingAdapter: TrendingAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrendingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeRefresh()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        trendingAdapter = TrendingAdapter { appItem ->
            val intent = DetailActivity.newIntent(
                requireContext(),
                appItem.repo.owner.login,
                appItem.repo.name
            )
            startActivity(intent)
        }

        binding.rvTrending.apply {
            adapter = trendingAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0) {
                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                        val visibleItemCount = layoutManager.childCount
                        val totalItemCount = layoutManager.itemCount
                        val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()

                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            viewModel.loadMore()
                        }
                    }
                }
            })
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    handleUiState(state)
                }
            }
        }
    }

    private fun handleUiState(state: TrendingUiState) {
        binding.swipeRefresh.isRefreshing = false
        binding.progressBar.visibility = if (state is TrendingUiState.Loading) View.VISIBLE else View.GONE
        binding.rvTrending.visibility = if (state is TrendingUiState.Success || state is TrendingUiState.LoadingMore) View.VISIBLE else View.GONE
        binding.tvError.visibility = if (state is TrendingUiState.Error || state is TrendingUiState.Empty) View.VISIBLE else View.GONE

        when (state) {
            is TrendingUiState.Success -> {
                trendingAdapter.submitList(state.apps)
            }
            is TrendingUiState.LoadingMore -> {
                trendingAdapter.submitList(state.currentApps)
            }
            is TrendingUiState.Error -> {
                binding.tvError.text = state.message
            }
            is TrendingUiState.Empty -> {
                binding.tvError.text = "No trending apps found"
            }
            else -> {}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = TrendingFragment()
    }
}

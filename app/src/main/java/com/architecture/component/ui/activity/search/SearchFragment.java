package com.architecture.component.ui.activity.search;

import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.architecture.component.R;
import com.architecture.component.binding.FragmentDataBindingComponent;
import com.architecture.component.databinding.SearchFragmentBinding;
import com.architecture.component.ui.adapter.RepoListAdapter;
import com.architecture.component.ui.base.NavigationController;
import com.architecture.component.util.common.AutoClearedValue;
import com.architecture.component.viewmodel.SearchViewModel;

import javax.inject.Inject;

import timber.log.Timber;

public class SearchFragment extends LifecycleFragment {

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Inject
    NavigationController navigationController;

    private DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);
    private AutoClearedValue<SearchFragmentBinding> binding;
    private AutoClearedValue<RepoListAdapter> adapter;
    private SearchViewModel searchViewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // data binding
        SearchFragmentBinding dataBinding = DataBindingUtil
                .inflate(inflater, R.layout.search_fragment, container, false, dataBindingComponent);

        binding = new AutoClearedValue<>(this, dataBinding);

        return dataBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        // search view model
        searchViewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(SearchViewModel.class);

        dataBinding();

        // adapter
        RepoListAdapter repoListAdapter;

        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Data binding.
     */
    private void dataBinding() {
        // add scroll listener
        binding.get().repoList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager)
                        recyclerView.getLayoutManager();

                int lastPosition = layoutManager.findLastVisibleItemPosition();
                if (lastPosition == adapter.get().getItemCount()) {
                    searchViewModel.loadNextPage();
                }
             }
        });

        // search result observe
        searchViewModel.getResults().observe(this, result -> {
            Timber.d("update ui");

            binding.get().setSearchResource(result);
            binding.get().setResultCount(result == null || result.data == null
                    ? 0 : result.data.size());
            adapter.get().replace(result == null ? null : result.data);
            binding.get().executePendingBindings();
        });

        // load more
        searchViewModel.getLoadMoreState().observe(this, loadingMore -> {
            if (loadingMore == null) {
                binding.get().setLoadingMore(false);
            } else {
                binding.get().setLoadingMore(loadingMore.isRunning());
                String error = loadingMore.getErrorMessageIfNotHandled();
                if (error != null) {
                    Snackbar.make(binding.get().loadMoreBar, error, Snackbar.LENGTH_LONG).show();
                }
            }

            binding.get().executePendingBindings();
        });
    }
}
package com.spoiledit.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.spoiledit.R;
import com.spoiledit.activities.DetailsMovieActivity;
import com.spoiledit.adapters.MoviesPopularAdapter;
import com.spoiledit.constants.Constants;
import com.spoiledit.constants.Status;
import com.spoiledit.utils.ViewUtils;
import com.spoiledit.viewmodels.DashboardViewModel;

public class MoviesPopularFragment extends RootFragment {
    public static final String TAG = MoviesPopularFragment.class.getCanonicalName();

    private DashboardViewModel dashboardViewModel;
    private MoviesPopularAdapter popularAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dashboardViewModel = ViewModelProviders.of(getActivity()).get(DashboardViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movies_popular, container, false);
    }

    @Override
    public void initUi(View view) {
        swipeRefreshLayout = view.findViewById(R.id.srl_movies_popular);
    }

    @Override
    public void initialiseListener(View view) {
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void setUpRecycler(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.rv_movies_popular);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        popularAdapter = new MoviesPopularAdapter(getContext(),
                (lastSelection, currentSelection) -> {
                    dashboardViewModel.requestMovieDetails(
                            popularAdapter.getItemAt(currentSelection).getId()
                    );
                });

        recyclerView.setAdapter(popularAdapter);
        ViewUtils.addFabOffset(getContext(), recyclerView);

        popularAdapter.registerAdapterDataObserver(getAdapterDataObserver());
    }

    @Override
    public void addObservers() {
        dashboardViewModel.getApiStatusModelMutable().observe(this, apiStatusModel -> {
            if (apiStatusModel.getApi() == Constants.Api.MOVIES_UPCOMING) {
                if (apiStatusModel.getStatus() == Status.Request.API_HIT) {
                    if (!swipeRefreshLayout.isRefreshing())
                        showLoader(apiStatusModel.getMessage());
                } else {
                    hideLoader();
                    if (apiStatusModel.getStatus() == Status.Request.API_ERROR)
                        showFailure(false, apiStatusModel.getMessage());
                }

            } else if (apiStatusModel.getApi() == Constants.Api.MOVIES_DETAILS) {
                if (apiStatusModel.getStatus() != Status.Request.API_HIT) {
                    popularAdapter.removeLastSelection();
                    if (apiStatusModel.getStatus() == Status.Request.API_SUCCESS)
                        startActivity(new Intent(getContext(), DetailsMovieActivity.class));
                    else
                        showFailure(false, apiStatusModel.getMessage());
                }
            }
        });

        dashboardViewModel.getMoviePopularModelsMutable().observe(this, moviePopularModels -> {
            if (moviePopularModels != null)
                popularAdapter.setItems(moviePopularModels);
        });
    }

    @Override
    public void onAdapterDataChanged() {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void requestData() {
        showLoader();
        dashboardViewModel.requestMoviesPopular();
    }

    @Override
    public void onRefresh() {
        dashboardViewModel.requestMoviesPopular();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        popularAdapter.unregisterAdapterDataObserver(getAdapterDataObserver());
    }
}

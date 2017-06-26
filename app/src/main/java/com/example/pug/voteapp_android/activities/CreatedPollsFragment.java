package com.example.pug.voteapp_android.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.pug.voteapp_android.adapters.PollRecyclerAdapter;
import com.example.pug.voteapp_android.models.Poll;
import com.example.pug.voteapp_android.R;
import com.github.jasminb.jsonapi.JSONAPIDocument;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class CreatedPollsFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private PollRecyclerAdapter adapter;
    private int currentPollsPage = 0;
    private int currentPollsPageSize = 10;

    public CreatedPollsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_created_polls, container, false);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.created_layout_swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(swipeRefreshListener);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.created_recyclerview);
        recyclerView.setHasFixedSize(true);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getActivity().getBaseContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity().getBaseContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PollRecyclerAdapter();
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshListener.onRefresh();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void makeToastShort(int msgId) {
        Toast.makeText(getActivity(),
                getString(msgId),
                Toast.LENGTH_SHORT).show();
    }
    private String craftAuthorizationHeader() {
        return "bearer " + ((MainActivity)getActivity())
                .getPrefs()
                .getString("AUTHENTICATION_TOKEN", null);
    }

    SwipeRefreshLayout.OnRefreshListener swipeRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            Observable<Response<JSONAPIDocument<List<Poll>>>> createdPollsObservable = ((MainActivity)getActivity()).getNetworkService().getNetworkApi()
                    .fetchCreatedPollsObservable(currentPollsPage, currentPollsPageSize, craftAuthorizationHeader())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
            createdPollsObservable.subscribe(new Observer<Response<JSONAPIDocument<List<Poll>>>>() {
                @Override
                public void onSubscribe(Disposable d) {

                }

                @Override
                public void onNext(Response<JSONAPIDocument<List<Poll>>> createdPollsResponse) {
                    switch(createdPollsResponse.code()) {
                        case 200:
                            if(createdPollsResponse.body().get().size() == 0) {
                                currentPollsPage = 0;
                                adapter.swapList(new ArrayList<Poll>());
                                break;
                            }
                            adapter.addList((ArrayList<Poll>) createdPollsResponse.body().get());
                            currentPollsPage++;
                            break;
                        case 401:
                            makeToastShort(R.string.network_invalid_token);
                            break;
                        default:
                            makeToastShort(R.string.network_badrequest);
                            break;
                    }
                }

                @Override
                public void onError(Throwable e) {
                    Log.d("DEBUG","Error: " + e.toString());
                    e.printStackTrace();
                    swipeRefreshLayout.setRefreshing(false);
                    makeToastShort(R.string.network_problem);
                }

                @Override
                public void onComplete() {
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        }
    };
}

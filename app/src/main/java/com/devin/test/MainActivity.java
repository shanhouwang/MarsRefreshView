package com.devin.test;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.devin.refreshview.MarsOnLoadListener;
import com.devin.refreshview.MarsRefreshView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private MarsRefreshView mMarsRefreshView;
    private MyRecyclerViewAdapter mAdapter;
    private MyListViewAdapter mMyListViewAdapter;

    volatile List<String> data = new ArrayList<>();

    private int page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMarsRefreshView = findViewById(R.id.marsRefreshView);
        mAdapter = new MyRecyclerViewAdapter(this);
        mMyListViewAdapter = new MyListViewAdapter(this);

        View headerView = LayoutInflater.from(this).inflate(R.layout.layout_header, mMarsRefreshView, false);
        headerView.setBackgroundColor(getResources().getColor(R.color._ffffff));
        View empty = LayoutInflater.from(this).inflate(R.layout.layout_tip_view, mMarsRefreshView, false);
        ((TextView) empty.findViewById(R.id.tipMsg)).setText("数据为空");
        View errorView = LayoutInflater.from(this).inflate(R.layout.layout_tip_view, mMarsRefreshView, false);
        ((TextView) errorView.findViewById(R.id.tipMsg)).setText("网络异常，请重新再试");
        headerView.setBackgroundColor(getResources().getColor(R.color._aaaaaa));

        mMarsRefreshView
                .setLinearLayoutManager()
                .addHeaderView(headerView)
                .setPreLoadMoreEnable(true)
                .setEmptyView(empty, true)
                .setErrorView(errorView, true, true)
                .setAdapter(mAdapter)
                .setMarsOnLoadListener(new MarsOnLoadListener() {
                    @Override
                    public void onRefresh() {
                        mMarsRefreshView.setRefreshing(false);
                        ThreadUtils.get(ThreadUtils.Type.SCHEDULED).callBack(new ThreadUtils.TpCallBack() {
                            @Override
                            public void onResponse(Object obj) {
                                mAdapter.bindData(data);
                                mMyListViewAdapter.bindData(data);
                            }
                        }).schedule(new ThreadUtils.TpRunnable() {
                            @Override
                            public Object execute() {
                                data.clear();
                                for (int i = 0; i < 10; i++) {
                                    data.add("onRefresh: " + i + ", page: " + 1);
                                }
                                return null;
                            }
                        }, 0, TimeUnit.MILLISECONDS);
                    }

                    @Override
                    public void onLoadMore() {
                        ThreadUtils.get(ThreadUtils.Type.SCHEDULED).callBack(new ThreadUtils.TpCallBack() {
                            @Override
                            public void onResponse(Object obj) {
                                mAdapter.bindData(data);
                                mMyListViewAdapter.bindData(data);
                            }
                        }).schedule(new ThreadUtils.TpRunnable() {
                            @Override
                            public Object execute() {
                                data.clear();
                                for (int i = 0; i < 10; i++) {
                                    data.add("onRefresh: " + i + ", page: " + 1);
                                }
                                return null;
                            }
                        }, 0, TimeUnit.MILLISECONDS);
                    }
                }).build();
        // mMarsRefreshView.setRefreshing(true);
        // mMarsRefreshView.showEmptyView();
        mMarsRefreshView.onError();
    }
}

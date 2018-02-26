package com.devin.test;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.MainThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.devin.refreshview.MarsOnLoadListener;
import com.devin.refreshview.MarsRefreshView;
import com.devin.refreshview.MercuryOnLoadMoreListener;
import com.devin.refreshview.VenusOnLoadListener;

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

        View headerView = LayoutInflater.from(this).inflate(R.layout.layout_header, null);
        headerView.setBackgroundColor(getResources().getColor(R.color._ffffff));
        View empty = LayoutInflater.from(this).inflate(R.layout.layout_empty, null);
        headerView.setBackgroundColor(getResources().getColor(R.color._aaaaaa));

        mMarsRefreshView
                .setLinearLayoutManager()
                .setAdapter(mAdapter)
                .addHeaderView(headerView)
                .setPageSizeEnable(false)
                .setEmptyView(empty, true)
                .setMercuryOnLoadMoreListener(1, new MercuryOnLoadMoreListener() {
                    @Override
                    public void onLoadMore(final int page) {
                        ThreadUtils.get(ThreadUtils.Type.SCHEDULED).schedule(new ThreadUtils.TpRunnable() {
                            @Override
                            public Object execute() {
                                if (page == 8) {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mMarsRefreshView.onError();
                                        }
                                    });
                                    return null;
                                }
                                if (page <= 10) {
                                    Log.d("MainActivity", ">>>>>onLoadMore, page: " + page);
                                    for (int i = 0; i < 10; i++) {
                                        data.add("onLoadMore: " + i + ", page: " + page);
                                    }
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mMyListViewAdapter.bindData(data);
                                            mAdapter.bindData(data);
                                        }
                                    });
                                    if (page == 10) {
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                mMarsRefreshView.onComplete();
                                            }
                                        });
                                    }
                                }
                                return null;
                            }
                        }, 500, TimeUnit.MILLISECONDS);
                    }
                });

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

        ThreadUtils.get(ThreadUtils.Type.SCHEDULED).callBack(new ThreadUtils.TpCallBack() {
            @Override
            public void onResponse(Object obj) {
                mAdapter.bindData(data);
                mMyListViewAdapter.bindData(data);
                mMarsRefreshView.showEmptyView(-1);
            }
        }).schedule(new ThreadUtils.TpRunnable() {
            @Override
            public Object execute() {
                data.clear();
                return null;
            }
        }, 10 * 1000, TimeUnit.MILLISECONDS);
    }
}

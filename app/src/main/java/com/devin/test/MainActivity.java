package com.devin.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.devin.refreshview.MarsOnLoadListener;
import com.devin.refreshview.MarsRefreshView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private MarsRefreshView mMarsRefreshView;
    private MyRecyclerViewAdapter mAdapter;

    List<String> data = new ArrayList<>();

    private int page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMarsRefreshView = findViewById(R.id.marsRefreshView);
        mAdapter = new MyRecyclerViewAdapter(this);
        mMarsRefreshView.setLinearLayoutManager();
        mMarsRefreshView.setAdapter(mAdapter);

        View v = LayoutInflater.from(this).inflate(R.layout.layout_footer, null);
        v.setBackgroundColor(getResources().getColor(R.color._ffffff));
        ((TextView) (v.findViewById(R.id.tv_footer))).setText("HeaderView 1 ");
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(-1, 200);
        v.setLayoutParams(params);
        v.setBackgroundColor(getResources().getColor(R.color._aaaaaa));
        mMarsRefreshView.addHeaderView(v);
        mMarsRefreshView.setPreLoadMoreEnable(true);

        mMarsRefreshView.setMarsOnLoadListener(new MarsOnLoadListener() {
            @Override
            public void onRefresh() {
                data.clear();
                page = 1;
                ThreadUtils.get(ThreadUtils.Type.SCHEDULED).callBack(new ThreadUtils.TpCallBack() {
                    @Override
                    public void onResponse(Object obj) {
                        mAdapter.bindData(data);
                        mMarsRefreshView.setRefreshing(false);
                    }
                }).schedule(new ThreadUtils.TpRunnable() {
                    @Override
                    public Object execute() {
                        for (int i = 0; i < 9; i++) {
                            data.add("onRefresh: " + i);
                        }
                        return null;
                    }
                }, 1 * 1000, TimeUnit.MILLISECONDS);
            }

            @Override
            public void onLoadMore() {
                page++;
                if (!NetWorkUtils.isNetworkConnected(getApplicationContext())) {
                    page--;
                    mMarsRefreshView.onError();
                    return;
                }
                ThreadUtils.get(ThreadUtils.Type.SCHEDULED).callBack(new ThreadUtils.TpCallBack() {
                    @Override
                    public void onResponse(Object obj) {
                        mAdapter.bindData(data);
                    }
                }).schedule(new ThreadUtils.TpRunnable() {
                    @Override
                    public Object execute() {
                        Log.d("MainActivity", ">>>>>onLoadMore, page: " + page);
                        for (int i = 0; i < 10; i++) {
                            data.add("onLoadMore: " + i + ", page: " + page);
                        }
                        return null;
                    }
                }, 500, TimeUnit.MILLISECONDS);
            }
        });
        mMarsRefreshView.setRefreshing(true);
    }
}

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

public class MainActivity extends AppCompatActivity {

    private MarsRefreshView mMarsRefreshView;
    private MyRecyclerViewAdapter mAdapter;

    List<String> data = new ArrayList<>();

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
        mMarsRefreshView.addHeaderView(v);

        mMarsRefreshView.setMarsOnLoadListener(new MarsOnLoadListener() {
            @Override
            public void onRefresh() {
                data.clear();
                for (int i = 0; i < 10; i++) {
                    data.add(i + "");
                }
                mAdapter.bindData(data);
                mMarsRefreshView.setRefreshing(false);
            }

            @Override
            public void onLoadMore() {
                Log.d("MainActivity", ">>>>>onLoadMore");
                for (int i = 0; i < 8; i++) {
                    data.add(i + "");
                }
                mAdapter.bindData(data);
                boolean isConnected = NetWorkUtils.isNetworkConnected(getApplicationContext());
                if (!isConnected) {
                    mMarsRefreshView.onError();
                }
                mMarsRefreshView.onComplete();
            }
        });

        mMarsRefreshView.setRefreshing(true);
    }
}

package com.devin.refreshview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;

/**
 * Created by Devin on 2017/11/14.
 *
 * @author Devin
 */
public class MarsRefreshView extends FrameLayout {

    public MarsRefreshView(@NonNull Context context) {
        super(context);
        initView(context, null);
    }

    public MarsRefreshView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public MarsRefreshView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private Context mContext;

    /**
     * 是否用ListView
     */
    private boolean isHaveListView;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mListView;
    private RecyclerView mRecyclerView;
    private MarsOnLoadMoreView mFooterView;

    /**
     * 是否还有更多
     */
    private boolean isLoadMoreEnable;

    private FooterViewWrapperAdapter mFooterViewWrapperAdapter;
    private RecyclerViewAdapterDataObserver mRecyclerViewAdapterDataObserver;

    private void initView(Context context, AttributeSet attrs) {
        mContext = context;
        mFooterView = new MarsNormalFooterView(mContext);
        if (attrs != null) {
            TypedArray attrsArray = context.obtainStyledAttributes(R.styleable.mars);
            isHaveListView = attrsArray.getBoolean(R.styleable.mars_isListView, false);
            ((MarsNormalFooterView) mFooterView).setAttributeSet(attrsArray);
        } else {
            isHaveListView = false;
        }
        mSwipeRefreshLayout = new SwipeRefreshLayout(mContext);
        FrameLayout.LayoutParams srfParams = new FrameLayout.LayoutParams(-1, -1);
        mSwipeRefreshLayout.setLayoutParams(srfParams);
        addView(mSwipeRefreshLayout);
        if (isHaveListView) {
            mListView = new ListView(context);
            SwipeRefreshLayout.LayoutParams lvParams = new SwipeRefreshLayout.LayoutParams(-1, -1);
            mListView.setLayoutParams(lvParams);
            mSwipeRefreshLayout.addView(mListView);
        } else {
            mRecyclerView = new RecyclerView(context);
            SwipeRefreshLayout.LayoutParams lvParams = new SwipeRefreshLayout.LayoutParams(-1, -1);
            mRecyclerView.setLayoutParams(lvParams);
            mSwipeRefreshLayout.addView(mRecyclerView);
        }
    }

    public ListView getListView() {
        return mListView;
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        if (adapter != null) {
            mRecyclerViewAdapterDataObserver = new RecyclerViewAdapterDataObserver();
            mFooterViewWrapperAdapter = new FooterViewWrapperAdapter(adapter);
            mRecyclerView.setAdapter(mFooterViewWrapperAdapter);
            adapter.registerAdapterDataObserver(mRecyclerViewAdapterDataObserver);
        }
    }

    /**
     * 设置加载状态
     *
     * @param refreshing
     */
    public void setRefreshing(boolean refreshing) {
        mSwipeRefreshLayout.setRefreshing(refreshing);
        mMarsOnLoadListener.onRefresh();
    }

    private MarsOnLoadListener mMarsOnLoadListener;

    public void setOnLoadListener(MarsOnLoadListener listener) {
        this.mMarsOnLoadListener = listener;
    }

    /**
     * 当发生错误时调用（网络/服务器宕机等）
     * <p>
     * onLoadMore里要 page--
     */
    public void onError() {
        mFooterView.onError();
        isLoadMoreEnable = false;
    }

    /**
     * 设置自己的加载更多View
     *
     * @param v
     */
    public void setMarsOnLoadMoreView(MarsOnLoadMoreView v) {
        mFooterView = v;
    }

    /**
     * 当数据发生改变的时候
     *
     * 调用onChanged方法
     */
    class RecyclerViewAdapterDataObserver extends RecyclerView.AdapterDataObserver {
        @Override
        public void onChanged() {
            super.onChanged();
        }
    }

    private class FooterViewWrapperAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_FOOTER = 0x100;

        private RecyclerView.Adapter<RecyclerView.ViewHolder> adapter;

        public FooterViewWrapperAdapter(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter){
            this.adapter = adapter;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_FOOTER) {
                return new FooterViewHolder(mFooterView);
            }
            return adapter.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            adapter.onBindViewHolder(holder, position);
        }

        @Override
        public int getItemCount() {
            return adapter.getItemCount() + 1;
        }

        public class FooterViewHolder extends RecyclerView.ViewHolder {
            public FooterViewHolder(View itemView) {
                super(itemView);
            }
        }
    }

}

package com.devin.refreshview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
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

    /**
     * 是否支持下拉刷新
     */
    private boolean isSupportRefresh = true;

    /**
     * 是否有FooterView
     */
    private boolean isHaveFooterView = true;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mListView;
    private RecyclerView mRecyclerView;
    private MarsOnLoadMoreView mFooterView;
    private LinearLayoutManager mLinearLayoutManager;

    /**
     * 是否有加载下一页的能力
     */
    private boolean isLoadMoreEnable = true;

    private boolean isComplete;

    private WrapperAdapter mWrapperAdapter;
    private RecyclerViewAdapterDataObserver mRecyclerViewAdapterDataObserver;

    private void initView(Context context, AttributeSet attrs) {
        mContext = context;
        if (attrs != null) {
            TypedArray attrsArray = context.obtainStyledAttributes(attrs, R.styleable.mars);
            isHaveListView = attrsArray.getBoolean(R.styleable.mars_isListView, false);
            isSupportRefresh = attrsArray.getBoolean(R.styleable.mars_isSupportRefresh, true);
            isHaveFooterView = attrsArray.getBoolean(R.styleable.mars_isHaveFooterView, true);
            if (isHaveFooterView) {
                mFooterView = new MarsNormalFooterView(mContext);
                ((MarsNormalFooterView) mFooterView).setAttributeSet(attrsArray);
            }
        } else {
            isSupportRefresh = true;
            isHaveListView = false;
            isHaveFooterView = true;
            mFooterView = new MarsNormalFooterView(mContext);
        }
        if (isSupportRefresh) {
            mSwipeRefreshLayout = new SwipeRefreshLayout(mContext);
            FrameLayout.LayoutParams srfParams = new FrameLayout.LayoutParams(-1, -1);
            mSwipeRefreshLayout.setLayoutParams(srfParams);
            addView(mSwipeRefreshLayout);
        }
        if (isHaveListView) {
            mListView = new ListView(context);
            if (isSupportRefresh) {
                SwipeRefreshLayout.LayoutParams lvParams = new SwipeRefreshLayout.LayoutParams(-1, -1);
                mListView.setLayoutParams(lvParams);
                mSwipeRefreshLayout.addView(mListView);
            } else {
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-1, -1);
                mListView.setLayoutParams(params);
                addView(mListView);
            }
        } else {
            mRecyclerView = new RecyclerView(context);
            if (isSupportRefresh) {
                SwipeRefreshLayout.LayoutParams lvParams = new SwipeRefreshLayout.LayoutParams(-1, -1);
                mRecyclerView.setLayoutParams(lvParams);
                mSwipeRefreshLayout.addView(mRecyclerView);
                mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        setRefreshing(true);
                    }
                });
                mRecyclerView.addOnScrollListener(new MarsOnScrollListener());
            } else {
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-1, -1);
                mRecyclerView.setLayoutParams(params);
                addView(mRecyclerView);
            }
        }
    }

    public ListView getListView() {
        return mListView;
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public void setLinearLayoutManager() {
        mLinearLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
    }

    public LinearLayoutManager getLinearLayoutManager() {
        return mLinearLayoutManager;
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        if (adapter != null) {
            mRecyclerViewAdapterDataObserver = new RecyclerViewAdapterDataObserver();
            mWrapperAdapter = new WrapperAdapter(adapter);
            mRecyclerView.setAdapter(mWrapperAdapter);
            adapter.registerAdapterDataObserver(mRecyclerViewAdapterDataObserver);
        }
    }

    /**
     * 设置加载状态
     *
     * @param refreshing
     */
    public void setRefreshing(boolean refreshing) {
        if (!isSupportRefresh) {
            throw new RuntimeException("如果使用下拉刷新，请设置isSupportRefresh为true");
        }
        mSwipeRefreshLayout.setRefreshing(refreshing);
        if (refreshing && mMarsOnLoadListener != null) {
            isComplete = false;
            mMarsOnLoadListener.onRefresh();
        }
    }

    private View mEmptyView;

    /**
     * 设置数据为空的布局
     *
     * @param v
     */
    public void setEmptyView(View v) {
        if (v == null) {
            throw new RuntimeException("EmptyView 为 Null");
        }
        mEmptyView = v;
        v.setVisibility(View.GONE);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-1, -1);
        v.setLayoutParams(params);
        addView(v);
    }

    /**
     * 显示空的布局
     */
    public void showEmptyView() {
        mEmptyView.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏空的布局
     */
    public void hideEmptyView() {
        mEmptyView.setVisibility(View.GONE);
    }

    /**
     * pageSize默认值为 10
     */
    private int pageSize = 10;

    /**
     * 设置PageSize
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * 是否正在刷新
     *
     * @return
     */
    private boolean isRefreshing() {
        return mSwipeRefreshLayout.isRefreshing();
    }

    /**
     * 设置刷新时 转圈的颜色
     *
     * @param colors
     */
    public void setColorSchemeColors(@ColorInt int... colors) {
        mSwipeRefreshLayout.setColorSchemeColors(colors);
    }

    private MarsOnLoadListener mMarsOnLoadListener;

    public void setMarsOnLoadListener(MarsOnLoadListener listener) {
        this.mMarsOnLoadListener = listener;
    }

    /**
     * 当发生错误时调用（网络/服务器宕机等）
     * <p>
     * onLoadMore里要 page--
     */
    public void onError() {
        mFooterView.onErrorStyle();
    }

    /**
     * 当加载结束时
     */
    public void onComplete() {
        isComplete = true;
        mFooterView.onCompleteStyle();
    }

    private View mHeaderView;

    public void addHeaderView(View v) {
        mHeaderView = v;
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
            mWrapperAdapter.notifyDataSetChanged();
        }
    }

    private class MarsOnScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            Log.d("MarsOnScrollListener", ">>>>>dx: " + dx + " , dy: " + dy);
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (isHaveFooterView && newState == RecyclerView.SCROLL_STATE_IDLE) {
                RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
                if (manager instanceof LinearLayoutManager) {
                    int lastVisibleItemPosition = ((LinearLayoutManager) manager).findLastVisibleItemPosition();
                    Log.d("MarsOnScrollListener", ">>>>>lastVisibleItemPosition: " + lastVisibleItemPosition);
                    if (lastVisibleItemPosition == recyclerView.getAdapter().getItemCount() - 1) {
                        if (isComplete) {
                            return;
                        }
                        if (lastVisibleItemPosition - (mHeaderView != null ? 1 : 0) != 0
                                && (lastVisibleItemPosition - (mHeaderView != null ? 1 : 0)) % pageSize == 0) {
                            isLoadMoreEnable = true;
                            mFooterView.onLoadingStyle();
                        } else {
                            isLoadMoreEnable = false;
                            mFooterView.onCompleteStyle();
                        }
                        if (isLoadMoreEnable && mMarsOnLoadListener != null) {
                            mMarsOnLoadListener.onLoadMore();
                        }
                    }
                }
            }
        }
    }

    private class WrapperAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_FOOTER = 0x100;

        private static final int TYPE_HEADER = 0x101;

        private RecyclerView.Adapter<RecyclerView.ViewHolder> adapter;

        public WrapperAdapter(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter){
            this.adapter = adapter;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.d("WrapperAdapter", ">>>>>onCreateViewHolder: " + viewType);
            if (viewType == TYPE_HEADER) {
                return new HeaderViewHolder(mHeaderView);
            } else if (viewType == TYPE_FOOTER) {
                return new FooterViewHolder(mFooterView);
            }
            return adapter.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            Log.d("WrapperAdapter", ">>>>>onBindViewHolder: " + position + ", viewType: " + getItemViewType(position));
            if (getItemViewType(position) == TYPE_FOOTER) {
            } else if (getItemViewType(position) == TYPE_HEADER) {
            } else {
                adapter.onBindViewHolder(holder, mHeaderView != null ? position - 1 : position);
            }
        }

        @Override
        public int getItemViewType(int position) {
            Log.d("WrapperAdapter", ">>>>>getItemViewType: " + position);
            if (mHeaderView != null && position == 0) {
                return TYPE_HEADER;
            } else if (mFooterView != null && position == adapter.getItemCount() + (mHeaderView != null ? 1 : 0)) {
                return TYPE_FOOTER;
            }
            return super.getItemViewType(position);
        }

        @Override
        public int getItemCount() {
            int count;
            if (mHeaderView == null && mFooterView == null) {
                count = adapter.getItemCount();
            } else if (mHeaderView != null && mFooterView != null) {
                count = adapter.getItemCount() + 2;
            } else {
                count = adapter.getItemCount() + 1;
            }
            return count;
        }

        public class HeaderViewHolder extends RecyclerView.ViewHolder {
            public HeaderViewHolder(View itemView) {
                super(itemView);
            }
        }

        public class FooterViewHolder extends RecyclerView.ViewHolder {
            public FooterViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}

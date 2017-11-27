package com.devin.refreshview;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
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

    public MarsRefreshView setLinearLayoutManager() {
        mLinearLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        return this;
    }

    public LinearLayoutManager getLinearLayoutManager() {
        return mLinearLayoutManager;
    }

    public MarsRefreshView setAdapter(RecyclerView.Adapter adapter) {
        if (adapter != null) {
            mRecyclerViewAdapterDataObserver = new RecyclerViewAdapterDataObserver();
            mWrapperAdapter = new WrapperAdapter(adapter);
            mRecyclerView.setAdapter(mWrapperAdapter);
            adapter.registerAdapterDataObserver(mRecyclerViewAdapterDataObserver);
        }
        return this;
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
        if (mMarsOnLoadListener != null && mVenusOnLoadListener != null) {
            throw new RuntimeException("请设置一个回调接口");
        }
        mSwipeRefreshLayout.setRefreshing(refreshing);
        if (refreshing) {
            isComplete = false;
            mRecyclerView.setTag(R.id.pre_load_more, null);
            if (mMarsOnLoadListener != null) {
                mMarsOnLoadListener.onRefresh();
            }
            if (mVenusOnLoadListener != null) {
                indexPage = storeIndexPage;
                mVenusOnLoadListener.onRefresh(storeIndexPage);
            }
        }
    }
    private View mEmptyView;

    /**
     * 设置数据为空的布局
     *
     * @param v
     */
    public MarsRefreshView setEmptyView(View v) {
        if (v == null) {
            throw new RuntimeException("EmptyView 为 Null");
        }
        mEmptyView = v;
        v.setVisibility(View.GONE);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-1, -1);
        v.setLayoutParams(params);
        addView(v);
        return this;
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
    public MarsRefreshView setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
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
    public MarsRefreshView setColorSchemeColors(@ColorInt int... colors) {
        mSwipeRefreshLayout.setColorSchemeColors(colors);
        return this;
    }

    private MarsOnLoadListener mMarsOnLoadListener;

    public MarsRefreshView setMarsOnLoadListener(MarsOnLoadListener listener) {
        this.mMarsOnLoadListener = listener;
        return this;
    }

    private VenusOnLoadListener mVenusOnLoadListener;

    /**
     * 起始页码
     */
    private int indexPage;

    /**
     * 储存起始页码
     */
    private int storeIndexPage;

    /**
     * @param indexPage 起始页码
     * @param v         回调
     */
    public MarsRefreshView setVenusOnLoadListener(int indexPage, VenusOnLoadListener v) {
        this.indexPage = indexPage;
        storeIndexPage = indexPage;
        mVenusOnLoadListener = v;
        return this;
    }

    /**
     * 当发生错误时调用（网络/服务器宕机等）
     * <p>
     * onLoadMore里要 page--
     */
    public void onError() {
        // 先这么解决
        mPreLoadMoreEnable = false;
        mFooterView.onErrorStyle();
        if (mVenusOnLoadListener != null) {
            indexPage--;
        }
    }

    /**
     * 当加载结束时
     */
    public void onComplete() {
        isComplete = true;
        mFooterView.onCompleteStyle();
    }

    private View mHeaderView;

    public MarsRefreshView addHeaderView(View v) {
        mHeaderView = v;
        return this;
    }

    /**
     * 设置自己的加载更多View
     *
     * @param v
     */
    public MarsRefreshView setMarsOnLoadMoreView(MarsOnLoadMoreView v) {
        mFooterView = v;
        return this;
    }

    private boolean mPreLoadMoreEnable;

    /**
     * 偏移量（在倒数第几开始加载）
     */
    private int offset;

    public MarsRefreshView setPreLoadMoreEnable(boolean preLoadMoreEnable) {
        mPreLoadMoreEnable = preLoadMoreEnable;
        offset = 5;
        return this;
    }

    public MarsRefreshView setPreLoadMoreEnable(int offset) {
        mPreLoadMoreEnable = true;
        this.offset = offset;
        return this;
    }

    private boolean pageSizeEnable = true;

    public MarsRefreshView setPageSizeEnable(boolean pageSizeEnable) {
        this.pageSizeEnable = pageSizeEnable;
        return this;
    }

    public static Handler mHandler = new Handler(Looper.getMainLooper());

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
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    onLoadMore();
                }
            }, 100);
        }
    }

    private class PreLoadMoreInfo {
        int loadPosition;
        int lastVisiblePosition;
    }

    private class MarsOnScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (mPreLoadMoreEnable && dy > 0) {
                onPreOnLoadMore(recyclerView);
            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (!mPreLoadMoreEnable && isHaveFooterView && newState == RecyclerView.SCROLL_STATE_IDLE) {
                onLoadMore();
            }
        }
    }

    private void onPreOnLoadMore(RecyclerView recyclerView) {
        int lastVisiblePosition = mLinearLayoutManager.findLastVisibleItemPosition();
        int loadPosition = mRecyclerView.getAdapter().getItemCount() - 1 - offset;
        Log.d("onPreLoadMore", ">>>>>onScrolled: " + lastVisiblePosition + ",loadPosition: " + loadPosition);
        PreLoadMoreInfo preLoadMoreInfo = (PreLoadMoreInfo) recyclerView.getTag(R.id.pre_load_more);
        if (preLoadMoreInfo != null) {
            if (preLoadMoreInfo.lastVisiblePosition == lastVisiblePosition
                    || preLoadMoreInfo.loadPosition == loadPosition) {
                return;
            }
        }
        if (lastVisiblePosition >= loadPosition) {
            if (isComplete) {
                return;
            }
            if (pageSizeEnable && (mRecyclerView.getAdapter().getItemCount() - 1) % pageSize == 0) {
                isLoadMoreEnable = false;
                mFooterView.onCompleteStyle();
                return;
            }
            mFooterView.onLoadingStyle();
            Log.d("onPreLoadMore", "have been onPreLoaded，lastVisiblePosition: " + lastVisiblePosition);
            if (mMarsOnLoadListener != null) {
                mMarsOnLoadListener.onLoadMore();
            }
            if (mVenusOnLoadListener != null) {
                indexPage++;
                mVenusOnLoadListener.onLoadMore(indexPage);
            }
            PreLoadMoreInfo info = new PreLoadMoreInfo();
            info.lastVisiblePosition = lastVisiblePosition;
            info.loadPosition = loadPosition;
            recyclerView.setTag(R.id.pre_load_more, info);
        }
    }

    /**
     * 判定是否开始加载更多
     */
    private void onLoadMore() {
        int lastVisibleItemPosition = mLinearLayoutManager.findLastVisibleItemPosition();
        Log.d("onLoadMore", ">>>>>lastVisibleItemPosition: " + lastVisibleItemPosition);
        if (lastVisibleItemPosition == mRecyclerView.getAdapter().getItemCount() - 1) {
            if (isComplete) {
                return;
            }
            if (pageSizeEnable && lastVisibleItemPosition - (mHeaderView != null ? 1 : 0) != 0
                    && (lastVisibleItemPosition - (mHeaderView != null ? 1 : 0)) % pageSize == 0) {
                isLoadMoreEnable = true;
                mFooterView.onLoadingStyle();
            } else if (pageSizeEnable) {
                isLoadMoreEnable = false;
                mFooterView.onCompleteStyle();
            } else if (!pageSizeEnable) {
                mFooterView.onLoadingStyle();
                isLoadMoreEnable = true;
            }
            if (isLoadMoreEnable && mMarsOnLoadListener != null) {
                mMarsOnLoadListener.onLoadMore();
            }
            if (isLoadMoreEnable && mVenusOnLoadListener != null) {
                indexPage++;
                mVenusOnLoadListener.onLoadMore(indexPage);
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
            return adapter.getItemViewType(position - (mHeaderView != null ? 1 : 0));
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

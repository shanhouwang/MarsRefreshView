package com.devin.refreshview;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
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
    private boolean isListView;

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
    private RecyclerView.Adapter mAdapter;

    private void initView(Context context, AttributeSet attrs) {
        mContext = context;
        if (attrs != null) {
            TypedArray attrsArray = context.obtainStyledAttributes(attrs, R.styleable.mars);
            isListView = attrsArray.getBoolean(R.styleable.mars_isListView, false);
            isSupportRefresh = attrsArray.getBoolean(R.styleable.mars_isSupportRefresh, true);
            isHaveFooterView = attrsArray.getBoolean(R.styleable.mars_isHaveFooterView, true);
            if (isHaveFooterView) {
                mFooterView = new MarsNormalFooterView(mContext);
                ((MarsNormalFooterView) mFooterView).setAttributeSet(attrsArray);
            }
        } else {
            isSupportRefresh = true;
            isListView = false;
            isHaveFooterView = true;
            mFooterView = new MarsNormalFooterView(mContext);
        }
        if (isSupportRefresh) {
            mSwipeRefreshLayout = new SwipeRefreshLayout(mContext);
            LayoutParams srfParams = new LayoutParams(-1, -1);
            mSwipeRefreshLayout.setLayoutParams(srfParams);
            addView(mSwipeRefreshLayout);
        }
        if (isListView) {
            mListView = new ListView(context);
            mListView.setDivider(null);
            mListView.setDividerHeight(0);
            if (isSupportRefresh) {
                SwipeRefreshLayout.LayoutParams lvParams = new SwipeRefreshLayout.LayoutParams(-1, -1);
                mListView.setLayoutParams(lvParams);
                mSwipeRefreshLayout.addView(mListView);
            } else {
                LayoutParams params = new LayoutParams(-1, -1);
                mListView.setLayoutParams(params);
                addView(mListView);
            }
            if (isHaveFooterView) {
                AbsListView.LayoutParams params = new AbsListView.LayoutParams(-1, -2);
                mFooterView.setLayoutParams(params);
                mListView.addFooterView(mFooterView);
            }
            mListView.setOnScrollListener(new ListViewOnScrollListener());
        } else {
            mRecyclerView = (RecyclerView) LayoutInflater.from(getContext()).inflate(R.layout.layout_recycler_view, null);
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
            } else {
                LayoutParams params = new LayoutParams(-1, -1);
                mRecyclerView.setLayoutParams(params);
                addView(mRecyclerView);
            }
            mRecyclerView.addOnScrollListener(new MarsOnScrollListener());
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

    /**
     * 设置RecyclerView适配器
     *
     * @param adapter
     * @return
     */
    public MarsRefreshView setAdapter(RecyclerView.Adapter adapter) {
        if (adapter != null) {
            mAdapter = adapter;
        }
        return this;
    }

    private BaseAdapter mListViewAdapter;

    /**
     * 设置ListView适配器
     *
     * @param adapter
     * @return
     */
    public MarsRefreshView setAdapter(BaseAdapter adapter) {
        mListViewAdapter = adapter;
        mListView.setAdapter(mListViewAdapter);
        adapter.registerDataSetObserver(new ListViewDataSetObserver());
        return this;
    }

    class ListViewDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            MLog.d("onChanged", ">>>>>mListViewAdapter getItemCount: " + mListViewAdapter.getCount());
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    onListViewLoadMore();
                }
            }, 100);
            if (mListViewAdapter.getCount() == 0) {
                showEmptyView(heightMode);
            } else {
                hideEmptyView();
            }
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
    private View mErrorView;
    private boolean headerViewVisibleByEmptyView;
    private boolean headerViewVisibleByErrorView;
    private boolean mInterceptClickEventByErrorView;
    private LinearLayout mHeaderAndOtherTipViewsContainer;
    private FrameLayout mOtherTipViewsContainer;

    /**
     * 设置数据为空的布局
     *
     * @param v
     * @param isShowHeaderView
     * @return
     */
    public MarsRefreshView setEmptyView(View v, boolean isShowHeaderView) {
        if (v == null) {
            throw new RuntimeException("EmptyView 为 Null");
        }
        this.mEmptyView = v;
        this.headerViewVisibleByEmptyView = isShowHeaderView;
        return this;
    }

    /**
     * 设置数据为空的布局
     *
     * @param v                   Error View 视图
     * @param headerViewVisible   显示 Error View 的时候 是否显示 Header View
     * @param interceptClickEvent 是否拦截Error View 的点击事件 （如果是 true 表示 点击了Error View 之后 主动拉取首页数据）
     * @return this
     */
    public MarsRefreshView setErrorView(View v, boolean headerViewVisible, boolean interceptClickEvent) {
        if (v == null) {
            throw new RuntimeException("ErrorView 为 Null");
        }
        this.mErrorView = v;
        this.headerViewVisibleByErrorView = headerViewVisible;
        this.mInterceptClickEventByErrorView = interceptClickEvent;
        return this;
    }

    private LinearLayout initHeaderAndOtherTipViewsContainer() {
        if (mHeaderAndOtherTipViewsContainer == null) {
            mHeaderAndOtherTipViewsContainer = new LinearLayout(mContext);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(-1, -2);
            mHeaderAndOtherTipViewsContainer.setLayoutParams(params);
            mHeaderAndOtherTipViewsContainer.setOrientation(LinearLayout.VERTICAL);
        }
        return mHeaderAndOtherTipViewsContainer;
    }

    /**
     * 显示空的布局
     */
    public void showEmptyView() {
        showEmptyView(-1);
    }

    private int heightMode = -1;

    /**
     * 显示空的布局
     *
     * @param heightMode -1 LayoutParams.MATCH_PARENT
     *                   -2 LayoutParams.WRAP_CONTENT
     *                   可赋值dp
     */
    public void showEmptyView(int heightMode) {
        this.heightMode = heightMode;
        if (headerViewVisibleByEmptyView) {
            ViewGroup.LayoutParams params = mHeaderAndOtherTipViewsContainer.getLayoutParams();
            if (heightMode < 0) {
                params.height = heightMode;
            } else {
                params.height = MeasureUtils.dp2px(mContext, heightMode);
            }
            mHeaderAndOtherTipViewsContainer.setLayoutParams(params);
        }
        if (mEmptyView != null && mEmptyView.getVisibility() == View.GONE) {
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏空的布局
     */
    public void hideEmptyView() {
        if (mEmptyView != null && mEmptyView.getVisibility() == View.VISIBLE) {
            mEmptyView.setVisibility(View.GONE);
        }
    }

    public void showErrorView() {
        if (mErrorView != null && mErrorView.getVisibility() == View.GONE) {
            mErrorView.setVisibility(View.VISIBLE);
        }
    }

    public void hideErrorView() {
        if (mErrorView != null && mErrorView.getVisibility() == View.VISIBLE) {
            mErrorView.setVisibility(View.GONE);
        }
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

    private MercuryOnLoadMoreListener mMercuryOnLoadMoreListener;

    public MarsRefreshView setMercuryOnLoadMoreListener(int indexPage, MercuryOnLoadMoreListener m) {
        this.indexPage = indexPage;
        storeIndexPage = indexPage;
        mMercuryOnLoadMoreListener = m;
        return this;
    }

    /**
     * 当发生错误时调用（网络/服务器宕机等）
     * <p>
     * onLoadMore里要 page--
     */
    public void onError() {
        if (null != mSwipeRefreshLayout) mSwipeRefreshLayout.setRefreshing(false);
        mPreLoadMoreEnable = false;
        if (mVenusOnLoadListener != null) indexPage--;
        // 第一页数据都没有加载出来 要显示 Error View
        if (mAdapter.getItemCount() == 0) {
            showErrorView();
        } else {
            if (mFooterView != null) mFooterView.onErrorStyle();
        }
    }

    /**
     * 当加载结束时
     */
    public void onComplete() {
        if (null != mSwipeRefreshLayout) mSwipeRefreshLayout.setRefreshing(false);
        isComplete = true;
        if (mFooterView != null) mFooterView.onCompleteStyle();
    }

    private View mHeaderView;

    public MarsRefreshView addHeaderView(View v) {
        this.mHeaderView = v;
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

    public void build() {
        initHeaderAndOtherTipViewsContainer();
        if (null != this.mHeaderView) {
            if (this.mHeaderView.getLayoutParams() == null) {
                this.mHeaderView.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
            }
            if (this.isListView) {
                this.mListView.addHeaderView(mHeaderAndOtherTipViewsContainer);
            }
            this.mHeaderAndOtherTipViewsContainer.addView(mHeaderView);
        }
        if (null != this.mErrorView || null != this.mEmptyView) {
            initTipViewsLayout();
            if (null != this.mEmptyView) {
                if (this.headerViewVisibleByEmptyView && null != this.mHeaderView) {
                    this.mOtherTipViewsContainer.addView(this.mEmptyView);
                } else {
                    LayoutParams params = new LayoutParams(-1, -1);
                    this.mEmptyView.setLayoutParams(params);
                    addView(this.mEmptyView);
                }
            }
            if (null != this.mErrorView) {
                if (this.headerViewVisibleByErrorView && null != this.mHeaderView) {
                    this.mOtherTipViewsContainer.addView(this.mErrorView);
                } else {
                    LayoutParams params = new LayoutParams(-1, -1);
                    this.mErrorView.setLayoutParams(params);
                    addView(this.mErrorView);
                }
                if (this.mInterceptClickEventByErrorView) {
                    this.mErrorView.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            setRefreshing(true);
                        }
                    });
                }
                hideErrorView();
            }
        }

        // Adapter 相关
        mRecyclerViewAdapterDataObserver = new RecyclerViewAdapterDataObserver();
        mWrapperAdapter = new WrapperAdapter(this.mAdapter);
        mRecyclerView.setAdapter(mWrapperAdapter);
        this.mAdapter.registerAdapterDataObserver(mRecyclerViewAdapterDataObserver);
        isComplete = false;
    }

    private FrameLayout initTipViewsLayout() {
        if (null != this.mOtherTipViewsContainer) {
            return this.mOtherTipViewsContainer;
        }
        FrameLayout tipViewsLayout = new FrameLayout(mContext);
        tipViewsLayout.setId(R.id.layout_tip_views_container);
        tipViewsLayout.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        this.mHeaderAndOtherTipViewsContainer.addView(tipViewsLayout);
        this.mOtherTipViewsContainer = tipViewsLayout;
        return this.mOtherTipViewsContainer;
    }

    public static Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * 当数据发生改变的时候
     * <p>
     * 调用onChanged方法
     */
    class RecyclerViewAdapterDataObserver extends RecyclerView.AdapterDataObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            MLog.d("onChanged", ">>>>>mAdapter getItemCount: " + mAdapter.getItemCount());
            mWrapperAdapter.notifyDataSetChanged();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    onLoadMore();
                }
            }, 100);
            if (mAdapter.getItemCount() == 0) {
                showEmptyView(heightMode);
            } else {
                hideEmptyView();
                hideErrorView();
            }
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            MLog.d("onChanged", ">>>>>onItemRangeChanged<<<<<");
            mWrapperAdapter.notifyItemRangeChanged(positionStart + (mHeaderView != null ? 1 : 0), itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            MLog.d("onChanged", ">>>>>onItemRangeChanged<<<<<");
            mWrapperAdapter.notifyItemRangeChanged(positionStart + (mHeaderView != null ? 1 : 0), itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            MLog.d("onChanged", ">>>>>onItemRangeInserted<<<<<");
            mWrapperAdapter.notifyItemRangeInserted(positionStart + (mHeaderView != null ? 1 : 0), itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            MLog.d("onChanged", ">>>>>onItemRangeRemoved<<<<<");
            mWrapperAdapter.notifyItemRangeRemoved(positionStart + (mHeaderView != null ? 1 : 0), itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            MLog.d("onChanged", ">>>>>onItemRangeMoved<<<<<");
            mWrapperAdapter.notifyItemMoved(fromPosition + (mHeaderView != null ? 1 : 0), toPosition + (mHeaderView != null ? 1 : 0));
        }
    }

    private class PreLoadMoreInfo {
        int loadPosition;
        int lastVisiblePosition;
    }

    private class ListViewOnScrollListener implements AbsListView.OnScrollListener {

        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {
            if (!mPreLoadMoreEnable && i == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                onListViewLoadMore();
            }
        }

        @Override
        public void onScroll(AbsListView absListView, int i, int i1, int i2) {
        }
    }

    private void onListViewLoadMore() {
        int lastVisibleItemPosition = mListView.getLastVisiblePosition();
        MLog.d("onLoadMore", ">>>>>lastVisibleItemPosition: " + lastVisibleItemPosition);
        if (lastVisibleItemPosition == mListView.getAdapter().getCount() - 1) {
            if (isComplete) {
                return;
            }
            // 如果列表没有数据
            if (lastVisibleItemPosition - mListView.getHeaderViewsCount() == 0) {
                if (mFooterView != null) mFooterView.setVisibility(View.GONE);
                return;
            } else {
                if (mFooterView != null) mFooterView.setVisibility(View.VISIBLE);
            }
            if (pageSizeEnable) {
                if ((lastVisibleItemPosition - mListView.getHeaderViewsCount()) % pageSize == 0) {
                    isLoadMoreEnable = true;
                    if (mFooterView != null) mFooterView.onLoadingStyle();
                } else {
                    isLoadMoreEnable = false;
                    if (mFooterView != null) mFooterView.onCompleteStyle();
                }
            } else {
                if (mFooterView != null) mFooterView.onLoadingStyle();
                isLoadMoreEnable = true;
            }
            if (isLoadMoreEnable && mMarsOnLoadListener != null) {
                mMarsOnLoadListener.onLoadMore();
            }
            if (isLoadMoreEnable && mVenusOnLoadListener != null) {
                indexPage++;
                mVenusOnLoadListener.onLoadMore(indexPage);
            }
            if (isLoadMoreEnable && mMercuryOnLoadMoreListener != null) {
                indexPage++;
                mMercuryOnLoadMoreListener.onLoadMore(indexPage);
            }
        }
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
        int loadPosition = mRecyclerView.getAdapter().getItemCount() - offset - 1 - (mHeaderView == null ? 0 : 1);
        MLog.d("onPreLoadMore", ">>>>>onScrolled: " + lastVisiblePosition + ",loadPosition: " + loadPosition);
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
            if (pageSizeEnable) {
                if ((mRecyclerView.getAdapter().getItemCount() - 1 - (mHeaderView == null ? 0 : 1)) % pageSize == 0) {
                    isLoadMoreEnable = true;
                    if (mFooterView != null) mFooterView.onLoadingStyle();
                } else {
                    isLoadMoreEnable = false;
                    if (mFooterView != null) mFooterView.onCompleteStyle();
                }
            } else {
                if (mFooterView != null) mFooterView.onLoadingStyle();
                isLoadMoreEnable = true;
            }
            if (isLoadMoreEnable && mMarsOnLoadListener != null) {
                mMarsOnLoadListener.onLoadMore();
            }
            if (isLoadMoreEnable && mVenusOnLoadListener != null) {
                indexPage++;
                mVenusOnLoadListener.onLoadMore(indexPage);
            }
            if (isLoadMoreEnable && mMercuryOnLoadMoreListener != null) {
                indexPage++;
                mMercuryOnLoadMoreListener.onLoadMore(indexPage);
            }
            PreLoadMoreInfo info = new PreLoadMoreInfo();
            info.lastVisiblePosition = lastVisiblePosition;
            info.loadPosition = loadPosition;
            recyclerView.setTag(R.id.pre_load_more, info);
            MLog.d("onPreLoadMore", "have been onPreLoaded，lastVisiblePosition: " + lastVisiblePosition);
        }
    }

    /**
     * 判定是否开始加载更多
     */
    private void onLoadMore() {
        int lastVisibleItemPosition = mLinearLayoutManager.findLastVisibleItemPosition();
        MLog.d("onLoadMore", ">>>>>lastVisibleItemPosition: " + lastVisibleItemPosition);
        if (lastVisibleItemPosition == mRecyclerView.getAdapter().getItemCount() - 1) {
            if (isComplete) {
                return;
            }
            // 如果列表没有数据
            if (lastVisibleItemPosition - (mHeaderView != null ? 1 : 0) == 0) {
                if (mFooterView != null) mFooterView.setVisibility(View.GONE);
                return;
            } else {
                if (mFooterView != null) mFooterView.setVisibility(View.VISIBLE);
            }
            if (pageSizeEnable) {
                if ((lastVisibleItemPosition - (mHeaderView != null ? 1 : 0)) % pageSize == 0) {
                    isLoadMoreEnable = true;
                    if (mFooterView != null) mFooterView.onLoadingStyle();
                } else {
                    isLoadMoreEnable = false;
                    if (mFooterView != null) mFooterView.onCompleteStyle();
                }
            } else {
                if (mFooterView != null) mFooterView.onLoadingStyle();
                isLoadMoreEnable = true;
            }
            if (isLoadMoreEnable && mMarsOnLoadListener != null) {
                mMarsOnLoadListener.onLoadMore();
            }
            if (isLoadMoreEnable && mVenusOnLoadListener != null) {
                indexPage++;
                mVenusOnLoadListener.onLoadMore(indexPage);
            }
            if (isLoadMoreEnable && mMercuryOnLoadMoreListener != null) {
                indexPage++;
                mMercuryOnLoadMoreListener.onLoadMore(indexPage);
            }
        }
    }

    private class WrapperAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_FOOTER = 0x100;

        private static final int TYPE_HEADER = 0x101;

        private RecyclerView.Adapter<RecyclerView.ViewHolder> adapter;

        public WrapperAdapter(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
            this.adapter = adapter;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MLog.d("WrapperAdapter", ">>>>>onCreateViewHolder: " + viewType);
            if (viewType == TYPE_HEADER) {
                return new ViewHolder(headerViewVisibleByEmptyView ? mHeaderAndOtherTipViewsContainer : mHeaderView);
            } else if (viewType == TYPE_FOOTER) {
                return new ViewHolder(mFooterView);
            }
            return adapter.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            MLog.d("WrapperAdapter", ">>>>>onBindViewHolder: " + position + ", viewType: " + getItemViewType(position));
            if (getItemViewType(position) == TYPE_FOOTER) {
            } else if (getItemViewType(position) == TYPE_HEADER) {
            } else {
                adapter.onBindViewHolder(holder, mHeaderView != null ? position - 1 : position);
            }
        }

        @Override
        public int getItemViewType(int position) {
            MLog.d("WrapperAdapter", ">>>>>getItemViewType: " + position);
            if (mHeaderView != null && position == 0) {
                return TYPE_HEADER;
            } else if (mFooterView != null && position == adapter.getItemCount() + (mHeaderView != null ? 1 : 0)) {
                return TYPE_FOOTER;
            }
            return adapter.getItemViewType(mHeaderView != null ? position - 1 : position);
        }

        @Override
        public int getItemCount() {
            int count = adapter.getItemCount();
            if (mHeaderView != null) {
                count++;
            }
            if (mFooterView != null) {
                count++;
            }
            return count;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}

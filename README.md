# MarsRefreshView
## 添加依赖

```
repositories {
    jcenter()
}
compile ('com.devin:mars-refresh:0.0.3-alpha-5')
```
## 如何使用
#### 1、XML布局
```
<com.devin.refreshview.MarsRefreshView
    android:id="@+id/marsRefreshView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/colorPrimary" />
```
实例代码

```
mMarsRefreshView.setLinearLayoutManager()
                .setAdapter(mAdapter)
                .addHeaderView(v)
                .setPreLoadMoreEnable(true)
                .setPageSizeEnable(false)
                .setMarsOnLoadListener(new MarsOnLoadListener() {
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
                                for (int i = 0; i < 10; i++) {
                                    data.add("onRefresh: " + i);
                                }
                                return null;
                            }
                        }, 1 * 1000, TimeUnit.MILLISECONDS);
                    }

                    @Override
                    public void onLoadMore() {
                        ThreadUtils.get(ThreadUtils.Type.SCHEDULED).schedule(new ThreadUtils.TpRunnable() {
                            @Override
                            public Object execute() {
                                page++;
                                if (page <= 10) {
                                    Log.d("MainActivity", ">>>>>onLoadMore, page: " + page);
                                    for (int i = 0; i < 10; i++) {
                                        data.add("onLoadMore: " + i + ", page: " + page);
                                    }
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
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
        mMarsRefreshView.setRefreshing(true);
```
#### 2、自定义属性
* namespace：xmlns:mars="http://schemas.android.com/apk/res-auto"
* 自定义属性 isHaveFooterView 类型 Boolean 是否添加加载更多footerView 默认添加
* 自定义属性 isSupportRefresh 类型 Boolean 是支持下拉刷新 默认支持
* 自定义属性 footerTextVisible 类型 Boolean 是否显示footerView
* 自定义属性 footerTextColor 类型 color 默认颜色#aaaaaa
* 自定义属性 onLoadingText 类型 String（正在加载时的文案 默认文案是 疯狂加载中）
* 自定义属性 onLoadingErrorText 类型 String（加载错误时的文案 默认文案是 上拉加载更多）
* 自定义属性 onLoadingCompleteText 类型 String（加载完成时的文案 默认文案是 疯狂加载中）
* 自定义属性 footerTextSize 类型 Integer（默认 13dp）

#### 3、网络耗时操作中如何使用

```
mMarsRefreshView.setMarsOnLoadListener(new MarsOnLoadListener() {
    @Override
    public void onRefresh() {
        // 下拉刷新网络耗时操作
        ...
        // 下拉刷新网络耗时操作结束时
        mMarsRefreshView.setRefreshing(false);
    }

    @Override
    public void onLoadMore() {
        // 上拉加载更多网络耗时操作
        如果 已经加载完所有的数据需要设置
        mMarsRefreshView.onComplete();
        如果 手机无网络 或者 访问不到服务器 httpCode不为200的情况下，要设置
        mMarsRefreshView.onError();
        // 此时还要记得记得设置 page--
    }
});
// 注意 setMarsOnLoadListener 在 setRefreshing(true)之上
mMarsRefreshView.setRefreshing(true);
```
#### 4、可addHeaderView

```
mMarsRefreshView.addHeaderView(v);
```
#### 5、可设置数据为空的时候的View
```
// 设置数据为空的时候的View
mMarsRefreshView.setEmptyView(v);
// 显示数据为空View
mMarsRefreshView.showEmptyView();
// 隐藏数据为空View
mMarsRefreshView.hideEmptyView();
```
#### 6、设置每页加载数据多少条默认10条
```
mMarsRefreshView.setPageSize(int pageSize);
```
#### 7、预加载设置
```
// 开启 默认在倒数第5个位置开始加载下一页数据
mMarsRefreshView.setPreLoadMoreEnable(true);

// 设置自己的Offset
mMarsRefreshView.setPreLoadMoreEnable(offset);
```
## 混淆配置

```
-dontwarn com.devin.refreshview.**
-keep class com.devin.refreshview.** {*;}
```



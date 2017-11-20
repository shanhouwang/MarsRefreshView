# MarsRefreshView
## 添加依赖

```
repositories {
    jcenter()
}
compile ('com.devin:mars-refresh:0.0.3-alpha-1')
```
## 如何使用
#### 1、XML布局
```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:mars="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <com.devin.refreshview.MarsRefreshView
        android:id="@+id/marsRefreshView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@color/colorPrimary" />
</LinearLayout>
```
#### 2、自定义属性
* 默认添加 footerView 的配置选项
* Namespace：xmlns:mars="http://schemas.android.com/apk/res-auto"
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
## 混淆配置

```
-dontwarn com.devin.refreshview.**
-keep class com.devin.refreshview.** {*;}
```



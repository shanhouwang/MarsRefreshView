package com.devin.refreshview;

import android.content.Context;
import android.widget.FrameLayout;

/**
 * Created by Devin on 2017/11/14.
 *
 * @author Devin
 */

public abstract class MarsOnLoadMoreView extends FrameLayout {

    public MarsOnLoadMoreView(Context context) {
        super(context);
    }

    /**
     * 结束的样式
     */
    public abstract void onCompleteStyle();

    /**
     * 正在加载中的样式
     */
    public abstract void onLoadingStyle();

    /**
     * 出现错误的样式
     */
    public abstract void onErrorStyle();
}

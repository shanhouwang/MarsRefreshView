package com.devin.refreshview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Devin on 2017/11/14.
 */

public abstract class MarsOnLoadMoreView extends View {

    public MarsOnLoadMoreView(Context context) {
        super(context);
    }

    public MarsOnLoadMoreView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MarsOnLoadMoreView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 结束
     */
    public abstract void onComplete();

    /**
     * 正在加载中
     */
    public abstract void onLoading();

    /**
     * 出现错误
     */
    public abstract void onError();
}

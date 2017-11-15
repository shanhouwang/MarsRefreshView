package com.devin.refreshview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by Devin on 2017/11/14.
 *
 * @author Devin
 */
public class MarsNormalFooterView extends MarsOnLoadMoreView {

    public ProgressBar pbFooter;
    public TextView tvFooter;

    private String onLoadingText = "疯狂加载中";
    private String onLoadingErrorText = "点击加载更多";
    private String onLoadingCompleteText = "--  没有更多了  --";

    public MarsNormalFooterView(Context context) {
        super(context);
        initView(context);
    }

    /**
     * 设置footerView的属性
     *
     * @param attrsArray
     */
    public void setAttributeSet(TypedArray attrsArray) {
        String onLoadingText = attrsArray.getString(R.styleable.mars_onLoadingText);
        if (!TextUtils.isEmpty(onLoadingText)) {
            this.onLoadingText = onLoadingText;
        }
        String onLoadingErrorText = attrsArray.getString(R.styleable.mars_onLoadingErrorText);
        if (!TextUtils.isEmpty(onLoadingErrorText)) {
            this.onLoadingErrorText = onLoadingErrorText;
        }
        String onLoadingCompleteText = attrsArray.getString(R.styleable.mars_onLoadingCompleteText);
        if (!TextUtils.isEmpty(onLoadingCompleteText)) {
            this.onLoadingCompleteText = onLoadingCompleteText;
        }
        @SuppressLint("ResourceAsColor")
        int footerColor = attrsArray.getColor(R.styleable.mars_footerTextColor, R.color._aaaaaa);
        int footerTxtSize = attrsArray.getInt(R.styleable.mars_footerTextSize, 13);
        boolean footerTextVisible = attrsArray.getBoolean(R.styleable.mars_footerTextVisible, true);
        tvFooter.setTextSize(footerTxtSize);
        tvFooter.setTextColor(footerColor);
        if (footerTextVisible) {
            tvFooter.setVisibility(View.VISIBLE);
        } else {
            tvFooter.setVisibility(View.GONE);
        }
    }

    private void initView(Context context) {
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setBackgroundColor(getResources().getColor(R.color._00000000));
        View v = LayoutInflater.from(context).inflate(R.layout.layout_footer, this, false);
        addView(v);
        pbFooter = v.findViewById(R.id.pb_footer);
        tvFooter = v.findViewById(R.id.tv_footer);
    }

    @Override
    public void onCompleteStyle() {
        tvFooter.setVisibility(View.VISIBLE);
        tvFooter.setText(onLoadingCompleteText);
        pbFooter.setVisibility(View.GONE);
    }

    @Override
    public void onLoadingStyle() {
        tvFooter.setVisibility(View.VISIBLE);
        tvFooter.setText(onLoadingText);
        pbFooter.setVisibility(View.VISIBLE);
    }

    @Override
    public void onErrorStyle() {
        tvFooter.setText(onLoadingErrorText);
        pbFooter.setVisibility(View.GONE);
    }
}

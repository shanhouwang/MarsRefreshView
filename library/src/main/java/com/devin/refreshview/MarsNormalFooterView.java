package com.devin.refreshview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
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

    private String onLoadingText;
    private String onLoadingErrorText;
    private String onLoadingCompleteText;

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
        onLoadingText = attrsArray.getString(R.styleable.mars_onLoadingText);
        onLoadingErrorText = attrsArray.getString(R.styleable.mars_onLoadingErrorText);
        onLoadingCompleteText = attrsArray.getString(R.styleable.mars_onLoadingCompleteText);
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
        View v = LayoutInflater.from(context).inflate(R.layout.layout_footer, null);
        pbFooter = v.findViewById(R.id.pb_footer);
        tvFooter = v.findViewById(R.id.tv_footer);
    }

    @Override
    public void onComplete() {
        tvFooter.setText(onLoadingCompleteText);
        pbFooter.setVisibility(View.GONE);
    }

    @Override
    public void onLoading() {
        tvFooter.setText(onLoadingText);
        pbFooter.setVisibility(View.VISIBLE);
    }

    @Override
    public void onError() {
        tvFooter.setText(onLoadingErrorText);
        pbFooter.setVisibility(View.GONE);
    }
}

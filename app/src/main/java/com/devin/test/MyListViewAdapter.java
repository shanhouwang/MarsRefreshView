package com.devin.test;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Devin on 2017/11/15.
 *
 * @author De
 */

public class MyListViewAdapter extends BaseAdapter {

    private List<String> data = new ArrayList<>();

    public void bindData(List<String> data) {
        this.data.clear();
        this.data.addAll(data);
        notifyDataSetChanged();
    }

    private Context mContext;

    public MyListViewAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            ViewHolder vh = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.item_main, viewGroup, false);
            vh.tv = view.findViewById(R.id.tv);
            view.setTag(vh);
        }
        ViewHolder vh = (ViewHolder) view.getTag();
        vh.tv.setText(data.get(i));
        return view;
    }

    public static class ViewHolder {
        public TextView tv;
    }
}

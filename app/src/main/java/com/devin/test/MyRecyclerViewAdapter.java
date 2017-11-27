package com.devin.test;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Devin on 2017/11/15.
 *
 * @author De
 */

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    private List<String> data = new ArrayList<>();

    public void bindData(List<String> data) {
        this.data.clear();
        this.data.addAll(data);
        notifyDataSetChanged();
    }

    private Context mContext;

    public MyRecyclerViewAdapter(Context context) {
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_main, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position == TYPE_NORMAL || position == 2) {
            holder.tv.setText("正常");
        } else {
            holder.tv.setText("非正常");
        }

    }

    public static final int TYPE_NORMAL = 0;

    public static final int TYPE_AB_NORMAL = 1;

    @Override
    public int getItemViewType(int position) {
        if (position == TYPE_NORMAL || position == 2) {
            return TYPE_NORMAL;
        } else {
            return TYPE_AB_NORMAL;
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView tv;

        public ViewHolder(View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tv);
        }
    }
}

package com.example.xqq.myapplication.refreshlib.Adpater.Base;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.xqq.myapplication.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ck on 2017/9/10.
 */

public  class BaseAdapter extends RecyclerView.Adapter {
    //数据集合
    protected List<String> list=new ArrayList<>();


    private int count = 0;
    /**
     * 设置数据构造
     *
     * @param list
     */
    public BaseAdapter(List list) {
        this.list = list;
    }

    public BaseAdapter(int count) {
        this.count = count;
    }

    public BaseAdapter() {

    }

    public void setList(List list) {
        this.list = list;
        notifyDataSetChanged();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v=LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new Holder(v);
    }


    /**
     * bindview 先拦截设置状态布局
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        String s=list.get(position);
        ((Holder)holder).fill(s);
    }

    /**
     * item数量
     *
     * @return
     */
    @Override
    public int getItemCount() {
        return getCount();
    }

    private int getCount() {
        return list == null ? count : list.size();
    }


     class Holder extends RecyclerView.ViewHolder {

        private TextView m;

        public Holder(View itemView) {
            super(itemView);
            m=itemView.findViewById(R.id.text);
        }

         public void fill(String str){
             m.setText(str);
         }
    }
}

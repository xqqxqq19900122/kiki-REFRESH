package com.example.xqq.myapplication.nestpullview;

import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xqq.myapplication.R;
import com.example.xqq.myapplication.refreshlib.Adpater.Base.BaseAdapter;
import com.example.xqq.myapplication.refreshlib.RefreshViews.RefreshLayout;
import com.example.xqq.myapplication.refreshlib.RefreshViews.RefreshListener;
import com.example.xqq.myapplication.refreshlib.RefreshViews.RefreshWrap.Base.RefreshOuterHanderImpl;


import java.util.ArrayList;
import java.util.List;

public class Main3Activity extends AppCompatActivity {


    private List<String> list;
    private RefreshLayout layout;
    private BaseAdapter mBaseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);


        initRefreshLayout();
    }



    private void initRefreshLayout() {
        list = new ArrayList<>();
        mBaseAdapter=new BaseAdapter();
        addlist();

        layout = findViewById(R.id.refreshing);
        final RecyclerView recyclerView = findViewById(R.id.recyclerview);

        recyclerView.setAdapter(mBaseAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mBaseAdapter.setList(list);

        layout.setListener(new RefreshListener() {
            @Override
            public void Refreshing() {
                getWindow().getDecorView().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        layout.NotifyCompleteRefresh0();
                    }
                }, 1000);
            }

            @Override
            public void Loading() {
                getWindow().getDecorView().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        layout.NotifyCompleteRefresh0();
                    }
                }, 1000);
            }
        });

        layout.setRefreshWrap(new RefreshOuterHanderImpl());
   }

    private void addlist() {
        for (int i = 0; i < 20; i++) {
                list.add("hah");

        }
    }

}

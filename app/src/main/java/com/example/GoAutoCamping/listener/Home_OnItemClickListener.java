package com.example.GoAutoCamping.listener;

import android.view.View;

import com.example.GoAutoCamping.adapter.Home_Adapter;

public interface Home_OnItemClickListener {
    void onItemClick(Home_Adapter.ItemViewHolder holder, View view, int pos);
}

package com.example.xqq.myapplication

import android.content.Context
import android.view.View
import android.widget.Toast

class ClickHandler(context:Context) {

    private val mContext=context

    fun onClick(v: View){
Toast.makeText(mContext,"text2",Toast.LENGTH_LONG).show()
    }
}
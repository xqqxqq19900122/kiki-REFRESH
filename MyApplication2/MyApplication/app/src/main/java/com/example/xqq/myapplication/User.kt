package com.example.xqq.myapplication

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR

class User :BaseObservable(){
    @set:Bindable
    var name:String=""
    set(value){
        field=value
        notifyPropertyChanged(BR.name)
    }
}
package com.example.xqq.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.webkit.WebView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.databinding.DataBindingUtil
import com.example.xqq.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() , OnClickListener{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
   val binding=DataBindingUtil.setContentView<ActivityMainBinding>(this,R.layout.activity_main)
        val user=User();
        var list=ArrayList<String>(5)
        list.add("hah")
        user.name="qiqi"
        binding.user=user
        binding.list=list
        binding.clickListener=this
      val handler=ClickHandler(this)
        binding.clickhandler=handler
    }

    override fun onClick(@Nullable p0: View) {
       when(p0?.id){
           R.id.text1-> Toast.makeText(this,"text1",Toast.LENGTH_LONG).show()
       }
    }

     fun test( p0: View) {

    }

}

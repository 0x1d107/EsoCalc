package com.example.myapplication

import android.content.Context
import android.graphics.PorterDuff
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class Btn: Button {
    constructor(ctx: Context, size:Int, color:Int, text:String, layout: ViewGroup, click:(View)->Unit={}):super(ctx){
        this.width=(size*ctx.resources.displayMetrics.density).toInt()
        this.height=(size*ctx.resources.displayMetrics.density).toInt()
        layout.addView(this)
        this.background.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
        this.layoutParams.height=(size*ctx.resources.displayMetrics.density).toInt()
        this.layoutParams.width=(size*ctx.resources.displayMetrics.density).toInt()
        this.text=text
        this.textSize=5*ctx.resources.displayMetrics.density
        setPadding(-(textSize*text.length/2).toInt(),0,0,0)

        this.setOnClickListener (click)
    }
}
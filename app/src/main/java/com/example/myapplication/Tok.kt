package com.example.myapplication

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.text.toSpannable


class TokType(color:Long=0xffffffff,join:Boolean=false){
    val color = color
    val join = join

}


data class Tok(var str:String,val type:TokType)


class TokViewer(private val text:TextView,private val scroll:ScrollView){
    private val tokList = mutableListOf<Tok>()
   /* private val tokState= mutableMapOf<String,Int>()
    fun pushState(state: String){
        tokState[state]=(tokState[state]?:0)+1
    }
    fun getState(state:String):Boolean{
        return (tokState[state]?:0)>0
    }
    fun popState(state: String):Boolean{
        if(tokState.containsKey(state)&&tokState[state]?:0>0){
            tokState[state]=tokState[state]?:1-1
            return true
        }
        return false
    }*/

    fun addToken(type:TokType,value:String) {
        if(tokList.getOrNull(tokList.size-1)?.type==type&&type.join) {
            tokList.last().str += value
        }else {
            tokList.add(Tok(value,type))

        }

        update()
    }
    fun ghostToken(type:TokType,value:String){
        tokList.add(Tok(value,type))
        update()
        if(!isEmpty)tokList.removeAt(tokList.size-1)
    }
    fun popToken():Tok?{
        val t = if(!isEmpty)tokList.removeAt(tokList.size-1) else null

        update()
        return t
    }
    fun removeToken():Tok?{
        val t = if(!isEmpty)tokList.removeAt(0)else null

        update()
        return t
    }
    fun frontPeek():Tok?{
        return if(!isEmpty)tokList.getOrNull(0)else null
    }
    fun peek():Tok?{
        return if(!isEmpty)tokList.getOrNull(tokList.size-1)else null
    }
    val isEmpty get() = tokList.isEmpty()
    private fun update(){
        var txt = SpannableStringBuilder()
        var i=0

        for(tok in tokList){
            txt.append(tok.str)
            txt.setSpan(ForegroundColorSpan(tok.type.color.toInt()),i,i+tok.str.length,Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            i+=tok.str.length
        }
        text.text=txt.toSpannable()
        scroll.fullScroll(ScrollView.FOCUS_DOWN)

    }
}


package com.example.myapplication
import android.view.ViewGroup
import android.content.Context
import android.util.AttributeSet
import android.view.View


class ButtonLayout:ViewGroup{
    constructor(ctx: Context):super(ctx)
    constructor(ctx:Context,attrs:AttributeSet):super(ctx,attrs)
    constructor(ctx:Context,attrs: AttributeSet,d:Int):super(ctx,attrs,d)

    override fun onLayout(changed:Boolean,l:Int,t:Int,r:Int,b:Int){
        val left = paddingLeft
        val top = paddingTop
        val maxRight = measuredWidth-paddingRight
        val maxBottom = measuredHeight-paddingBottom
        val childWidth = maxRight-left
        val childHeight = maxBottom-top
        var x = left
        var y = top
        var maxHeight = 0
        for(i in 0.until(childCount)){
            val view = getChildAt(i)
            if(view.visibility == View.GONE){
                continue
            }

            view.measure(MeasureSpec.makeMeasureSpec(childWidth,MeasureSpec.AT_MOST),MeasureSpec.makeMeasureSpec(childHeight,MeasureSpec.AT_MOST))
            var marginTop = 0
            var marginBottom = 0
            var marginLeft = 0
            var marginRight = 0
            if(view.layoutParams is MarginLayoutParams){
                val params = view.layoutParams as MarginLayoutParams
                marginTop=params.topMargin
                marginBottom=params.bottomMargin
                marginLeft=params.leftMargin
                marginRight=params.rightMargin
                println("BTN_LAYOUT_$i MARGIN");
            }
            x+=marginLeft
            y+=marginTop
            val w = view.layoutParams.width
            val h = view.layoutParams.height
            val dpW = w / view.context.resources.displayMetrics.density
            val dpH = h / view.context.resources.displayMetrics.density
           // println("BTN_LAYOUT_$i Size: $dpW $dpH")
            if(w+x>maxRight){
                x = left
                y+= maxHeight
                maxHeight=0
            }
            val r = w+x
            val b = h+y
            //println("BTN_LAYOUT_$i:\n\t x = $x;\n\t y = $y;\n\t r = $r;\n\t b = $b;")
            view.layout(x,y,r,b)
            maxHeight = maxOf(maxHeight,h+marginTop+marginBottom)
            x+=w+marginRight
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var h=0
        var w=0

        var rowWidth=0
        var rowHeight=0

        for(i in 0.until(childCount)){
            val view = getChildAt(i)
            var marginWidth=0
            var marginHeight=0
            measureChild(view,widthMeasureSpec,heightMeasureSpec)
            if(view.layoutParams is MarginLayoutParams){
                val params = view.layoutParams as MarginLayoutParams
                marginHeight = params.bottomMargin+params.topMargin
                marginWidth = params.leftMargin+params.rightMargin
            }
            val childWidth = view.measuredWidth+marginWidth
            val childHeight = view.measuredHeight+marginHeight
            val nextWidth = rowWidth+childWidth
            val rowSize = MeasureSpec.getSize(widthMeasureSpec)-paddingLeft-paddingRight
            if (nextWidth>rowSize){
                w=maxOf(w,rowWidth)
                rowWidth=childWidth
                h+=rowHeight
                rowHeight=childHeight
            }else{
                rowWidth=nextWidth
            }
            rowHeight= maxOf(rowHeight,childHeight)
            //println("MEASURING LAYOUT::: $rowWidth $rowHeight")

        }
        w = maxOf(w,rowWidth)
        h+=rowHeight

        setMeasuredDimension(w,h)
    }

}
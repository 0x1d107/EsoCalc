package com.example.myapplication


class NDList<T:Any>( dimensions:List<Int>,vararg stuff:T):Iterable<NDList<T>>{


    private val dimensions= dimensions.toMutableList()
    private val data = stuff.toMutableList()
    val length get()= dimensions.fold(1){a,x->a*x}
    val rank get() = dimensions.size
    val dimension get() = dimensions.toList()
    fun  getData():List<T>{
        return 0.until(length).map {i->data[i%data.size]}
    }
    operator fun get(vararg index:Int):T{
        var i = 0
        for(di in dimensions.withIndex()){
            i+=index[di.index]*dimensions.subList(di.index+1,dimensions.size).fold(1){a,x->a*x}
        }
        return data[i%data.size]
    }
    override fun iterator(): Iterator<NDList<T>> {
        val newDim=dimensions.toMutableList()
        val iter = mutableListOf<NDList<T>>()
        for(i in 0.until(newDim.removeAt(0))){
            val newData = mutableListOf<T>()
            for(j in 0.until(newDim.fold(1){a,x->a*x}))
                newData.add(this.get(i,*globalToLocalIndex(newDim,j)))
            val a :Array<Any> = newData.toList().toTypedArray()
            iter.add(NDList(newDim,*(a as Array<T>)))
        }
        return iter.iterator()
    }
    private fun globalToLocalIndex(dims:List<Int>,i:Int):IntArray{
        val index = mutableListOf<Int>()
        for (d in 0.until(dims.size)){
            val ind = i/dims.subList(d+1,dims.size).fold(1){a,x->a*x}%dims[d]
            index.add(ind)

        }
        return index.toIntArray()
    }
    override fun toString(): String {
        var str = dimensions.joinToString(separator = ":",prefix = "[",postfix = "]")+"{"

        for(i in 0.until(length)){
            val index = globalToLocalIndex(dimensions,i)
            var delim = 1
            var c = 1

            for(d in (dimensions.size-1).downTo(0)){
                if(index[d]==dimensions[d]-1)
                    c++
                else {
                    delim=maxOf(delim,c)
                    c=0
                }
            }

            str+=this.get(*index).toString()+";".repeat(delim)
        }
        str = str.substringBeforeLast(";")
        str+="}"
        return str
    }
    operator fun plus(l:NDList<T>):NDList<T>{
        val d = this.dimensions
        d[0]++
        val ndata:Array<Any> =  (this.data+l.data).toList().toTypedArray()
        return NDList(d,*ndata as Array<T>)
    }
    fun combine(other:NDList<T>,fn:(T,T)->T):NDList<T>{
        val ndims = if(length>=other.length)dimensions  else other.dimensions
        val ndata:Array<Any> = 0.until(maxOf(length,other.length)).map{i->fn(data[i%data.size],other.data[i%other.data.size])}.toTypedArray()
        return NDList(ndims,*ndata as Array<T>)
    }


}

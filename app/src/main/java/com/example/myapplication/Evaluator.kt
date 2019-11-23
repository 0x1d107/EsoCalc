package com.example.myapplication


import java.math.RoundingMode
import kotlin.ClassCastException

class ValueStack{
    private val list = mutableListOf<EValue<Any>>()
    val storage = VariableStorage()
    fun< T:Any> push(v:EValue<T>){
        @Suppress("UNCHECKED_CAST")
        list.add(v as EValue<Any>)
    }
    fun<T:Any> popType(type:EValueType<T>):EValue<T>?{
        val v = list.lastOrNull()
        val vType = v?.getType()

        var result:EValue<T>?=null

        if(vType==type){
            try {
                @Suppress("UNCHECKED_CAST")
                result = v as EValue<T>?
            }catch (e:ClassCastException){
                System.err.println("[popType] Invalid Type was passed")
                return null
            }
        }
        if (result!=null)list.removeAt(list.size-1)
        return result
    }
    fun showLast(v:TokViewer){
        val t = list.lastOrNull()
        t?.display(v)
    }

    override fun toString(): String {
        return list.toString()
    }
}
class VariableStorage{
    private val storage = mutableListOf(mutableMapOf<String,EValue<Any>>())
    fun<T:Any> setVariable(name:String,value:EValue<T>){
        @Suppress("UNCHECKED_CAST")
        storage.lastOrNull()?.set(name,value as EValue<Any>)
    }
    fun<T:Any> getVariable(name:String,type:EValueType<T>):EValue<T>?{
        val v = storage.lastOrNull()?.get(name)
        val vType = v?.getType()
        var result:EValue<T>?=null
        if(vType==type){
            try {
                @Suppress("UNCHECKED_CAST")
                result = v as EValue<T>?
            }catch (e:ClassCastException){
                System.err.println("[getVariable] Invalid Type was passed")
                return null
            }
        }

        return result
    }
    fun pushContext(){
        storage.add(storage.last().toMutableMap())
    }
    fun popContext(){
        if(storage.size>1){
            storage.removeAt(storage.size-1)
        }
    }
    fun getDefinedNames():Set<String>{
        return storage.lastOrNull()?.keys?: emptySet()
    }

}
class Evaluator {
    private val stack=ValueStack()

    val parser = Parser()
    fun getDefinedVariables():Set<String>{
        return stack.storage.getDefinedNames()
    }
    fun evaluate(v:TokViewer){
        fun show_error(){

            val tokErr = TokType(0xffed261f)
            while(!v.isEmpty)v.popToken()
            v.ghostToken(tokErr,"ERROR")


        }

        val stream = parser.parse(v)
        println(stream)
        if(stream==null){
            show_error()
            return
        }

        while(!stream.empty){
            var t:Boolean? = null
            println(stream)
            try{
                t=stream.getParsed()?.evaluate(stack)
            }catch(e:Exception){
                System.err.println(e.toString())
            }

            if(t!=true){
                show_error()
                return
            }
        }
       stack.showLast(v)
    }
}
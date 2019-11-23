package com.example.myapplication

import java.math.BigDecimal
import java.math.RoundingMode

interface EValue<T:Any>:Parsed<EValue<T>>{
    override fun getType():EValueType<T>
    fun getValue():T
    fun display(v:TokViewer){
        v.addToken(this.getType().getTokenType(),this.getValue().toString())
    }

    override fun evaluate(stack: ValueStack): Boolean {
        stack.push(this)
        return true
    }
}




interface EValueType<T:Any>:Parsable<EValue<T>>

interface Parsable<T>{
    fun canParse(t:TokViewer):Boolean
    fun parse(t:TokViewer):T
    fun getTokenType():TokType
}
interface Parsed<T>{
    fun getType():Parsable<T>
    fun evaluate(stack:ValueStack):Boolean
}
interface EOp<A:Any,B:Any,T:Any>:Parsed<EOp<A,B,T>>{
    override fun getType():EOpType<A,B,T>

}
interface EOpType<A:Any,B:Any,T:Any>:Parsable<EOp<A,B,T>>{
    fun getPrecedence():Int
}
class ENumber(private val value:BigDecimal):EValue<BigDecimal>{
    override fun evaluate(stack: ValueStack):Boolean {
        stack.push(this)
        return true

    }


    override fun getType(): EValueType<BigDecimal> {
       return ENumberType
    }

    override fun getValue(): BigDecimal {
       return value
    }

    override fun toString(): String {
        return getValue().toPlainString()
    }

    override fun display(v: TokViewer) {
        v.addToken(ENumberType.tokType,value.setScale(6,RoundingMode.HALF_UP).stripTrailingZeros().toString())
    }

}
object ENumberType:EValueType<BigDecimal>{
    override fun getTokenType(): TokType {
        return this.tokType
    }


    override fun parse(t: TokViewer): EValue<BigDecimal> {
        assert(this.canParse(t))
        val i = t.removeToken()?.str?.toBigDecimal()?: BigDecimal(0)
        return ENumber(i)
    }

    val tokType=TokType(0xff53d1ed,true)
    override fun canParse(t: TokViewer): Boolean {
        return t.frontPeek()?.type == tokType && t.frontPeek()?.str?.toBigDecimalOrNull()!=null
    }

}
object EBracketType:Parsable<EBracket>{
    override fun getTokenType(): TokType {
        return this.tokParenType
    }


    val tokParenType = TokType(0xff00ff77,false)
    override fun canParse(t: TokViewer): Boolean {
        return t.frontPeek()?.type== tokParenType && strToBracket(t.frontPeek()?.str?:"").variant!=EBracket.BracketVariant.UNKNOWN
    }
    fun strToBracket(str:String):EBracket{
        //System.out.println("strToBracket:"+str)
        val result = when(str){
            "("-> EBracket(EBracket.BracketVariant.ROUND,false)
            "["-> EBracket(EBracket.BracketVariant.SQUARE,false)
            "{"->EBracket(EBracket.BracketVariant.CURLY,false)
            ")"-> EBracket(EBracket.BracketVariant.ROUND,true)
            "]"->EBracket(EBracket.BracketVariant.SQUARE,true)
            "}"-> EBracket(EBracket.BracketVariant.CURLY,true)
            else -> EBracket(EBracket.BracketVariant.UNKNOWN,false)
        }
        //System.out.println(result.variant)
        return result
    }
    override fun parse(t: TokViewer): EBracket {
        return  strToBracket(t.removeToken()?.str?:"")
    }

}
class EBracket(val variant:BracketVariant,val isClosing:Boolean):Parsed<EBracket>{
    enum class BracketVariant{
        ROUND,CURLY,SQUARE,UNKNOWN
    }
    override fun getType(): Parsable<EBracket> {
        return EBracketType
    }

    override fun evaluate(stack: ValueStack): Boolean {
        System.err.println("Tried to evaluate bracket!!")
        return false
    }

}

class EBinOpType(private val symbol:String,private val precedence:Int,private val eval:(BigDecimal,BigDecimal)->BigDecimal?):EOpType<BigDecimal,BigDecimal,BigDecimal>{
    override fun getTokenType(): TokType {
        return tokOpType
    }
    val tokOpType = TokType(0xFFff7526,false)

    class EBinOp(private val sym:String,private val type:EOpType<BigDecimal,BigDecimal,BigDecimal>,private val eval:(BigDecimal,BigDecimal)->BigDecimal?):EOp<BigDecimal,BigDecimal,BigDecimal>{
        override fun evaluate(stack: ValueStack):Boolean {
            val b = stack.popType(ENumberType)
            val a = stack.popType(ENumberType)
            if(a==null||b==null)return false
            val c = eval(a.getValue(), b.getValue())
            c?:return false
            stack.push(ENumber(c))
            return true
        }
        override fun getType(): EOpType<BigDecimal, BigDecimal, BigDecimal> {
            return type
        }

        override fun toString(): String {
            return sym
        }

    }
    override fun canParse(t: TokViewer): Boolean {
        return t.frontPeek()?.str==" $symbol "
    }

    override fun parse(t: TokViewer): EOp<BigDecimal, BigDecimal, BigDecimal> {
        assert(this.canParse(t))
        t.removeToken()
        return EBinOp(symbol,this,eval)
    }

    override fun getPrecedence(): Int {
       return precedence
    }

}

val ESubType = EBinLOpType("-",7){a,b -> a - b}
val EMulType = EBinLOpType("*",14){a,b -> a * b}
val EDivType = EBinLOpType("/",14){a,b-> if(b!= BigDecimal(.00)) a.divide(b,16,RoundingMode.HALF_UP) else null}
val EAddType = EBinLOpType("+",7){a,b->a + b}

class Parser{
    private val parsableTypeList = mutableListOf<Parsable<Parsed<*>>>()
    fun addType(p:Parsable<*>){
        @Suppress("UNCHECKED_CAST")
        val t = p as Parsable<Parsed<*>>
        parsableTypeList.add(t)
    }
    fun parse(t:TokViewer):ParsedStream?{
        val parsed = mutableListOf<Parsed<*>>()

        while (!t.isEmpty){
            var didParse=false
            for (p in parsableTypeList){
                if(p.canParse(t)){
                    parsed.add(p.parse(t))
                    didParse=true
                    break
                }
            }
            if(!didParse){
                System.err.println("Failed to parse!!!")
                System.err.println("Dump parsed: $parsed")
                return null
            }

        }
        val processed =  mutableListOf<Parsed<*>>()
        val operators = mutableListOf<EOp<*,*,*>>()
        val bracketStack = mutableListOf<Int>()
        fun addOperator(op:EOp<*,*,*>){
            var last = operators.lastOrNull()?.getType()?.getPrecedence()
            val self = op.getType().getPrecedence()
            while (last!=null&&self<=last&&bracketStack.lastOrNull()?:0<operators.size){
                processed.add(operators.last())
                operators.removeAt(operators.size-1)
                last = operators.lastOrNull()?.getType()?.getPrecedence()
            }
            operators.add(op)

        }

        for(p in parsed){

            when (p.getType()){
                is EOpType<*,*,*> ->
                    addOperator(p as EOp<*, *, *>)

                is EBracketType ->
                    if ((p as EBracket).isClosing){
                        if(bracketStack.size==0){
                            System.err.println("bracket stack underflow!!")
                            return null
                        }
                        val lastBracketLength = bracketStack.removeAt(bracketStack.size-1)
                        while(operators.size>lastBracketLength)processed.add(operators.removeAt(operators.size-1))
                    }else{
                        bracketStack.add(operators.size)
                    }
                else -> processed.add(p)
            }
            //println(bracketStack)
        }

        if(bracketStack.size>0){
            System.err.println("bracket stack is not empty!!")
            return null
        }
        processed.addAll(operators.reversed())
        return ParsedStream(processed)
    }
}
class ParsedStream(private val list:List<Parsed<*>>){



    private var ptr = 0
    val empty get() = ptr>=list.size
    fun getParsed():Parsed<*>?{
        val v = list.getOrNull(ptr)
        ptr++
        return v
    }
    fun peekParsed():Parsed<*>?{
        return list.getOrNull(ptr)
    }
    override fun toString():String{
        return list.subList(ptr,list.size).toString()
    }



}
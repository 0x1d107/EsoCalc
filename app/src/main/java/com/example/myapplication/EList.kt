package com.example.myapplication

import java.math.BigDecimal

class EListNumber(private val num:ENumber):EValue<NDList<ENumber>> {
    override fun getType(): EValueType<NDList<ENumber>> {
        return EListNumType
    }

    override fun getValue(): NDList<ENumber> {
       return NDList(listOf(1),num)
    }

    override fun display(v: TokViewer) {
        num.display(v)
    }

    override fun toString(): String {
        return num.toString()
    }
}
object EListNumType:EValueType<NDList<ENumber>> {
    override fun canParse(t: TokViewer): Boolean {
        return ENumberType.canParse(t)
    }

    override fun parse(t: TokViewer): EValue<NDList<ENumber>> {
        return EListNumber(ENumberType.parse(t) as ENumber)
    }

    override fun getTokenType(): TokType {
        return ENumberType.tokType
    }
}


class EList(private val value:NDList<ENumber>):EValue<NDList<ENumber>> {
    override fun display(v: TokViewer) {
        v.addToken(EBracketType.tokParenType,"(")
       if(value.rank>1){
           for (i in value.dimension.withIndex()){
                v.addToken(ENumberType.tokType,i.value.toString())
                if(i.index != value.dimension.lastIndex){
                    v.addToken(EOpConcatType.tokOpType,", ")
                }
           }
           v.addToken(EOpReshapeType.tokOpType,": ")
        }
        for (n in value.getData().withIndex()){
            n.value.display(v)
            if(n.index != value.getData().lastIndex){
                v.addToken(EOpConcatType.tokOpType,", ")
            }
        }
        v.addToken(EBracketType.tokParenType,")")
    }

    override fun getType(): EValueType<NDList<ENumber>> {
        return EListType
    }

    override fun getValue(): NDList<ENumber> {
        return this.value
    }

    override fun evaluate(stack: ValueStack): Boolean {
        stack.push(this)
        return true
    }

    override fun toString(): String {
        return value.toString()
    }
}

object EListType:EValueType<NDList<ENumber>> {
    override fun getTokenType(): TokType {
        return ENumberType.tokType
    }

    override fun canParse(t: TokViewer): Boolean {
        return false
    }

    override fun parse(t: TokViewer): EValue<NDList<ENumber>> {
        return EList(NDList(listOf(1), ENumber(BigDecimal(0))))
    }
}

object EOpConcat:EOp<EList,EList,EList> {
    override fun evaluate(stack: ValueStack): Boolean {
        println(stack)
        val lb = stack.popType(EListType)?:stack.popType(EListNumType)?:return false
        println(lb)
        val la = stack.popType(EListType)?:stack.popType(EListNumType)?:return false
        println(la)

        stack.push( EList(la.getValue()+lb.getValue()))
        return true
    }

    override fun getType(): EOpType<EList, EList, EList> {
        return EOpConcatType
    }
}
object EOpConcatType:EOpType<EList,EList,EList> {
    override fun getTokenType(): TokType {
        return tokOpType
    }
    val tokOpType = TokType(0xFFff7526,false)
    override fun canParse(t: TokViewer): Boolean {
       return t.frontPeek()?.str == ", "
    }

    override fun parse(t: TokViewer): EOp<EList, EList, EList> {
        assert(canParse(t))
        t.removeToken()
        return EOpConcat
    }

    override fun getPrecedence(): Int {
        return -224
    }
}
object EOpReshapeType:EOpType<EList,EList,EList>{
    override fun canParse(t: TokViewer): Boolean {
        return t.frontPeek()?.str == ": "
    }
    val tokOpType = TokType(0xFFff7526,false)
    override fun parse(t: TokViewer): EOp<EList, EList, EList> {
        assert(canParse(t))
        t.removeToken()
        return EOpReshape
    }

    override fun getTokenType(): TokType {
        return tokOpType
    }

    override fun getPrecedence(): Int {
       return -231
    }

}
object EOpReshape:EOp<EList,EList,EList>{
    override fun evaluate(stack: ValueStack): Boolean {
        val lb = stack.popType(EListType)?:stack.popType(EListNumType)?:return false
        val la = stack.popType(EListType)?:stack.popType(EListNumType)?:return false
        val dims = la.getValue().map { t->t[0].getValue().toInt() }
        val data = lb.getValue().map{ t->t[0]}.toTypedArray()
        val lst = NDList(dims,*data)
        stack.push(EList(lst))
        return true
    }

    override fun getType(): EOpType<EList, EList, EList> {
        return EOpReshapeType
    }

}
class EBinLOpType(private val symbol:String,private val precedence:Int,private val eval:(BigDecimal,BigDecimal)->BigDecimal?):EOpType<EList,EList,EList>{
    override fun getTokenType(): TokType {
        return tokOpType
    }
    val tokOpType = TokType(0xFFff7526,false)

    class EBinOp(private val sym:String,private val type:EOpType<EList,EList,EList>,private val eval:(BigDecimal,BigDecimal)->BigDecimal?):EOp<EList,EList,EList>{
        override fun evaluate(stack: ValueStack):Boolean {
            val lb = stack.popType(EListType)?:stack.popType(EListNumType)?:return false
            val la = stack.popType(EListType)?:stack.popType(EListNumType)?:return false
            var success=true
            val c = la.getValue().combine(lb.getValue()){a,b->
                val t =eval(a.getValue(),b.getValue())
                success=success and (t!= null)
                ENumber(t?: BigDecimal(0))
            }

            if(success)stack.push(EList(c))
            return success
        }
        override fun getType(): EOpType<EList,EList,EList> {
            return type
        }

        override fun toString(): String {
            return sym
        }

    }
    override fun canParse(t: TokViewer): Boolean {
        return t.frontPeek()?.str==" $symbol "
    }

    override fun parse(t: TokViewer): EOp<EList,EList,EList> {
        assert(this.canParse(t))
        t.removeToken()
        return EBinOp(symbol,this,eval)
    }

    override fun getPrecedence(): Int {
        return precedence
    }

}

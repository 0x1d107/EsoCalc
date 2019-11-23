package com.example.myapplication

object EFCallOp:EOp<Any,EList,EList> {
    override fun evaluate(stack: ValueStack): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getType(): EOpType<Any, EList, EList> {
        return EFCallType
    }
}
object EFCallType:EOpType<Any,EList,EList> {
    override fun canParse(t: TokViewer): Boolean {
        return 
    }

    override fun parse(t: TokViewer): EOp<Any, EList, EList> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getTokenType(): TokType {
        return TokType()
    }

    override fun getPrecedence(): Int {
        return 1337
    }
}
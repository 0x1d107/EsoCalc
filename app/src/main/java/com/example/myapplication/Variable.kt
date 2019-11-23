package com.example.myapplication




data class EVarName(val name:String,val indexed:Boolean=false,val index:IntArray= IntArray(0)):EValue<EVarName> {
    override fun getType(): EValueType<EVarName> {
       return EVarNameType
    }

    override fun getValue(): EVarName {
       return this
    }

    override fun evaluate(stack: ValueStack): Boolean {
       return false
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other)&&other is EVarName&&other.index.contentEquals(this.index)
    }

    override fun hashCode(): Int {
        return super.hashCode() xor index.hashCode()
    }
}
object EVarNameType:EValueType<EVarName> {
    override fun getTokenType(): TokType {
        return TokType()
    }

    override fun canParse(t: TokViewer): Boolean {
        return false
    }

    override fun parse(t: TokViewer): EValue<EVarName> {
        assert(this.canParse(t))
        return EVarName("")
    }
}

class EVariable(private val name:String,private val assign:Boolean = false):Parsed<EVariable> {
    override fun getType(): Parsable<EVariable> {
        return EVariableType
    }

    override fun evaluate(stack: ValueStack): Boolean {
        if(assign){
            stack.push(EVarName(name))
            return true
        }
        val v = stack.storage.getVariable(name,EListType)?:stack.storage.getVariable(name,EListNumType)
        if(v!=null){
            stack.push(v)
            return true
        }
        return false
    }

    override fun toString(): String {
        return (if(assign)"@" else "")+"\$$name"
    }
}
object EVariableType:Parsable<EVariable> {
    override fun getTokenType(): TokType {
        return tokVarTokType
    }

    val tokVarTokType = TokType(0xFFff4b14)
    override fun canParse(t: TokViewer): Boolean {
       return t.frontPeek()?.type == tokVarTokType && t.frontPeek() ?.str?.map { x->x.isLetter() }?.reduce{a,x->a&&x}==true
    }

    override fun parse(t: TokViewer): EVariable {
        assert(this.canParse(t))
        val tok = t.removeToken()?:return EVariable("")

        var assign =false
        if(t.frontPeek()?.type== EAssType.tokOpType&&t.frontPeek()?.str==" ← "){
            assign=true

        }
        return EVariable(tok.str,assign)
    }
}
object EAssOp:EOp<EVarName,EList,EList> {
    override fun evaluate(stack: ValueStack): Boolean {
        val num =  stack.popType(EListType)?:stack.popType(EListNumType)?:return false
        val vName = stack.popType(EVarNameType)?:return false
        stack.storage.setVariable(vName.getValue().name,num)
        stack.push(num)
        return true
    }

    override fun getType(): EOpType<EVarName,EList,EList> {
       return EAssType
    }
}
object EAssType:EOpType<EVarName,EList,EList> {
    override fun getTokenType(): TokType {
        return tokOpType
    }
    val tokOpType = TokType(0xFFff7526,false)
    override fun canParse(t: TokViewer): Boolean {
        return t.frontPeek()?.type== tokOpType&&t.frontPeek()?.str==" ← "
    }

    override fun parse(t: TokViewer): EOp<EVarName,EList,EList> {
        assert(this.canParse(t))
        t.removeToken()
        return EAssOp
    }

    override fun getPrecedence(): Int {
        return -238
    }
}
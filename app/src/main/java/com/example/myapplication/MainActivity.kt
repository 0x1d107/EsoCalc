package com.example.myapplication


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod

import android.widget.ScrollView
import android.widget.TextView
import android.content.Context
import android.content.DialogInterface
import android.text.InputType
import android.widget.EditText
import androidx.appcompat.app.AlertDialog

fun prompt(ctx:Context,title:String,cbk:(String)->Unit){
    val builder = AlertDialog.Builder(ctx)
    builder.setTitle(title)
    builder.setNegativeButton("Cancel"){d,_->
        d.cancel()
    }
    val textInput = EditText(ctx)
    textInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
    builder.setView(textInput)
    builder.setPositiveButton("Ok"){d,_->
        d.dismiss()
        cbk(textInput.text.toString())

    }

    builder.show()

}
val eval = Evaluator()

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        setContentView(R.layout.activity_main)
        val tView = findViewById<TextView>(R.id.exprView)
        val buttonLayout = findViewById<ButtonLayout>(R.id.buttonLayout)
        val scroll = findViewById<ScrollView>(R.id.exprScrollView)

        val tkViewer = TokViewer(tView,scroll)





        //eval.parser.addType(ENumberType)
        eval.parser.addType(EListNumType)
        eval.parser.addType(EOpConcatType)
        eval.parser.addType(EOpReshapeType)
        eval.parser.addType(EAddType)
        eval.parser.addType(ESubType)
        eval.parser.addType(EMulType)
        eval.parser.addType(EDivType)
        eval.parser.addType(EBracketType)
        eval.parser.addType(EVariableType)
        eval.parser.addType(EAssType)
        tView.movementMethod=ScrollingMovementMethod()
        for (i in 0.until(10)){

            Btn(applicationContext,64,0xFF0099CC.toInt(),i.toString(),buttonLayout){

                tkViewer.addToken(ENumberType.tokType,i.toString())

            }

        }
        Btn(applicationContext,64,0xFF0099CC.toInt(),".E",buttonLayout){
            if(tkViewer.peek()?.type != ENumberType.tokType)
                tkViewer.addToken(ENumberType.tokType,"0")
            if(tkViewer.peek()?.type == ENumberType.tokType&&tkViewer.peek()?.str?.contains(".")==false)
                tkViewer.addToken(ENumberType.tokType,".")
            else
                tkViewer.addToken(ENumberType.tokType,"E")

        }

        Btn(applicationContext,64,0xFFed8302.toInt(),"+",buttonLayout){
            tkViewer.addToken(EAddType.tokOpType," + ")
        }
        Btn(applicationContext,64,0xFFed8302.toInt(),"*",buttonLayout){
            tkViewer.addToken(EMulType.tokOpType," * ")
        }
        Btn(applicationContext,64,0xFFed8302.toInt(),"-",buttonLayout){
            if(tkViewer.peek()?.type!=ENumberType.tokType&&tkViewer.peek()?.str!=")"||tkViewer.peek()?.type==ENumberType.tokType&&tkViewer.peek()?.str?.endsWith("E",true)==true){
                tkViewer.addToken(ENumberType.tokType,"-")
            }else
                tkViewer.addToken(EMulType.tokOpType," - ")
        }
        Btn(applicationContext,64,0xFFed8302.toInt(),"/",buttonLayout){
            tkViewer.addToken(EDivType.tokOpType," / ")
        }
        Btn(applicationContext,64,0xFF00a14e.toInt(),"()",buttonLayout){


            when(tkViewer.peek()?.type){
                EVariableType.tokVarTokType,ENumberType.tokType-> tkViewer.addToken(EBracketType.tokParenType,")")
                EAssType.tokOpType-> tkViewer.addToken(EBracketType.tokParenType,"(")
                else ->  tkViewer.addToken(EBracketType.tokParenType,"(")
            }



        }

        Btn(applicationContext,64,0xFFE71D36.toInt(),"C",buttonLayout){
            tkViewer.popToken()
        }
        Btn(applicationContext,64,0xFFA6E22E.toInt(),"=",buttonLayout){
            eval.evaluate(tkViewer)
        }
        Btn(applicationContext,64,0xffed4c02.toInt(),"←",buttonLayout){
            tkViewer.addToken(EAssType.tokOpType," ← ")
        }
        Btn(applicationContext,64,0xffed4c02.toInt(),",",buttonLayout){
            tkViewer.addToken(EOpConcatType.tokOpType,", ")
        }
        Btn(applicationContext,64,0xffed4c02.toInt(),":",buttonLayout){
            tkViewer.addToken(EOpConcatType.tokOpType,": ")
        }
        Btn(applicationContext,64,0xffed4c02.toInt(),"var",buttonLayout){
            prompt(this,"Variable name") {s->
                tkViewer.addToken(EVariableType.tokVarTokType, s)
                Btn(applicationContext,64,0xffed4c02.toInt(),s,buttonLayout){
                    tkViewer.addToken(EVariableType.tokVarTokType,s)
                }
            }
        }

        for(name in eval.getDefinedVariables()){
            Btn(applicationContext,64,0xffed4c02.toInt(),name,buttonLayout){
                tkViewer.addToken(EVariableType.tokVarTokType,name)
            }
        }

    }
}

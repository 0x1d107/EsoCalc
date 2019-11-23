package com.example.myapplication

import java.math.BigInteger

class Rational (a:BigInteger,b:BigInteger){
    val numerator:BigInteger = a.divide(a.gcd(b))
    val denominator:BigInteger = b.divide(a.gcd(b))
    fun plus(x:Rational) = Rational(numerator*x.denominator+x.numerator*denominator,denominator*x.denominator)
    fun minus(x:Rational) = Rational(numerator*x.denominator-x.numerator*denominator,denominator*x.denominator)
    fun times(x:Rational) = Rational(numerator*x.numerator,denominator*x.denominator)
    fun div(x:Rational) = Rational(numerator*x.denominator,denominator*x.numerator)
}
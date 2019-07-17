package com.foodbodi.utils

interface Action<T:Any> {
    fun accept(data:T?)

    fun deny(data:T?, reason:String)
}
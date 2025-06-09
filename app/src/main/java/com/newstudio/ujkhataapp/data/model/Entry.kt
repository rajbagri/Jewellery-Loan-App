package com.newstudio.ujkhataapp.data.model

data class Entry(
    val id : String = "",
    val amount : Double = 0.0,
    val jewellery : String = "",
    val cross : Boolean = false,
    val time : Long = System.currentTimeMillis()

)
package com.newstudio.ujkhataapp.data.model

data class EntryInEntry(
    val id : String = "",
    val amount : Double = 0.0,
    val interestRate: Double = 3.0,
    val cross : Boolean = false,
    val time : Long = System.currentTimeMillis()
)

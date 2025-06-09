package com.newstudio.ujkhataapp.data.model

data class Customer (
    val id : String = "",
    val customerName : String = "",
    val customerTown : String = "",
    val number : String = "",
    val time : Long = System.currentTimeMillis()
)
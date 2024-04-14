package com.example.hopperrestaurant

data class Schedule (
    val schedId: Long = -1L,
    var date: String,
    var time: String,
    var empid: Long,
    var isBusyDay: Boolean,
)
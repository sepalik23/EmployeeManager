package com.example.hopperrestaurant

data class EmpAvailability(
    val availId: Long = -1L,
    val empId: Long,
    val monAM: Boolean,
    val monPM: Boolean,
    val tueAM: Boolean,
    val tuePM: Boolean,
    val wedAM: Boolean,
    val wedPM: Boolean,
    val thuAM: Boolean,
    val thuPM: Boolean,
    val friAM: Boolean,
    val friPM: Boolean,
    val sat: Boolean,
    val sun: Boolean
)
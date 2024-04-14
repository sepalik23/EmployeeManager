package com.example.hopperrestaurant

data class Employee(
    val empId: Long = -1,
    var firstName: String,
    var lastName: String,
    var nickName: String = "",
    var email: String,
    var phoneNum: String,
    var canOpen: Boolean,
    var canClose: Boolean,
    var status: Boolean,

    ) {
    fun getCustomName(): String {
        var name = nickName.ifEmpty {  // name = nickname BUT if it's empty, use firstname
            firstName
        }
        // add first letter and .
        name += " ${lastName.substring(0, 1)}."
        return name
    }

    fun getRoles(): String {
        val canOpen = if (canOpen) "Opener" else ""
        val canClose = if (canClose) "Closer" else ""
        // convert to array, remove blanks, convert to string with , as separator
        return arrayOf(canOpen, canClose)
            .filter { it != "" }
            .joinToString(", ")
    }
}

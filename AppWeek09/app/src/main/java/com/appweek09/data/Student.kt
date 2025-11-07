package com.appweek09.data

import java.util.Date
import java.util.UUID

data class Student(
    val name: String,
    val id: String = UUID.randomUUID().toString(),
    val department: String = "Computer Science",
    val grade: String = "2nd Year",
    val email: String = "",
    val addedDate: Date = Date()
){
    override fun toString(): String = name
}

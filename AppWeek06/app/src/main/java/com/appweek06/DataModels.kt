package com.appweek06

import java.util.*

data class Student(
    val name: String,
    val id: String = UUID.randomUUID().toString(),
    val addedDate: Date = Date()
)

data class CartItem(
    //기본 생성자
    val name: String,
    var quantity: Int = 1,
    val price: Double,
    val id: String = UUID.randomUUID().toString(),
    val addedDate: Date = Date()
) {
    // 멤버 함수, 메소드
    fun getTotalPrice(): Double = quantity * price

    override fun toString(): String {
        return "$name (x$quantity) - $%.2f".format(getTotalPrice())
    }
}

// 값 변경 X , 열거형
enum class AppMode(val displayName: String) {
    STUDENT_LIST("Student List"),
    SHOPPING_CART("Shopping Cart"),
}
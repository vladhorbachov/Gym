package com.gymshark.domain.models

data class MealInfo(
    val product: Product,
    val date: Long,
    val mealType: String

)

enum class MealType(
    val mealType: String
){
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    SNACK("Snack")
}


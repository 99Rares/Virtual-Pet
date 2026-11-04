package com.rares.virtualpetpompi.domain

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.rares.virtualpetpompi.R

enum class FoodCategory { BREAKFAST, DINNER, SNACKS }

/** Something Pompi can eat. [id] is the stable key stored in the database. */
data class Food(
    val id: String,
    @StringRes val name: Int,
    @DrawableRes val image: Int,
    val category: FoodCategory,
    val fullness: Int,
    val price: Int,
)

/** A room (background) Pompi lives in. */
data class Room(
    val id: String,
    @StringRes val name: Int,
    @DrawableRes val image: Int,
    val price: Int,
)

object Catalog {
    val foods: List<Food> = listOf(
        Food("rice", R.string.food_rice, R.drawable.rice, FoodCategory.BREAKFAST, 5, 5),
        Food("cirese", R.string.food_cirese, R.drawable.cirese, FoodCategory.BREAKFAST, 1, 2),
        Food("clatitecucafea", R.string.food_clatitecucafea, R.drawable.clatitecucafea, FoodCategory.BREAKFAST, 20, 16),
        Food("englishbreakfast", R.string.food_englishbreakfast, R.drawable.englishbreakfast, FoodCategory.BREAKFAST, 26, 16),
        Food("fruits1", R.string.food_fruits1, R.drawable.fruits1, FoodCategory.BREAKFAST, 15, 13),
        Food("cafea", R.string.food_cafea, R.drawable.cafea, FoodCategory.BREAKFAST, 4, 4),

        Food("carbonara", R.string.food_carbonara, R.drawable.carbonara, FoodCategory.DINNER, 15, 25),
        Food("salad", R.string.food_salad, R.drawable.salad, FoodCategory.DINNER, 8, 15),
        Food("soup", R.string.food_soup, R.drawable.soup, FoodCategory.DINNER, 10, 12),
        Food("steak1", R.string.food_steak1, R.drawable.steak1, FoodCategory.DINNER, 30, 35),
        Food("steak2", R.string.food_steak2, R.drawable.steak2, FoodCategory.DINNER, 20, 22),

        Food("cartofipai", R.string.food_cartofipai, R.drawable.cartofipai, FoodCategory.SNACKS, 7, 10),
        Food("burger", R.string.food_burger, R.drawable.burger, FoodCategory.SNACKS, 20, 27),
        Food("cola", R.string.food_cola, R.drawable.cola, FoodCategory.SNACKS, 4, 3),
        Food("hotdog", R.string.food_hotdog, R.drawable.hotdog, FoodCategory.SNACKS, 16, 20),
        Food("nuggets", R.string.food_nuggets, R.drawable.nuggets, FoodCategory.SNACKS, 10, 10),
        Food("onionrings", R.string.food_onionrings, R.drawable.onionrings, FoodCategory.SNACKS, 9, 8),
        Food("gogosi", R.string.food_gogosi, R.drawable.gogosi, FoodCategory.SNACKS, 12, 10),
        Food("pizza", R.string.food_pizza, R.drawable.pizza, FoodCategory.SNACKS, 10, 9),
    )

    val rooms: List<Room> = listOf(
        Room("bg0", R.string.room_default, R.drawable.bg0, 0),
        Room("bg1", R.string.room_1, R.drawable.bg1, 45),
        Room("bg2", R.string.room_2, R.drawable.bg2, 40),
        Room("bg3", R.string.room_3, R.drawable.bg3, 30),
    )

    const val DEFAULT_ROOM_ID = "bg0"

    fun food(id: String): Food? = foods.firstOrNull { it.id == id }
    fun room(id: String): Room = rooms.firstOrNull { it.id == id } ?: rooms.first()
}

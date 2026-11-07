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
    /** Hunger points restored. */
    val fullness: Int,
    val price: Int,
    /** 0 (empty calories) to 5 (whole food). Healthier food is better value per coin, see [Catalog.foods]. */
    val health: Int,
)

/** How a room is drawn: the original artwork, or a scene painted in code. */
sealed interface RoomArt {
    data class Image(@DrawableRes val res: Int) : RoomArt
    data class Scene(val kind: SceneKind) : RoomArt
}

enum class SceneKind { SUNSET, NIGHT, OCEAN, BLOSSOM }

/** A room (background) Pompi lives in. */
data class Room(
    val id: String,
    @StringRes val name: Int,
    val art: RoomArt,
    val price: Int,
)

object Catalog {
    /**
     * Balance: the pet loses 3 hunger points an hour, so about 72 a day. Whole food costs roughly
     * 0.4 coins per point (about 30 coins a day, affordable at ~3,000 steps on Normal); junk costs
     * 1.0 to 2.0 coins per point and fills little, so it is never the efficient choice.
     */
    val foods: List<Food> = listOf(
        Food("rice", R.string.food_rice, R.drawable.rice, FoodCategory.BREAKFAST, 12, 5, 4),
        Food("cherries", R.string.food_cherries, R.drawable.cherries, FoodCategory.BREAKFAST, 8, 3, 5),
        Food("coffee_crepes", R.string.food_coffee_crepes, R.drawable.coffee_crepes, FoodCategory.BREAKFAST, 14, 15, 2),
        Food("english_breakfast", R.string.food_english_breakfast, R.drawable.english_breakfast, FoodCategory.BREAKFAST, 28, 24, 2),
        Food("fruit_plate", R.string.food_fruit_plate, R.drawable.fruit_plate, FoodCategory.BREAKFAST, 22, 9, 5),
        Food("coffee", R.string.food_coffee, R.drawable.coffee, FoodCategory.BREAKFAST, 3, 4, 1),

        Food("carbonara", R.string.food_carbonara, R.drawable.carbonara, FoodCategory.DINNER, 26, 22, 2),
        Food("salad", R.string.food_salad, R.drawable.salad, FoodCategory.DINNER, 18, 7, 5),
        Food("soup", R.string.food_soup, R.drawable.soup, FoodCategory.DINNER, 20, 9, 4),
        Food("steak", R.string.food_steak, R.drawable.steak, FoodCategory.DINNER, 32, 22, 3),
        Food("grilled_steak", R.string.food_grilled_steak, R.drawable.grilled_steak, FoodCategory.DINNER, 30, 18, 4),

        Food("straw_fries", R.string.food_straw_fries, R.drawable.straw_fries, FoodCategory.SNACKS, 8, 12, 1),
        Food("burger", R.string.food_burger, R.drawable.burger, FoodCategory.SNACKS, 16, 20, 1),
        Food("cola", R.string.food_cola, R.drawable.cola, FoodCategory.SNACKS, 2, 4, 0),
        Food("hot_dog", R.string.food_hot_dog, R.drawable.hot_dog, FoodCategory.SNACKS, 12, 16, 1),
        Food("nuggets", R.string.food_nuggets, R.drawable.nuggets, FoodCategory.SNACKS, 10, 14, 1),
        Food("onion_rings", R.string.food_onion_rings, R.drawable.onion_rings, FoodCategory.SNACKS, 8, 12, 1),
        Food("doughnuts", R.string.food_doughnuts, R.drawable.doughnuts, FoodCategory.SNACKS, 6, 10, 1),
        Food("pizza", R.string.food_pizza, R.drawable.pizza, FoodCategory.SNACKS, 14, 14, 2),
    )

    val rooms: List<Room> = listOf(
        Room("bg0", R.string.room_default, RoomArt.Image(R.drawable.bg0), 0),
        Room("bg1", R.string.room_1, RoomArt.Image(R.drawable.bg1), 45),
        Room("bg2", R.string.room_2, RoomArt.Image(R.drawable.bg2), 40),
        Room("bg3", R.string.room_3, RoomArt.Image(R.drawable.bg3), 30),
        Room("sunset", R.string.room_sunset, RoomArt.Scene(SceneKind.SUNSET), 35),
        Room("night", R.string.room_night, RoomArt.Scene(SceneKind.NIGHT), 40),
        Room("ocean", R.string.room_ocean, RoomArt.Scene(SceneKind.OCEAN), 45),
        Room("blossom", R.string.room_blossom, RoomArt.Scene(SceneKind.BLOSSOM), 50),
    )

    const val DEFAULT_ROOM_ID = "bg0"

    fun food(id: String): Food? = foods.firstOrNull { it.id == id }
    fun room(id: String): Room = rooms.firstOrNull { it.id == id } ?: rooms.first()
}

package com.rares.virtualpetpompi.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

/** Steps walked on one calendar day, keyed by [java.time.LocalDate.toEpochDay]. */
@Entity(tableName = "step_day")
data class StepDay(
    @PrimaryKey val epochDay: Long,
    val steps: Int,
)

/** How many portions of one food the player owns. */
@Entity(tableName = "pantry")
data class PantryItem(
    @PrimaryKey val foodId: String,
    val count: Int,
)

@Dao
interface StepDayDao {
    @Query("INSERT INTO step_day (epochDay, steps) VALUES (:epochDay, :steps) " +
        "ON CONFLICT(epochDay) DO UPDATE SET steps = steps + :steps")
    suspend fun addSteps(epochDay: Long, steps: Int)

    @Query("SELECT * FROM step_day WHERE epochDay >= :fromEpochDay ORDER BY epochDay")
    fun observeSince(fromEpochDay: Long): Flow<List<StepDay>>

    @Query("SELECT COALESCE(SUM(steps), 0) FROM step_day")
    fun observeTotal(): Flow<Long>

    @Query("SELECT COALESCE(MAX(steps), 0) FROM step_day")
    fun observeRecord(): Flow<Int>

    @Query("SELECT COUNT(*) FROM step_day")
    fun observeDayCount(): Flow<Int>
}

@Dao
interface PantryDao {
    @Query("SELECT * FROM pantry WHERE count > 0")
    fun observeAll(): Flow<List<PantryItem>>

    @Query("INSERT INTO pantry (foodId, count) VALUES (:foodId, 1) " +
        "ON CONFLICT(foodId) DO UPDATE SET count = count + 1")
    suspend fun add(foodId: String)

    @Query("UPDATE pantry SET count = count - 1 WHERE foodId = :foodId AND count > 0")
    suspend fun consume(foodId: String): Int

    @Query("DELETE FROM pantry")
    suspend fun clear()
}

@Database(entities = [StepDay::class, PantryItem::class], version = 1, exportSchema = true)
abstract class PetDatabase : RoomDatabase() {
    abstract fun stepDays(): StepDayDao
    abstract fun pantry(): PantryDao

    companion object {
        fun create(context: Context): PetDatabase =
            Room.databaseBuilder(context, PetDatabase::class.java, "pet.db").build()
    }
}

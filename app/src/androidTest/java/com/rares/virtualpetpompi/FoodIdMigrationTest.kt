package com.rares.virtualpetpompi

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.rares.virtualpetpompi.data.PetDatabase
import com.rares.virtualpetpompi.domain.Catalog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/** A version 1 database (Romanian/English food ids) must open as version 2 with the portions kept. */
class FoodIdMigrationTest {
    @Test
    fun legacyFoodIdsAreRenamedAndKept() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "migration-test.db"
        context.deleteDatabase(name)
        val path = context.getDatabasePath(name)
        path.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(path, null).use { raw ->
            raw.execSQL("CREATE TABLE IF NOT EXISTS step_day (epochDay INTEGER NOT NULL, steps INTEGER NOT NULL, PRIMARY KEY(epochDay))")
            raw.execSQL("CREATE TABLE IF NOT EXISTS pantry (foodId TEXT NOT NULL, count INTEGER NOT NULL, PRIMARY KEY(foodId))")
            raw.execSQL("INSERT INTO pantry VALUES ('cirese', 2), ('steak1', 1), ('rice', 3)")
            raw.execSQL("CREATE TABLE room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT)")
            raw.execSQL("INSERT INTO room_master_table VALUES (42, '${currentV1Hash()}')")
            raw.version = 1
        }
        val db = Room.databaseBuilder(context, PetDatabase::class.java, name).addMigrations(PetDatabase.MIGRATION_1_2).build()
        val pantry = db.pantry().observeAll().first().associate { it.foodId to it.count }
        db.close()
        context.deleteDatabase(name)
        assertEquals(mapOf("cherries" to 2, "steak" to 1, "rice" to 3), pantry)
        pantry.keys.forEach { assertNotNull("$it is in the catalog", Catalog.food(it)) }
    }

    private fun currentV1Hash(): String =
        org.json.JSONObject(androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().context.assets.open("com.rares.virtualpetpompi.data.PetDatabase/1.json").bufferedReader().readText())
            .getJSONObject("database").getString("identityHash")
}

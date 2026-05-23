package com.druk.app.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.druk.app.domain.AppSettings
import com.druk.app.domain.DrinkLog
import com.druk.app.domain.FoodLevel
import com.druk.app.domain.MealLog
import com.druk.app.domain.SessionLog
import com.druk.app.domain.Sex
import com.druk.app.domain.TargetScheme
import com.druk.app.domain.UserProfile
import kotlinx.coroutines.flow.Flow

@Database(
    entities = [
        ProfileEntity::class,
        SessionEntity::class,
        DrinkEntity::class,
        MealEntity::class,
        SettingsEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class DrukDatabase : RoomDatabase() {
    abstract fun dao(): DrukDao

    companion object {
        @Volatile
        private var instance: DrukDatabase? = null

        fun get(context: Context): DrukDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    DrukDatabase::class.java,
                    "druk.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build().also { instance = it }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE drinks ADD COLUMN startedAtMillis INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE drinks ADD COLUMN endedAtMillis INTEGER")
                db.execSQL("UPDATE drinks SET startedAtMillis = timestampMillis, endedAtMillis = timestampMillis WHERE startedAtMillis = 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE sessions ADD COLUMN drinkName TEXT NOT NULL DEFAULT 'House drink'")
                db.execSQL("ALTER TABLE sessions ADD COLUMN drinkAbv REAL NOT NULL DEFAULT 42.8")
                db.execSQL("ALTER TABLE sessions ADD COLUMN caloriesPer30Ml REAL NOT NULL DEFAULT 71.0")
                db.execSQL(
                    """
                    UPDATE sessions
                    SET drinkName = COALESCE((SELECT drinkName FROM profiles WHERE id = 1), 'House drink'),
                        drinkAbv = COALESCE((SELECT drinkAbv FROM profiles WHERE id = 1), 42.8),
                        caloriesPer30Ml = COALESCE((SELECT caloriesPer30Ml FROM profiles WHERE id = 1), 71.0)
                    """.trimIndent()
                )
            }
        }
    }
}

@Dao
interface DrukDao {
    @Query("SELECT * FROM profiles WHERE id = 1")
    fun observeProfile(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profiles WHERE id = 1")
    suspend fun getProfile(): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: ProfileEntity)

    @Query("SELECT * FROM settings WHERE id = 1")
    fun observeSettings(): Flow<SettingsEntity?>

    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun getSettings(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSettings(settings: SettingsEntity)

    @Query("SELECT * FROM sessions WHERE endedAtMillis IS NULL ORDER BY startedAtMillis DESC LIMIT 1")
    fun observeActiveSession(): Flow<SessionEntity?>

    @Query("SELECT * FROM sessions WHERE endedAtMillis IS NULL ORDER BY startedAtMillis DESC LIMIT 1")
    suspend fun getActiveSession(): SessionEntity?

    @Query("SELECT * FROM sessions WHERE endedAtMillis IS NOT NULL ORDER BY startedAtMillis DESC LIMIT 20")
    fun observeCompletedSessions(): Flow<List<SessionEntity>>

    @Insert
    suspend fun insertSession(session: SessionEntity): Long

    @Query("UPDATE sessions SET endedAtMillis = :endedAtMillis WHERE id = :sessionId")
    suspend fun endSession(sessionId: Long, endedAtMillis: Long)

    @Query("SELECT * FROM drinks WHERE sessionId = :sessionId ORDER BY timestampMillis DESC")
    fun observeDrinks(sessionId: Long): Flow<List<DrinkEntity>>

    @Query("SELECT * FROM meals WHERE sessionId = :sessionId ORDER BY timestampMillis DESC")
    fun observeMeals(sessionId: Long): Flow<List<MealEntity>>

    @Insert
    suspend fun insertDrink(drink: DrinkEntity): Long

    @Query("SELECT * FROM drinks WHERE sessionId = :sessionId AND endedAtMillis IS NULL ORDER BY startedAtMillis DESC LIMIT 1")
    suspend fun getActiveDrink(sessionId: Long): DrinkEntity?

    @Query("UPDATE drinks SET endedAtMillis = :endedAtMillis, timestampMillis = :endedAtMillis WHERE id = :drinkId")
    suspend fun endDrink(drinkId: Long, endedAtMillis: Long)

    @Insert
    suspend fun insertMeal(meal: MealEntity): Long

    @Query("DELETE FROM drinks WHERE id = :drinkId")
    suspend fun deleteDrink(drinkId: Long)

    @Query("DELETE FROM meals WHERE id = :mealId")
    suspend fun deleteMeal(mealId: Long)

    @Query("SELECT * FROM drinks WHERE sessionId = :sessionId ORDER BY timestampMillis DESC LIMIT 1")
    suspend fun getLatestDrink(sessionId: Long): DrinkEntity?

    @Query("SELECT * FROM meals WHERE sessionId = :sessionId ORDER BY timestampMillis DESC LIMIT 1")
    suspend fun getLatestMeal(sessionId: Long): MealEntity?
}

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val sex: String,
    val weightKg: Double,
    val heightCm: Double,
    val r: Double,
    val drinkName: String,
    val drinkAbv: Double,
    val caloriesPer30Ml: Double
) {
    fun toDomain(): UserProfile {
        return UserProfile(
            name = name,
            sex = Sex.valueOf(sex),
            weightKg = weightKg,
            heightCm = heightCm,
            r = r,
            drinkName = drinkName,
            drinkAbv = drinkAbv,
            caloriesPer30Ml = caloriesPer30Ml
        )
    }
}

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val selectedScheme: String?,
    val foodLevel: String,
    val notificationsEnabled: Boolean
) {
    fun toDomain(): AppSettings {
        return AppSettings(
            selectedScheme = selectedScheme?.let { TargetScheme.valueOf(it) },
            foodLevel = FoodLevel.valueOf(foodLevel),
            notificationsEnabled = notificationsEnabled
        )
    }
}

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAtMillis: Long,
    val endedAtMillis: Long? = null,
    val drinkName: String = "House drink",
    val drinkAbv: Double = 42.8,
    val caloriesPer30Ml: Double = 71.0
) {
    fun toDomain(): SessionLog {
        return SessionLog(
            id = id,
            startedAtMillis = startedAtMillis,
            endedAtMillis = endedAtMillis,
            drinkName = drinkName,
            drinkAbv = drinkAbv,
            caloriesPer30Ml = caloriesPer30Ml
        )
    }
}

@Entity(tableName = "drinks")
data class DrinkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val timestampMillis: Long,
    val startedAtMillis: Long,
    val endedAtMillis: Long?,
    val volumeMl: Double,
    val abv: Double,
    val gramsAlcohol: Double,
    val calories: Double,
    val foodLevel: String
) {
    fun toDomain(): DrinkLog {
        return DrinkLog(
            id = id,
            sessionId = sessionId,
            timestampMillis = timestampMillis,
            startedAtMillis = startedAtMillis,
            endedAtMillis = endedAtMillis,
            volumeMl = volumeMl,
            abv = abv,
            gramsAlcohol = gramsAlcohol,
            calories = calories,
            foodLevel = FoodLevel.valueOf(foodLevel)
        )
    }
}

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val timestampMillis: Long,
    val foodLevel: String
) {
    fun toDomain(): MealLog {
        return MealLog(
            id = id,
            sessionId = sessionId,
            timestampMillis = timestampMillis,
            foodLevel = FoodLevel.valueOf(foodLevel)
        )
    }
}

fun UserProfile.toEntity(): ProfileEntity {
    return ProfileEntity(
        name = name,
        sex = sex.name,
        weightKg = weightKg,
        heightCm = heightCm,
        r = r,
        drinkName = drinkName,
        drinkAbv = drinkAbv,
        caloriesPer30Ml = caloriesPer30Ml
    )
}

fun AppSettings.toEntity(): SettingsEntity {
    return SettingsEntity(
        selectedScheme = selectedScheme?.name,
        foodLevel = foodLevel.name,
        notificationsEnabled = notificationsEnabled
    )
}

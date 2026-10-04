package com.example.data

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
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val updatedAtMs: Long,
    val createdAtMs: Long,
    val durationMs: Long,
    val aspectRatioLabel: String,
    val exportResolution: String,
    val exportFps: Int = 30,
    val coverDrawableRes: Int,
    val thumbnailUri: String = "",
    val clipsCount: Int,
    val effectsCount: Int = 0,
    val keyframesCount: Int = 0,
    val fileSizeEstimateMb: Float = 4.0f,
    val isDraft: Boolean = true,
    val isRecoveredDraft: Boolean = false,
    val lastPlayheadMs: Long = 0L,
    val projectJson: String
)

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY updatedAtMs DESC")
    fun observeAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE isDraft = 1 ORDER BY updatedAtMs DESC LIMIT :limit")
    fun observeRecentDrafts(limit: Int): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects ORDER BY updatedAtMs DESC")
    suspend fun getAllProjectsOnce(): List<ProjectEntity>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: String): ProjectEntity?

    @Query("SELECT * FROM projects WHERE isDraft = 1 ORDER BY updatedAtMs DESC LIMIT 1")
    suspend fun getLatestDraft(): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProject(entity: ProjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAllProjects(entities: List<ProjectEntity>)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: String)

    @Query("UPDATE projects SET name = :newName, updatedAtMs = :updatedAtMs WHERE id = :id")
    suspend fun renameProject(id: String, newName: String, updatedAtMs: Long)
}

@Database(entities = [ProjectEntity::class], version = 6, exportSchema = false)
abstract class ProjectDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao

    companion object {
        @Volatile
        private var INSTANCE: ProjectDatabase? = null

        fun getInstance(context: Context): ProjectDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ProjectDatabase::class.java,
                    "novacut_studio_v6.db"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

package com.example.data

import com.example.engine.EffectFilterTransitionCatalog
import com.example.engine.VideoRenderEngine
import com.example.model.VideoProject
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class ProjectRepository(private val projectDao: ProjectDao) {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val projectAdapter = moshi.adapter(VideoProject::class.java)

    val allProjectsFlow: Flow<List<VideoProject>> = projectDao.observeAllProjects().map { entities ->
        entities.mapNotNull { entity ->
            deserializeProject(entity)
        }
    }

    suspend fun ensureSeedProjects() = withContext(Dispatchers.IO) {
        val current = projectDao.getAllProjectsOnce()
        if (current.isEmpty()) {
            val starterTemplates = EffectFilterTransitionCatalog.templates.take(3)
            val now = System.currentTimeMillis()
            starterTemplates.forEachIndexed { idx, tpl ->
                val proj = EffectFilterTransitionCatalog.buildProjectFromTemplate(tpl).copy(
                    id = "seed_proj_${idx + 1}",
                    createdAtMs = now - (idx + 1) * 3600_000L,
                    updatedAtMs = now - idx * 1200_000L
                )
                saveProject(proj)
            }
        }
    }

    suspend fun saveProject(project: VideoProject, isRecoveredDraft: Boolean = false) = withContext(Dispatchers.IO) {
        val updated = project.copy(updatedAtMs = System.currentTimeMillis())
        val durationMs = VideoRenderEngine.computeProjectTotalDurationMs(updated)
        val coverRes = updated.primaryClips.firstOrNull()?.sampleDrawableRes ?: updated.coverDrawableRes
        val json = try {
            projectAdapter.toJson(updated)
        } catch (_: Exception) {
            ""
        }
        val entity = ProjectEntity(
            id = updated.id,
            name = updated.name,
            updatedAtMs = updated.updatedAtMs,
            createdAtMs = updated.createdAtMs,
            durationMs = durationMs,
            aspectRatioLabel = updated.aspectRatio.label,
            exportResolution = updated.exportResolution,
            coverDrawableRes = coverRes,
            clipsCount = updated.primaryClips.size + updated.overlayClips.size,
            isRecoveredDraft = isRecoveredDraft,
            projectJson = json
        )
        projectDao.upsertProject(entity)
    }

    suspend fun loadProjectById(id: String): VideoProject? = withContext(Dispatchers.IO) {
        val entity = projectDao.getProjectById(id) ?: return@withContext null
        deserializeProject(entity)
    }

    suspend fun duplicateProject(project: VideoProject): VideoProject = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val copy = project.copy(
            id = UUID.randomUUID().toString(),
            name = "${project.name} (Copy)",
            createdAtMs = now,
            updatedAtMs = now
        )
        saveProject(copy)
        copy
    }

    suspend fun renameProject(project: VideoProject, newName: String): VideoProject = withContext(Dispatchers.IO) {
        val trimmed = newName.trim().ifBlank { "Untitled Project" }
        val updated = project.copy(name = trimmed, updatedAtMs = System.currentTimeMillis())
        saveProject(updated)
        updated
    }

    suspend fun deleteProject(projectId: String) = withContext(Dispatchers.IO) {
        projectDao.deleteProjectById(projectId)
    }

    private fun deserializeProject(entity: ProjectEntity): VideoProject? {
        return try {
            if (entity.projectJson.isNotBlank()) {
                projectAdapter.fromJson(entity.projectJson)?.copy(
                    id = entity.id,
                    name = entity.name,
                    updatedAtMs = entity.updatedAtMs
                )
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}

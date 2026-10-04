package com.example.data

import com.example.engine.EffectFilterTransitionCatalog
import com.example.engine.VideoRenderEngine
import com.example.model.AspectRatioMode
import com.example.model.TimelineClip
import com.example.model.VideoProject
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Repository abstracting local Room database operations for projects and drafts.
 */
class ProjectRepository(private val projectDao: ProjectDao) {

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private val projectAdapter by lazy {
        moshi.adapter(VideoProject::class.java)
    }

    val allProjectsFlow: Flow<List<VideoProject>> = projectDao.observeAllProjects().map { entities ->
        val parsed = entities.mapNotNull { entity -> deserializeProject(entity) }
        parsed.ifEmpty { EffectFilterTransitionCatalog.buildDefaultSeedProjects() }
    }

    val recentDraftEntitiesFlow: Flow<List<ProjectEntity>> = projectDao.observeRecentDrafts(15)

    suspend fun ensureSeedProjects() = withContext(Dispatchers.IO) {
        val current = projectDao.getAllProjectsOnce()
        if (current.isEmpty()) {
            val seedList = EffectFilterTransitionCatalog.buildDefaultSeedProjects()
            val entities = seedList.mapIndexed { idx, proj ->
                buildEntity(
                    project = proj,
                    isDraft = true,
                    isRecoveredDraft = (idx == 0),
                    lastPlayheadMs = if (idx == 0) 2400L else 0L
                )
            }
            projectDao.upsertAllProjects(entities)
        }
    }

    suspend fun saveProject(
        project: VideoProject,
        isDraft: Boolean = true,
        isRecoveredDraft: Boolean = false,
        lastPlayheadMs: Long = 0L
    ) = withContext(Dispatchers.IO) {
        val updated = project.copy(updatedAtMs = System.currentTimeMillis())
        val entity = buildEntity(
            project = updated,
            isDraft = isDraft,
            isRecoveredDraft = isRecoveredDraft,
            lastPlayheadMs = lastPlayheadMs
        )
        projectDao.upsertProject(entity)
    }

    private fun buildEntity(
        project: VideoProject,
        isDraft: Boolean,
        isRecoveredDraft: Boolean,
        lastPlayheadMs: Long
    ): ProjectEntity {
        val durationMs = VideoRenderEngine.computeProjectTotalDurationMs(project)
        val firstClip = project.primaryClips.firstOrNull()
        val coverRes = EffectFilterTransitionCatalog.safeDrawableRes(
            firstClip?.sampleDrawableRes ?: project.coverDrawableRes
        )
        val thumbUri = firstClip?.mediaUri.orEmpty()
        val totalKeyframes = project.primaryClips.sumOf { it.keyframes.size } +
            project.overlayClips.sumOf { it.keyframes.size } +
            project.textClips.sumOf { it.keyframes.size } +
            project.effectItems.sumOf { it.keyframes.size }
        val estMb = ((durationMs / 1000f) * 2.4f).coerceAtLeast(3.2f)

        val json = try {
            projectAdapter.toJson(project)
        } catch (_: Throwable) {
            ""
        }

        return ProjectEntity(
            id = project.id,
            name = project.name,
            updatedAtMs = project.updatedAtMs,
            createdAtMs = project.createdAtMs,
            durationMs = durationMs,
            aspectRatioLabel = project.aspectRatio.label,
            exportResolution = project.exportResolution,
            exportFps = project.exportFps,
            coverDrawableRes = coverRes,
            thumbnailUri = thumbUri,
            clipsCount = project.primaryClips.size + project.overlayClips.size,
            effectsCount = project.effectItems.size,
            keyframesCount = totalKeyframes,
            fileSizeEstimateMb = estMb,
            isDraft = isDraft,
            isRecoveredDraft = isRecoveredDraft,
            lastPlayheadMs = lastPlayheadMs,
            projectJson = json
        )
    }

    suspend fun loadProjectById(id: String): VideoProject? = withContext(Dispatchers.IO) {
        val entity = projectDao.getProjectById(id) ?: return@withContext null
        deserializeProject(entity)
    }

    suspend fun getLatestDraftProject(): Pair<VideoProject, Long>? = withContext(Dispatchers.IO) {
        val entity = projectDao.getLatestDraft() ?: return@withContext null
        val project = deserializeProject(entity) ?: return@withContext null
        project to entity.lastPlayheadMs
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

    fun deserializeProject(entity: ProjectEntity): VideoProject? {
        try {
            if (entity.projectJson.isNotBlank()) {
                val parsed = projectAdapter.fromJson(entity.projectJson)
                if (parsed != null) {
                    return parsed.copy(
                        id = entity.id,
                        name = entity.name,
                        updatedAtMs = entity.updatedAtMs,
                        coverDrawableRes = EffectFilterTransitionCatalog.safeDrawableRes(parsed.coverDrawableRes),
                        primaryClips = parsed.primaryClips.map {
                            it.copy(sampleDrawableRes = EffectFilterTransitionCatalog.safeDrawableRes(it.sampleDrawableRes))
                        },
                        overlayClips = parsed.overlayClips.map {
                            it.copy(sampleDrawableRes = EffectFilterTransitionCatalog.safeDrawableRes(it.sampleDrawableRes))
                        }
                    )
                }
            }
        } catch (_: Throwable) {
            // Fall through to metadata reconstruction
        }

        val matchingSeed = EffectFilterTransitionCatalog.buildDefaultSeedProjects().find { it.id == entity.id }
        if (matchingSeed != null) {
            return matchingSeed.copy(
                name = entity.name,
                updatedAtMs = entity.updatedAtMs
            )
        }

        val safeCover = EffectFilterTransitionCatalog.safeDrawableRes(entity.coverDrawableRes)
        val aspect = AspectRatioMode.entries.find { it.label == entity.aspectRatioLabel } ?: AspectRatioMode.RATIO_16_9
        val clipCount = entity.clipsCount.coerceAtLeast(1)
        val clipDur = (entity.durationMs / clipCount).coerceAtLeast(3000L)
        return VideoProject(
            id = entity.id,
            name = entity.name,
            createdAtMs = entity.createdAtMs,
            updatedAtMs = entity.updatedAtMs,
            aspectRatio = aspect,
            exportResolution = entity.exportResolution,
            exportFps = entity.exportFps,
            coverDrawableRes = safeCover,
            primaryClips = (0 until clipCount).map { idx ->
                TimelineClip(
                    title = "${entity.name} Clip ${idx + 1}",
                    sampleDrawableRes = safeCover,
                    sourceDurationMs = clipDur,
                    trimStartMs = 0L,
                    trimEndMs = clipDur
                )
            }
        )
    }
}

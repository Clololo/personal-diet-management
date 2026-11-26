package com.smartnutrient.app

import android.content.Context
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocalStore(private val context: Context) {
    private val file = File(context.filesDir, "smartnutrient.json")
    private val tempFile = File(context.filesDir, "smartnutrient.json.tmp")

    fun load(): LoadedState {
        if (!file.exists()) return LoadedState(PersistedState())
        return runCatching {
            val decoded = DataCodec.decode(file.readText())
            LoadedState(PersistedState(
                days = decoded.logsByDate.mapValues { (date, logs) -> DayLog(date, logs, decoded.waterByDate[date] ?: 0) },
                profile = decoded.profile,
                profileVersions = decoded.profileVersions,
                activityPlans = decoded.activityPlans,
                templates = decoded.templates,
                measurements = decoded.measurements,
                adaptiveSettings = decoded.adaptiveSettings,
                customFoods = decoded.customFoods,
                favorites = decoded.favorites,
                recentFoodIds = decoded.recentFoodIds,
                ignoredRecommendationNames = decoded.ignoredRecommendationNames,
            ))
        }.getOrElse {
            val stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
            val corrupt = File(context.filesDir, "smartnutrient-corrupt-$stamp.json")
            runCatching { file.copyTo(corrupt, overwrite = false) }
            writeFailureMarker()
            LoadedState(PersistedState(), "本地数据无法读取，原文件已保留为 ${corrupt.name}。本次显示为空数据，且在你继续保存前不会覆盖原文件。")
        }
    }

    fun save(state: PersistedState) {
        if (failureMarker.exists()) return
        tempFile.writeText(DataCodec.exportState(state))
        if (!tempFile.renameTo(file)) {
            file.writeText(tempFile.readText())
            tempFile.delete()
        }
    }

    fun exportBackup(state: PersistedState): String = DataCodec.exportState(state)

    fun importBackup(json: String): PersistedState {
        require(DataCodec.isValidBackup(json)) { "Invalid SmartNutrient backup" }
        val decoded = DataCodec.decode(json)
        return PersistedState(
            days = decoded.logsByDate.mapValues { (date, logs) -> DayLog(date, logs, decoded.waterByDate[date] ?: 0) },
            profile = decoded.profile,
            profileVersions = decoded.profileVersions,
            activityPlans = decoded.activityPlans,
            templates = decoded.templates,
            measurements = decoded.measurements,
            adaptiveSettings = decoded.adaptiveSettings,
            customFoods = decoded.customFoods,
            favorites = decoded.favorites,
            recentFoodIds = decoded.recentFoodIds,
            ignoredRecommendationNames = decoded.ignoredRecommendationNames,
        )
    }

    fun writeNamedBackup(json: String): File {
        val directory = File(context.cacheDir, "backups").apply { mkdirs() }
        val target = File(directory, AutoBackupRules.fileName())
        target.writeText(json)
        return target
    }

    fun saveRestoreSnapshot(json: String): File {
        val directory = File(context.filesDir, "restore-snapshots").apply { mkdirs() }
        val target = File(directory, AutoBackupRules.fileName())
        target.writeText(json)
        val old = directory.listFiles()?.sortedByDescending { it.lastModified() }?.drop(3).orEmpty()
        old.forEach { runCatching { it.delete() } }
        return target
    }
    fun restoreSnapshots(): List<File> {
        val directory = File(context.filesDir, "restore-snapshots")
        return directory.listFiles()?.sortedByDescending { it.lastModified() }.orEmpty()
    }

    fun automaticBackupFiles(): List<AutoBackupFile> {
        val directory = automaticBackupDirectory()
        return directory.listFiles().orEmpty().map { file ->
            AutoBackupFile(
                name = file.name,
                modifiedAt = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(file.lastModified()), java.time.ZoneId.systemDefault()),
                sizeBytes = file.length(),
            )
        }
    }

    fun ensureDailyAutomaticBackup(state: PersistedState): AutoBackupFile? {
        val existing = automaticBackupFiles()
        val now = LocalDateTime.now()
        if (!AutoBackupRules.needsDailyBackup(existing, now)) return null
        val directory = automaticBackupDirectory()
        val target = File(directory, AutoBackupRules.autoBackupFileName(now))
        target.writeText(DataCodec.exportState(state))
        AutoBackupRules.keepNewest(automaticBackupFiles(), 7)
            .map { File(directory, it.name) }
            .toSet()
            .let { keep -> directory.listFiles().orEmpty().filter { it !in keep }.forEach { runCatching { it.delete() } } }
        return AutoBackupFile(target.name, now, target.length())
    }

    fun automaticBackupDirectory(): File =
        File(context.filesDir, "automatic-backups").apply { mkdirs() }

    private val failureMarker = File(context.filesDir, "smartnutrient-read-error.marker")

    private fun writeFailureMarker() {
        runCatching { failureMarker.writeText("Load failed; original smartnutrient.json is preserved.") }
    }
}

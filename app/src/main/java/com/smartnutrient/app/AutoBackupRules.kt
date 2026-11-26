package com.smartnutrient.app

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class RestoreSnapshot(
    val json: String,
    val dayCount: Int,
    val entryCount: Int,
)

data class AutoBackupFile(
    val name: String,
    val modifiedAt: LocalDateTime,
    val sizeBytes: Long,
)

object AutoBackupRules {
    private val formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")

    fun snapshotBeforeRestore(state: PersistedState): RestoreSnapshot? {
        val entryCount = state.days.values.sumOf { it.entries.size }
        if (entryCount == 0 && state.customFoods.isEmpty()) return null
        return RestoreSnapshot(
            json = DataCodec.exportState(state),
            dayCount = state.days.size,
            entryCount = entryCount,
        )
    }

    fun fileName(now: LocalDateTime = LocalDateTime.now()): String =
        "SmartNutrient-备份-${now.format(formatter)}.json"

    fun needsDailyBackup(existing: List<AutoBackupFile>, now: LocalDateTime = LocalDateTime.now()): Boolean =
        existing.none { it.modifiedAt.toLocalDate() == now.toLocalDate() }

    fun keepNewest(existing: List<AutoBackupFile>, limit: Int = 7): List<AutoBackupFile> =
        existing.sortedByDescending { it.modifiedAt }.take(limit.coerceAtLeast(0))

    fun autoBackupFileName(now: LocalDateTime = LocalDateTime.now()): String =
        "SmartNutrient-自动备份-${now.format(formatter)}.json"
}

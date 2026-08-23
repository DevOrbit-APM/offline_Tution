package com.tuition.offline.backup

import com.tuition.offline.data.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/**
 * Simple unencrypted JSON backup. This is intentionally local and offline.
 * Future versions should preserve backward compatibility when the schema evolves.
 */
@Serializable data class BackupEnvelope(
    val appVersion: Int = 1,
    val createdAt: Long,
    val note: String = "Offline Tuition Manager Backup"
)

class BackupManager(private val db: AppDatabase) {
    suspend fun exportMetadata(output: OutputStream) {
        val envelope = BackupEnvelope(createdAt = System.currentTimeMillis())
        output.write(Json { prettyPrint = true }.encodeToString(envelope).toByteArray())
    }

    // Data export/import implementation is isolated here so full entity serialization
    // can be expanded without touching payment business rules.
    // The project README documents the recommended v1 full-data JSON format.
}

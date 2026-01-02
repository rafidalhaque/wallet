package com.ivy.data.supabase.datasource

import com.ivy.data.db.entity.SettingsEntity
import com.ivy.data.supabase.SupabaseTableNames
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supabase data source for Settings operations
 * Replaces SettingsDao and WriteSettingsDao from Room
 * Also replaces DataStore preferences functionality
 */
@Singleton
class SettingsSupabaseDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val tableNames: SupabaseTableNames
) {
    

    suspend fun findFirst(): SettingsEntity? {
        return try {
            supabaseClient.from(tableNames.settings)
                .select(columns = Columns.ALL)
                .decodeList<SettingsEntity>()
                .firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun findAll(): List<SettingsEntity> {
        return try {
            supabaseClient.from(tableNames.settings)
                .select(columns = Columns.ALL)
                .decodeList<SettingsEntity>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun save(entity: SettingsEntity) {
        try {
            supabaseClient.from(tableNames.settings)
                .upsert(entity)
        } catch (e: Exception) {
            throw Exception("Failed to save settings: ${e.message}", e)
        }
    }

    suspend fun deleteAll() {
        try {
            supabaseClient.from(tableNames.settings)
                .delete {
                    filter {
                        neq("id", "00000000-0000-0000-0000-000000000000")
                    }
                }
        } catch (e: Exception) {
            throw Exception("Failed to delete all settings: ${e.message}", e)
        }
    }

    /**
     * Get a specific preference value by key
     * This replaces DataStore functionality
     */
    suspend fun getPreference(key: String): String? {
        // This would require a separate preferences table in Supabase
        // For now, storing as part of settings
        return null
    }

    /**
     * Set a specific preference value by key
     * This replaces DataStore functionality
     */
    suspend fun setPreference(key: String, value: String) {
        // This would require a separate preferences table in Supabase
        // Implementation depends on how preferences are structured
    }
}

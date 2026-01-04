package com.ivy.data.supabase.datasource

import com.ivy.data.db.entity.TagEntity
import com.ivy.data.supabase.SupabaseDataException
import com.ivy.data.supabase.SupabaseTableNames
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supabase data source for Tag operations
 * Replaces TagDao and WriteTagDao from Room
 */
@Singleton
class TagSupabaseDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val tableNames: SupabaseTableNames
) {

    suspend fun findAll(): List<TagEntity> {
        return try {
            supabaseClient.from(tableNames.tags)
                .select(columns = Columns.ALL) {
                    filter {
                        eq("isDeleted", false)
                    }
                }
                .decodeList<TagEntity>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun findById(id: UUID): TagEntity? {
        return try {
            supabaseClient.from(tableNames.tags)
                .select(columns = Columns.ALL) {
                    filter {
                        eq("id", id.toString())
                        eq("isDeleted", false)
                    }
                }
                .decodeSingleOrNull<TagEntity>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun findByIds(ids: List<UUID>): List<TagEntity> {
        return try {
            supabaseClient.from(tableNames.tags)
                .select(columns = Columns.ALL) {
                    filter {
                        isIn("id", ids.map { it.toString() })
                        eq("isDeleted", false)
                    }
                }
                .decodeList<TagEntity>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun save(entity: TagEntity) {
        try {
            supabaseClient.from(tableNames.tags)
                .upsert(entity)
        } catch (e: Exception) {
            throw SupabaseDataException("Failed to save tag: ${e.message}", e)
        }
    }

    suspend fun saveMany(entities: List<TagEntity>) {
        try {
            supabaseClient.from(tableNames.tags)
                .upsert(entities)
        } catch (e: Exception) {
            throw SupabaseDataException("Failed to save tags: ${e.message}", e)
        }
    }

    suspend fun deleteById(id: UUID) {
        try {
            supabaseClient.from(tableNames.tags)
                .delete {
                    filter {
                        eq("id", id.toString())
                    }
                }
        } catch (e: Exception) {
            throw SupabaseDataException("Failed to delete tag: ${e.message}", e)
        }
    }

    suspend fun deleteAll() {
        try {
            supabaseClient.from(tableNames.tags)
                .delete {
                    filter {
                        neq("id", "00000000-0000-0000-0000-000000000000")
                    }
                }
        } catch (e: Exception) {
            throw SupabaseDataException("Failed to delete all tags: ${e.message}", e)
        }
    }
}

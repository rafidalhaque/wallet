package com.ivy.data.supabase.datasource

import com.ivy.data.db.entity.TagAssociationEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supabase data source for Tag Association operations
 * Replaces TagAssociationDao and WriteTagAssociationDao from Room
 */
@Singleton
class TagAssociationSupabaseDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    private val tableName = "tags_association"

    suspend fun findByTagId(tagId: UUID): List<TagAssociationEntity> {
        return try {
            supabaseClient.from(tableName)
                .select(columns = Columns.ALL) {
                    filter {
                        eq("tagId", tagId.toString())
                        eq("isDeleted", false)
                    }
                }
                .decodeList<TagAssociationEntity>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun findByAssociatedId(associatedId: UUID): List<TagAssociationEntity> {
        return try {
            supabaseClient.from(tableName)
                .select(columns = Columns.ALL) {
                    filter {
                        eq("associatedId", associatedId.toString())
                        eq("isDeleted", false)
                    }
                }
                .decodeList<TagAssociationEntity>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun findByAssociatedIds(associatedIds: List<UUID>): List<TagAssociationEntity> {
        return try {
            supabaseClient.from(tableName)
                .select(columns = Columns.ALL) {
                    filter {
                        isIn("associatedId", associatedIds.map { it.toString() })
                        eq("isDeleted", false)
                    }
                }
                .decodeList<TagAssociationEntity>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun findAll(): List<TagAssociationEntity> {
        return try {
            supabaseClient.from(tableName)
                .select(columns = Columns.ALL) {
                    filter {
                        eq("isDeleted", false)
                    }
                }
                .decodeList<TagAssociationEntity>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun save(entity: TagAssociationEntity) {
        try {
            supabaseClient.from(tableName)
                .upsert(entity)
        } catch (e: Exception) {
            throw Exception("Failed to save tag association: ${e.message}", e)
        }
    }

    suspend fun saveMany(entities: List<TagAssociationEntity>) {
        try {
            supabaseClient.from(tableName)
                .upsert(entities)
        } catch (e: Exception) {
            throw Exception("Failed to save tag associations: ${e.message}", e)
        }
    }

    suspend fun deleteByTagIdAndAssociatedId(tagId: UUID, associatedId: UUID) {
        try {
            supabaseClient.from(tableName)
                .delete {
                    filter {
                        eq("tagId", tagId.toString())
                        eq("associatedId", associatedId.toString())
                    }
                }
        } catch (e: Exception) {
            throw Exception("Failed to delete tag association: ${e.message}", e)
        }
    }

    suspend fun deleteByAssociatedId(associatedId: UUID) {
        try {
            supabaseClient.from(tableName)
                .delete {
                    filter {
                        eq("associatedId", associatedId.toString())
                    }
                }
        } catch (e: Exception) {
            throw Exception("Failed to delete tag associations: ${e.message}", e)
        }
    }

    suspend fun deleteAll() {
        try {
            supabaseClient.from(tableName)
                .delete {
                    filter {
                        neq("tagId", "00000000-0000-0000-0000-000000000000")
                    }
                }
        } catch (e: Exception) {
            throw Exception("Failed to delete all tag associations: ${e.message}", e)
        }
    }
}

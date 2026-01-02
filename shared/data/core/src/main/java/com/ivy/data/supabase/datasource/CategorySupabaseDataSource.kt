package com.ivy.data.supabase.datasource

import com.ivy.data.db.entity.CategoryEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supabase data source for Category operations
 * Replaces CategoryDao and WriteCategoryDao from Room
 */
@Singleton
class CategorySupabaseDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    private val tableName = "categories"

    suspend fun findAll(): List<CategoryEntity> {
        return try {
            supabaseClient.from(tableName)
                .select(columns = Columns.ALL) {
                    filter {
                        eq("isDeleted", false)
                    }
                }
                .decodeList<CategoryEntity>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun findById(id: UUID): CategoryEntity? {
        return try {
            supabaseClient.from(tableName)
                .select(columns = Columns.ALL) {
                    filter {
                        eq("id", id.toString())
                        eq("isDeleted", false)
                    }
                }
                .decodeSingleOrNull<CategoryEntity>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun findMaxOrderNum(): Double? {
        return try {
            val categories = findAll()
            categories.maxOfOrNull { it.orderNum }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun save(entity: CategoryEntity) {
        try {
            supabaseClient.from(tableName)
                .upsert(entity)
        } catch (e: Exception) {
            throw Exception("Failed to save category: ${e.message}", e)
        }
    }

    suspend fun saveMany(entities: List<CategoryEntity>) {
        try {
            supabaseClient.from(tableName)
                .upsert(entities)
        } catch (e: Exception) {
            throw Exception("Failed to save categories: ${e.message}", e)
        }
    }

    suspend fun deleteById(id: UUID) {
        try {
            supabaseClient.from(tableName)
                .delete {
                    filter {
                        eq("id", id.toString())
                    }
                }
        } catch (e: Exception) {
            throw Exception("Failed to delete category: ${e.message}", e)
        }
    }

    suspend fun deleteAll() {
        try {
            supabaseClient.from(tableName)
                .delete {
                    filter {
                        neq("id", "00000000-0000-0000-0000-000000000000")
                    }
                }
        } catch (e: Exception) {
            throw Exception("Failed to delete all categories: ${e.message}", e)
        }
    }
}

package com.ivy.data.supabase.datasource

import com.ivy.data.db.entity.UserEntity
import com.ivy.data.supabase.SupabaseTableNames
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supabase data source for User operations
 * Replaces UserDao from Room
 * Note: UserEntity is marked as deprecated/legacy in Room
 */
@Singleton
class UserSupabaseDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val tableNames: SupabaseTableNames
) {
    

    suspend fun findAll(): List<UserEntity> {
        return try {
            supabaseClient.from(tableNames.users)
                .select(columns = Columns.ALL)
                .decodeList<UserEntity>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun findById(id: UUID): UserEntity? {
        return try {
            supabaseClient.from(tableNames.users)
                .select(columns = Columns.ALL) {
                    filter {
                        eq("id", id.toString())
                    }
                }
                .decodeSingleOrNull<UserEntity>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun save(entity: UserEntity) {
        try {
            supabaseClient.from(tableNames.users)
                .upsert(entity)
        } catch (e: Exception) {
            throw Exception("Failed to save user: ${e.message}", e)
        }
    }

    suspend fun deleteAll() {
        try {
            supabaseClient.from(tableNames.users)
                .delete {
                    filter {
                        neq("id", "00000000-0000-0000-0000-000000000000")
                    }
                }
        } catch (e: Exception) {
            throw Exception("Failed to delete all users: ${e.message}", e)
        }
    }
}

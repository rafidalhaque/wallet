package com.ivy.data.supabase.datasource

import com.ivy.data.db.entity.AccountEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supabase data source for Account operations
 * Replaces AccountDao and WriteAccountDao from Room
 */
@Singleton
class AccountSupabaseDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    private val tableName = "accounts"

    suspend fun findById(id: UUID): AccountEntity? {
        return try {
            supabaseClient.from(tableName)
                .select(columns = Columns.ALL) {
                    filter {
                        eq("id", id.toString())
                    }
                }
                .decodeSingleOrNull<AccountEntity>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun findAll(): List<AccountEntity> {
        return try {
            supabaseClient.from(tableName)
                .select(columns = Columns.ALL)
                .decodeList<AccountEntity>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun findMaxOrderNum(): Double? {
        return try {
            val accounts = findAll()
            accounts.maxOfOrNull { it.orderNum }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun save(entity: AccountEntity) {
        try {
            supabaseClient.from(tableName)
                .upsert(entity)
        } catch (e: Exception) {
            throw Exception("Failed to save account: ${e.message}", e)
        }
    }

    suspend fun saveMany(entities: List<AccountEntity>) {
        try {
            supabaseClient.from(tableName)
                .upsert(entities)
        } catch (e: Exception) {
            throw Exception("Failed to save accounts: ${e.message}", e)
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
            throw Exception("Failed to delete account: ${e.message}", e)
        }
    }

    suspend fun deleteAll() {
        try {
            supabaseClient.from(tableName)
                .delete {
                    filter {
                        // Delete all records
                        neq("id", "00000000-0000-0000-0000-000000000000")
                    }
                }
        } catch (e: Exception) {
            throw Exception("Failed to delete all accounts: ${e.message}", e)
        }
    }
}

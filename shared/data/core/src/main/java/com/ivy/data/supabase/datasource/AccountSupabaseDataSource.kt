package com.ivy.data.supabase.datasource

import com.ivy.data.db.entity.AccountEntity
import com.ivy.data.supabase.SupabaseDataException
import com.ivy.data.supabase.SupabaseTableNames
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
    private val supabaseClient: SupabaseClient,
    private val tableNames: SupabaseTableNames
) {
    suspend fun findById(id: UUID): AccountEntity? {
        return try {
            supabaseClient.from(tableNames.accounts)
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
            supabaseClient.from(tableNames.accounts)
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
            supabaseClient.from(tableNames.accounts)
                .upsert(entity)
        } catch (e: Exception) {
            throw SupabaseDataException("Failed to save account: ${e.message}", e)
        }
    }

    suspend fun saveMany(entities: List<AccountEntity>) {
        try {
            supabaseClient.from(tableNames.accounts)
                .upsert(entities)
        } catch (e: Exception) {
            throw SupabaseDataException("Failed to save accounts: ${e.message}", e)
        }
    }

    suspend fun deleteById(id: UUID) {
        try {
            supabaseClient.from(tableNames.accounts)
                .delete {
                    filter {
                        eq("id", id.toString())
                    }
                }
        } catch (e: Exception) {
            throw SupabaseDataException("Failed to delete account: ${e.message}", e)
        }
    }

    suspend fun deleteAll() {
        try {
            supabaseClient.from(tableNames.accounts)
                .delete {
                    filter {
                        // Delete all records
                        neq("id", "00000000-0000-0000-0000-000000000000")
                    }
                }
        } catch (e: Exception) {
            throw SupabaseDataException("Failed to delete all accounts: ${e.message}", e)
        }
    }
}

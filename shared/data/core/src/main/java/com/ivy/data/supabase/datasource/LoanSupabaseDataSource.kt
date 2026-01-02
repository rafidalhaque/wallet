package com.ivy.data.supabase.datasource

import com.ivy.data.db.entity.LoanEntity
import com.ivy.data.supabase.SupabaseTableNames
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supabase data source for Loan operations
 * Replaces LoanDao and WriteLoanDao from Room
 */
@Singleton
class LoanSupabaseDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val tableNames: SupabaseTableNames
) {
    

    suspend fun findAll(): List<LoanEntity> {
        return try {
            supabaseClient.from(tableNames.loans)
                .select(columns = Columns.ALL) {
                    filter {
                        eq("isDeleted", false)
                    }
                }
                .decodeList<LoanEntity>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun findById(id: UUID): LoanEntity? {
        return try {
            supabaseClient.from(tableNames.loans)
                .select(columns = Columns.ALL) {
                    filter {
                        eq("id", id.toString())
                        eq("isDeleted", false)
                    }
                }
                .decodeSingleOrNull<LoanEntity>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun save(entity: LoanEntity) {
        try {
            supabaseClient.from(tableNames.loans)
                .upsert(entity)
        } catch (e: Exception) {
            throw Exception("Failed to save loan: ${e.message}", e)
        }
    }

    suspend fun saveMany(entities: List<LoanEntity>) {
        try {
            supabaseClient.from(tableNames.loans)
                .upsert(entities)
        } catch (e: Exception) {
            throw Exception("Failed to save loans: ${e.message}", e)
        }
    }

    suspend fun deleteById(id: UUID) {
        try {
            supabaseClient.from(tableNames.loans)
                .delete {
                    filter {
                        eq("id", id.toString())
                    }
                }
        } catch (e: Exception) {
            throw Exception("Failed to delete loan: ${e.message}", e)
        }
    }

    suspend fun deleteAll() {
        try {
            supabaseClient.from(tableNames.loans)
                .delete {
                    filter {
                        neq("id", "00000000-0000-0000-0000-000000000000")
                    }
                }
        } catch (e: Exception) {
            throw Exception("Failed to delete all loans: ${e.message}", e)
        }
    }
}

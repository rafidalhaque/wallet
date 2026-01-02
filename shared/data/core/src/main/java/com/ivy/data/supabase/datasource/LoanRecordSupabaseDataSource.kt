package com.ivy.data.supabase.datasource

import com.ivy.data.db.entity.LoanRecordEntity
import com.ivy.data.supabase.SupabaseTableNames
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supabase data source for Loan Record operations
 * Replaces LoanRecordDao and WriteLoanRecordDao from Room
 */
@Singleton
class LoanRecordSupabaseDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val tableNames: SupabaseTableNames
) {
    

    suspend fun findAll(): List<LoanRecordEntity> {
        return try {
            supabaseClient.from(tableNames.loanRecords)
                .select(columns = Columns.ALL) {
                    filter {
                        eq("isDeleted", false)
                    }
                }
                .decodeList<LoanRecordEntity>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun findAllByLoanId(loanId: UUID): List<LoanRecordEntity> {
        return try {
            supabaseClient.from(tableNames.loanRecords)
                .select(columns = Columns.ALL) {
                    filter {
                        eq("loanId", loanId.toString())
                        eq("isDeleted", false)
                    }
                    order("dateTime", ascending = false)
                }
                .decodeList<LoanRecordEntity>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun findById(id: UUID): LoanRecordEntity? {
        return try {
            supabaseClient.from(tableNames.loanRecords)
                .select(columns = Columns.ALL) {
                    filter {
                        eq("id", id.toString())
                        eq("isDeleted", false)
                    }
                }
                .decodeSingleOrNull<LoanRecordEntity>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun save(entity: LoanRecordEntity) {
        try {
            supabaseClient.from(tableNames.loanRecords)
                .upsert(entity)
        } catch (e: Exception) {
            throw Exception("Failed to save loan record: ${e.message}", e)
        }
    }

    suspend fun saveMany(entities: List<LoanRecordEntity>) {
        try {
            supabaseClient.from(tableNames.loanRecords)
                .upsert(entities)
        } catch (e: Exception) {
            throw Exception("Failed to save loan records: ${e.message}", e)
        }
    }

    suspend fun deleteById(id: UUID) {
        try {
            supabaseClient.from(tableNames.loanRecords)
                .delete {
                    filter {
                        eq("id", id.toString())
                    }
                }
        } catch (e: Exception) {
            throw Exception("Failed to delete loan record: ${e.message}", e)
        }
    }

    suspend fun deleteAllByLoanId(loanId: UUID) {
        try {
            supabaseClient.from(tableNames.loanRecords)
                .delete {
                    filter {
                        eq("loanId", loanId.toString())
                    }
                }
        } catch (e: Exception) {
            throw Exception("Failed to delete loan records: ${e.message}", e)
        }
    }

    suspend fun deleteAll() {
        try {
            supabaseClient.from(tableNames.loanRecords)
                .delete {
                    filter {
                        neq("id", "00000000-0000-0000-0000-000000000000")
                    }
                }
        } catch (e: Exception) {
            throw Exception("Failed to delete all loan records: ${e.message}", e)
        }
    }
}

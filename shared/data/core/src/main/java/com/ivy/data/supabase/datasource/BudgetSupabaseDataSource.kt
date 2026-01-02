package com.ivy.data.supabase.datasource

import com.ivy.data.db.entity.BudgetEntity
import com.ivy.data.supabase.SupabaseDataException
import com.ivy.data.supabase.SupabaseTableNames
import com.ivy.data.supabase.SupabaseDataException
import io.github.jan.supabase.SupabaseClient
import com.ivy.data.supabase.SupabaseDataException
import io.github.jan.supabase.postgrest.from
import com.ivy.data.supabase.SupabaseDataException
import io.github.jan.supabase.postgrest.query.Columns
import com.ivy.data.supabase.SupabaseDataException
import java.util.UUID
import com.ivy.data.supabase.SupabaseDataException
import javax.inject.Inject
import com.ivy.data.supabase.SupabaseDataException
import javax.inject.Singleton
import com.ivy.data.supabase.SupabaseDataException

/**
 * Supabase data source for Budget operations
 * Replaces BudgetDao and WriteBudgetDao from Room
 */
@Singleton
class BudgetSupabaseDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val tableNames: SupabaseTableNames
) {
    

    suspend fun findAll(): List<BudgetEntity> {
        return try {
            supabaseClient.from(tableNames.budgets)
                .select(columns = Columns.ALL) {
                    filter {
                        eq("isDeleted", false)
                    }
                }
                .decodeList<BudgetEntity>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun findById(id: UUID): BudgetEntity? {
        return try {
            supabaseClient.from(tableNames.budgets)
                .select(columns = Columns.ALL) {
                    filter {
                        eq("id", id.toString())
                        eq("isDeleted", false)
                    }
                }
                .decodeSingleOrNull<BudgetEntity>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun save(entity: BudgetEntity) {
        try {
            supabaseClient.from(tableNames.budgets)
                .upsert(entity)
        } catch (e: Exception) {
            throw SupabaseDataException("Failed to save budget: ${e.message}", e)
        }
    }

    suspend fun saveMany(entities: List<BudgetEntity>) {
        try {
            supabaseClient.from(tableNames.budgets)
                .upsert(entities)
        } catch (e: Exception) {
            throw SupabaseDataException("Failed to save budgets: ${e.message}", e)
        }
    }

    suspend fun deleteById(id: UUID) {
        try {
            supabaseClient.from(tableNames.budgets)
                .delete {
                    filter {
                        eq("id", id.toString())
                    }
                }
        } catch (e: Exception) {
            throw SupabaseDataException("Failed to delete budget: ${e.message}", e)
        }
    }

    suspend fun deleteAll() {
        try {
            supabaseClient.from(tableNames.budgets)
                .delete {
                    filter {
                        neq("id", "00000000-0000-0000-0000-000000000000")
                    }
                }
        } catch (e: Exception) {
            throw SupabaseDataException("Failed to delete all budgets: ${e.message}", e)
        }
    }
}

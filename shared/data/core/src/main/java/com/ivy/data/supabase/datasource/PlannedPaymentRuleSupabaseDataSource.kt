package com.ivy.data.supabase.datasource

import com.ivy.data.db.entity.PlannedPaymentRuleEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supabase data source for Planned Payment Rule operations
 * Replaces PlannedPaymentRuleDao and WritePlannedPaymentRuleDao from Room
 */
@Singleton
class PlannedPaymentRuleSupabaseDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    private val tableName = "planned_payment_rules"

    suspend fun findAll(): List<PlannedPaymentRuleEntity> {
        return try {
            supabaseClient.from(tableName)
                .select(columns = Columns.ALL) {
                    filter {
                        eq("isDeleted", false)
                    }
                }
                .decodeList<PlannedPaymentRuleEntity>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun findById(id: UUID): PlannedPaymentRuleEntity? {
        return try {
            supabaseClient.from(tableName)
                .select(columns = Columns.ALL) {
                    filter {
                        eq("id", id.toString())
                        eq("isDeleted", false)
                    }
                }
                .decodeSingleOrNull<PlannedPaymentRuleEntity>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun findByAccountId(accountId: UUID): List<PlannedPaymentRuleEntity> {
        return try {
            supabaseClient.from(tableName)
                .select(columns = Columns.ALL) {
                    filter {
                        eq("accountId", accountId.toString())
                        eq("isDeleted", false)
                    }
                }
                .decodeList<PlannedPaymentRuleEntity>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun save(entity: PlannedPaymentRuleEntity) {
        try {
            supabaseClient.from(tableName)
                .upsert(entity)
        } catch (e: Exception) {
            throw Exception("Failed to save planned payment rule: ${e.message}", e)
        }
    }

    suspend fun saveMany(entities: List<PlannedPaymentRuleEntity>) {
        try {
            supabaseClient.from(tableName)
                .upsert(entities)
        } catch (e: Exception) {
            throw Exception("Failed to save planned payment rules: ${e.message}", e)
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
            throw Exception("Failed to delete planned payment rule: ${e.message}", e)
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
            throw Exception("Failed to delete all planned payment rules: ${e.message}", e)
        }
    }
}

package com.ivy.data.supabase.datasource

import com.ivy.base.model.TransactionType
import com.ivy.data.db.entity.TransactionEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supabase data source for Transaction operations
 * Replaces TransactionDao and WriteTransactionDao from Room
 */
@Singleton
class TransactionSupabaseDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    private val tableName = "transactions"

    suspend fun findAll(): List<TransactionEntity> {
        return try {
            supabaseClient.from(tableName)
                .select(columns = Columns.ALL) {
                    filter {
                        eq("isDeleted", false)
                    }
                    order("dateTime", ascending = false)
                }
                .decodeList<TransactionEntity>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun findById(id: UUID): TransactionEntity? {
        return try {
            supabaseClient.from(tableName)
                .select(columns = Columns.ALL) {
                    filter {
                        eq("id", id.toString())
                        eq("isDeleted", false)
                    }
                }
                .decodeSingleOrNull<TransactionEntity>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun findByIds(ids: List<UUID>): List<TransactionEntity> {
        return try {
            supabaseClient.from(tableName)
                .select(columns = Columns.ALL) {
                    filter {
                        isIn("id", ids.map { it.toString() })
                        eq("isDeleted", false)
                    }
                }
                .decodeList<TransactionEntity>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun findAllByTypeAndAccount(
        type: TransactionType,
        accountId: UUID
    ): List<TransactionEntity> {
        return try {
            supabaseClient.from(tableName)
                .select(columns = Columns.ALL) {
                    filter {
                        eq("type", type.name)
                        eq("accountId", accountId.toString())
                        eq("isDeleted", false)
                    }
                    order("dateTime", ascending = false)
                }
                .decodeList<TransactionEntity>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun findAllTransfersToAccount(toAccountId: UUID): List<TransactionEntity> {
        return try {
            supabaseClient.from(tableName)
                .select(columns = Columns.ALL) {
                    filter {
                        eq("type", TransactionType.TRANSFER.name)
                        eq("toAccountId", toAccountId.toString())
                        eq("isDeleted", false)
                    }
                    order("dateTime", ascending = false)
                }
                .decodeList<TransactionEntity>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun findAllBetween(
        startDate: Instant,
        endDate: Instant
    ): List<TransactionEntity> {
        return try {
            supabaseClient.from(tableName)
                .select(columns = Columns.ALL) {
                    filter {
                        gte("dateTime", startDate.toString())
                        lte("dateTime", endDate.toString())
                        eq("isDeleted", false)
                    }
                    order("dateTime", ascending = false)
                }
                .decodeList<TransactionEntity>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun findAllByAccountAndBetween(
        accountId: UUID,
        startDate: Instant,
        endDate: Instant
    ): List<TransactionEntity> {
        return try {
            supabaseClient.from(tableName)
                .select(columns = Columns.ALL) {
                    filter {
                        eq("accountId", accountId.toString())
                        gte("dateTime", startDate.toString())
                        lte("dateTime", endDate.toString())
                        eq("isDeleted", false)
                    }
                    order("dateTime", ascending = false)
                }
                .decodeList<TransactionEntity>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun findAllByRecurringRuleId(recurringRuleId: UUID): List<TransactionEntity> {
        return try {
            supabaseClient.from(tableName)
                .select(columns = Columns.ALL) {
                    filter {
                        eq("recurringRuleId", recurringRuleId.toString())
                        eq("isDeleted", false)
                    }
                }
                .decodeList<TransactionEntity>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun countHappenedTransactions(): Long {
        return try {
            val transactions = supabaseClient.from(tableName)
                .select(columns = Columns.list("id")) {
                    filter {
                        eq("isDeleted", false)
                        // Filter transactions that have happened (have dateTime)
                        neq("dateTime", "null")
                    }
                }
                .decodeList<Map<String, Any>>()
            transactions.size.toLong()
        } catch (e: Exception) {
            0L
        }
    }

    suspend fun save(entity: TransactionEntity) {
        try {
            supabaseClient.from(tableName)
                .upsert(entity)
        } catch (e: Exception) {
            throw Exception("Failed to save transaction: ${e.message}", e)
        }
    }

    suspend fun saveMany(entities: List<TransactionEntity>) {
        try {
            supabaseClient.from(tableName)
                .upsert(entities)
        } catch (e: Exception) {
            throw Exception("Failed to save transactions: ${e.message}", e)
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
            throw Exception("Failed to delete transaction: ${e.message}", e)
        }
    }

    suspend fun deleteAllByAccountId(accountId: UUID) {
        try {
            supabaseClient.from(tableName)
                .delete {
                    filter {
                        eq("accountId", accountId.toString())
                    }
                }
        } catch (e: Exception) {
            throw Exception("Failed to delete transactions: ${e.message}", e)
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
            throw Exception("Failed to delete all transactions: ${e.message}", e)
        }
    }
}

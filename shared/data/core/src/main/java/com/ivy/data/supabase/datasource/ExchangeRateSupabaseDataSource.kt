package com.ivy.data.supabase.datasource

import com.ivy.data.db.entity.ExchangeRateEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supabase data source for Exchange Rate operations
 * Replaces ExchangeRatesDao and WriteExchangeRatesDao from Room
 * Note: ExchangeRateEntity uses composite primary key (baseCurrency, currency)
 */
@Singleton
class ExchangeRateSupabaseDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    private val tableName = "exchange_rates"

    suspend fun findAll(): List<ExchangeRateEntity> {
        return try {
            supabaseClient.from(tableName)
                .select(columns = Columns.ALL)
                .decodeList<ExchangeRateEntity>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun findByBaseCurrency(baseCurrency: String): List<ExchangeRateEntity> {
        return try {
            supabaseClient.from(tableName)
                .select(columns = Columns.ALL) {
                    filter {
                        eq("baseCurrency", baseCurrency)
                    }
                }
                .decodeList<ExchangeRateEntity>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun findByBaseCurrencyAndCurrency(
        baseCurrency: String,
        currency: String
    ): ExchangeRateEntity? {
        return try {
            supabaseClient.from(tableName)
                .select(columns = Columns.ALL) {
                    filter {
                        eq("baseCurrency", baseCurrency)
                        eq("currency", currency)
                    }
                }
                .decodeSingleOrNull<ExchangeRateEntity>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun save(entity: ExchangeRateEntity) {
        try {
            supabaseClient.from(tableName)
                .upsert(entity)
        } catch (e: Exception) {
            throw Exception("Failed to save exchange rate: ${e.message}", e)
        }
    }

    suspend fun saveMany(entities: List<ExchangeRateEntity>) {
        try {
            supabaseClient.from(tableName)
                .upsert(entities)
        } catch (e: Exception) {
            throw Exception("Failed to save exchange rates: ${e.message}", e)
        }
    }

    suspend fun deleteByBaseCurrencyAndCurrency(
        baseCurrency: String,
        currency: String
    ) {
        try {
            supabaseClient.from(tableName)
                .delete {
                    filter {
                        eq("baseCurrency", baseCurrency)
                        eq("currency", currency)
                    }
                }
        } catch (e: Exception) {
            throw Exception("Failed to delete exchange rate: ${e.message}", e)
        }
    }

    suspend fun deleteAll() {
        try {
            supabaseClient.from(tableName)
                .delete {
                    filter {
                        neq("baseCurrency", "")
                    }
                }
        } catch (e: Exception) {
            throw Exception("Failed to delete all exchange rates: ${e.message}", e)
        }
    }
}

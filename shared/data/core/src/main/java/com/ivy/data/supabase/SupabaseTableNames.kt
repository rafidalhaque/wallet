package com.ivy.data.supabase

import com.ivy.data.supabase.SupabaseConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class to manage Supabase table names with optional prefix
 * This allows multiple environments to use the same Supabase project
 * by prefixing table names (e.g., "dev_", "staging_", "prod_")
 */
@Singleton
class SupabaseTableNames @Inject constructor(
    private val config: SupabaseConfig
) {
    private val prefix = config.tablePrefix

    /**
     * Get the full table name with prefix
     */
    private fun getTableName(baseName: String): String {
        return if (prefix.isNotEmpty()) {
            "${prefix}${baseName}"
        } else {
            baseName
        }
    }

    val accounts: String get() = getTableName("accounts")
    val transactions: String get() = getTableName("transactions")
    val categories: String get() = getTableName("categories")
    val settings: String get() = getTableName("settings")
    val budgets: String get() = getTableName("budgets")
    val plannedPaymentRules: String get() = getTableName("planned_payment_rules")
    val tags: String get() = getTableName("tags")
    val tagsAssociation: String get() = getTableName("tags_association")
    val exchangeRates: String get() = getTableName("exchange_rates")
    val users: String get() = getTableName("users")
    val loans: String get() = getTableName("loans")
    val loanRecords: String get() = getTableName("loan_records")
}

@file:Suppress("Deprecation")

package com.ivy.data.repository

import android.icu.util.Currency
import com.ivy.base.legacy.Theme
import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.entity.SettingsEntity
import com.ivy.data.model.primitive.AssetCode
import com.ivy.data.supabase.datasource.ISettingsDataSource
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyRepository @Inject constructor(
    private val settingsDataSource: ISettingsDataSource,
    private val dispatchersProvider: DispatchersProvider,
) {
    companion object {
        const val FALLBACK_DEFAULT_CURRENCY = "USD"
    }

    private var baseCurrencyMemo: AssetCode? = null

    suspend fun getBaseCurrency(): AssetCode = withContext(dispatchersProvider.io) {
        val baseCurrency = baseCurrencyMemo
        if (baseCurrency != null) return@withContext baseCurrency

        val currencyCode = settingsDataSource.findFirst()?.currency
            ?: getDefaultFIATCurrency()?.currencyCode
        currencyCode?.let(AssetCode::from)?.getOrNull()
            ?: AssetCode.unsafe(FALLBACK_DEFAULT_CURRENCY)
    }

    private fun getDefaultFIATCurrency(): Currency? {
        return Currency.getInstance(Locale.getDefault())
    }

    suspend fun setBaseBaseCurrency(newCurrency: AssetCode) {
        withContext<Unit>(dispatchersProvider.io) {
            val currentEntity = settingsDataSource.findFirst()
                ?: SettingsEntity(
                    theme = Theme.AUTO,
                    currency = FALLBACK_DEFAULT_CURRENCY,
                    bufferAmount = 0.0,
                    name = "",
                    isSynced = true,
                    isDeleted = false,
                    id = UUID.randomUUID()
                )
            baseCurrencyMemo = newCurrency
            settingsDataSource.save(
                currentEntity.copy(
                    currency = newCurrency.code
                )
            )
        }
    }
}
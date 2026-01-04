package com.ivy.data.supabase

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ivy.data.datastore.IvyDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages Supabase configuration stored securely in DataStore
 * This allows users to configure Supabase from the app's settings UI
 * instead of using environment variables
 */
@Singleton
class SupabaseConfigDataStore @Inject constructor(
    private val dataStore: IvyDataStore
) {
    private val supabaseUrlKey = stringPreferencesKey("supabase_url")
    private val supabaseAnonKeyKey = stringPreferencesKey("supabase_anon_key")
    private val supabaseTablePrefixKey = stringPreferencesKey("supabase_table_prefix")

    /**
     * Get the Supabase URL from DataStore
     */
    suspend fun getSupabaseUrl(): String? {
        return dataStore.data.map { preferences ->
            preferences[supabaseUrlKey]
        }.first()
    }

    /**
     * Get the Supabase anonymous key from DataStore
     */
    suspend fun getSupabaseAnonKey(): String? {
        return dataStore.data.map { preferences ->
            preferences[supabaseAnonKeyKey]
        }.first()
    }

    /**
     * Get the Supabase table prefix from DataStore
     */
    suspend fun getSupabaseTablePrefix(): String {
        return dataStore.data.map { preferences ->
            preferences[supabaseTablePrefixKey] ?: ""
        }.first()
    }

    /**
     * Save Supabase configuration to DataStore
     */
    suspend fun saveSupabaseConfig(
        url: String,
        anonKey: String,
        tablePrefix: String = ""
    ) {
        dataStore.edit { preferences ->
            preferences[supabaseUrlKey] = url
            preferences[supabaseAnonKeyKey] = anonKey
            preferences[supabaseTablePrefixKey] = tablePrefix
        }
    }

    /**
     * Check if Supabase is configured
     */
    suspend fun isConfigured(): Boolean {
        return dataStore.data.map { preferences ->
            preferences[supabaseUrlKey] != null && preferences[supabaseAnonKeyKey] != null
        }.first()
    }

    /**
     * Clear Supabase configuration from DataStore
     */
    suspend fun clearSupabaseConfig() {
        dataStore.edit { preferences ->
            preferences.remove(supabaseUrlKey)
            preferences.remove(supabaseAnonKeyKey)
            preferences.remove(supabaseTablePrefixKey)
        }
    }

    /**
     * Flow to observe Supabase configuration status
     */
    fun isConfiguredFlow(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[supabaseUrlKey] != null && preferences[supabaseAnonKeyKey] != null
        }
    }
}

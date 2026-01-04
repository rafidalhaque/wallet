package com.ivy.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

/**
 * Supabase client wrapper for Ivy Wallet
 * Provides access to Supabase services: Database (Postgrest), Realtime, Auth, and Storage
 */
object IvySupabaseClient {
    private var _client: SupabaseClient? = null

    /**
     * Initialize the Supabase client with the given configuration
     * This should be called once during app initialization
     */
    fun initialize(config: SupabaseConfig) {
        _client = createSupabaseClient(
            supabaseUrl = config.url,
            supabaseKey = config.anonKey
        ) {
            install(Postgrest)
            install(Realtime)
            install(Auth)
            install(Storage)
        }
    }

    /**
     * Get the initialized Supabase client
     * Throws an exception if the client has not been initialized
     */
    val client: SupabaseClient
        get() = _client ?: error("SupabaseClient not initialized. Call initialize() first.")

    /**
     * Check if the client has been initialized
     */
    val isInitialized: Boolean
        get() = _client != null
}

/**
 * Configuration for Supabase client
 */
data class SupabaseConfig(
    val url: String,
    val anonKey: String,
    val tablePrefix: String
)

package com.ivy.data.di

import android.content.Context
import com.ivy.data.supabase.IvySupabaseClient
import com.ivy.data.supabase.SupabaseConfig
import com.ivy.data.supabase.SupabaseConfigDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseConfig(
        configDataStore: SupabaseConfigDataStore
    ): SupabaseConfig {
        // Load configuration from DataStore (user-configurable via Settings UI)
        // Falls back to environment variables for backwards compatibility
        return runBlocking {
            val urlFromDataStore = configDataStore.getSupabaseUrl()
            val keyFromDataStore = configDataStore.getSupabaseAnonKey()
            val prefixFromDataStore = configDataStore.getSupabaseTablePrefix()

            SupabaseConfig(
                url = urlFromDataStore 
                    ?: System.getenv("SUPABASE_URL") 
                    ?: "https://your-project.supabase.co",
                anonKey = keyFromDataStore 
                    ?: System.getenv("SUPABASE_ANON_KEY") 
                    ?: "your-anon-key",
                tablePrefix = prefixFromDataStore.takeIf { it.isNotEmpty() }
                    ?: System.getenv("SUPABASE_TABLE_PREFIX")
                    ?: ""
            )
        }
    }

    @Provides
    @Singleton
    fun provideSupabaseClient(
        config: SupabaseConfig
    ): SupabaseClient {
        if (!IvySupabaseClient.isInitialized) {
            IvySupabaseClient.initialize(config)
        }
        return IvySupabaseClient.client
    }
}

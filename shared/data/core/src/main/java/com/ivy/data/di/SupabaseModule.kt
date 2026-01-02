package com.ivy.data.di

import android.content.Context
import com.ivy.data.supabase.IvySupabaseClient
import com.ivy.data.supabase.SupabaseConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseConfig(): SupabaseConfig {
        // TODO: Load from BuildConfig or secure storage
        // For now, using placeholder values
        return SupabaseConfig(
            url = System.getenv("SUPABASE_URL") ?: "https://your-project.supabase.co",
            anonKey = System.getenv("SUPABASE_ANON_KEY") ?: "your-anon-key"
        )
    }

    @Provides
    @Singleton
    fun provideSupabaseClient(
        @ApplicationContext context: Context,
        config: SupabaseConfig
    ): SupabaseClient {
        if (!IvySupabaseClient.isInitialized) {
            IvySupabaseClient.initialize(context, config)
        }
        return IvySupabaseClient.client
    }
}

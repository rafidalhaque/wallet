package com.ivy.data.supabase.datasource

import com.ivy.data.db.entity.SettingsEntity

interface ISettingsDataSource {
    suspend fun findFirst(): SettingsEntity?
    suspend fun findAll(): List<SettingsEntity>
    suspend fun save(entity: SettingsEntity)
    suspend fun deleteAll()
}


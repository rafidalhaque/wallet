package com.ivy.data.supabase.datasource

import com.ivy.data.db.entity.AccountEntity
import java.util.UUID

interface IAccountDataSource {
    suspend fun findAll(): List<AccountEntity>
    suspend fun findById(id: UUID): AccountEntity?
    suspend fun findMaxOrderNum(): Double?
    suspend fun save(entity: AccountEntity)
    suspend fun saveMany(entities: List<AccountEntity>)
    suspend fun deleteById(id: UUID)
    suspend fun deleteAll()
}


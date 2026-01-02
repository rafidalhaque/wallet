package com.ivy.data.repository

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.DataWriteEvent
import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId
import com.ivy.data.repository.mapper.CategoryMapper
import com.ivy.data.supabase.datasource.CategorySupabaseDataSource
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val mapper: CategoryMapper,
    private val categoryDataSource: CategorySupabaseDataSource,
    private val dispatchersProvider: DispatchersProvider,
    memoFactory: RepositoryMemoFactory,
) {
    private val memo = memoFactory.createMemo(
        getDataWriteSaveEvent = DataWriteEvent::SaveCategories,
        getDateWriteDeleteEvent = DataWriteEvent::DeleteCategories,
    )

    suspend fun findAll(): List<Category> = memo.findAll(
        findAllOperation = {
            categoryDataSource.findAll().mapNotNull {
                with(mapper) { it.toDomain() }.getOrNull()
            }
        },
        sortMemo = { sortedBy(Category::orderNum) }
    )

    suspend fun findById(id: CategoryId): Category? = memo.findById(
        id = id,
        findByIdOperation = {
            categoryDataSource.findById(id.value)?.let {
                with(mapper) { it.toDomain() }.getOrNull()
            }
        }
    )

    suspend fun findMaxOrderNum(): Double = if (memo.findAllMemoized) {
        memo.items.maxOfOrNull { (_, acc) -> acc.orderNum } ?: 0.0
    } else {
        withContext(dispatchersProvider.io) {
            categoryDataSource.findMaxOrderNum() ?: 0.0
        }
    }

    suspend fun save(value: Category): Unit = memo.save(
        value = value,
    ) {
        categoryDataSource.save(
            with(mapper) { it.toEntity() }
        )
    }

    suspend fun saveMany(values: List<Category>): Unit = memo.saveMany(
        values = values,
    ) {
        categoryDataSource.saveMany(
            values.map { with(mapper) { it.toEntity() } }
        )
    }

    suspend fun deleteById(id: CategoryId): Unit = memo.deleteById(id = id) {
        categoryDataSource.deleteById(id.value)
    }

    suspend fun deleteAll(): Unit = memo.deleteAll(categoryDataSource::deleteAll)
}

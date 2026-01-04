package com.ivy.data.repository

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.DataWriteEvent
import com.ivy.data.model.Tag
import com.ivy.data.model.TagAssociation
import com.ivy.data.model.TagId
import com.ivy.data.model.primitive.AssociationId
import com.ivy.data.repository.mapper.TagMapper
import com.ivy.data.supabase.datasource.TagAssociationSupabaseDataSource
import com.ivy.data.supabase.datasource.TagSupabaseDataSource
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepository @Inject constructor(
    private val mapper: TagMapper,
    private val tagDataSource: TagSupabaseDataSource,
    private val tagAssociationDataSource: TagAssociationSupabaseDataSource,
    private val dispatchersProvider: DispatchersProvider,
    memoFactory: RepositoryMemoFactory,
) {
    private val memo = memoFactory.createMemo(
        getDataWriteSaveEvent = DataWriteEvent::SaveTags,
        getDateWriteDeleteEvent = DataWriteEvent::DeleteTags,
    )

    suspend fun findById(id: TagId): Tag? = memo.findById(
        id = id,
        findByIdOperation = ::findByIdOperation
    )

    suspend fun findByIds(ids: List<TagId>): List<Tag> = memo.findByIds(
        ids = ids,
        findByIdOperation = ::findByIdOperation,
    )

    private suspend fun findByIdOperation(id: TagId): Tag? = tagDataSource.findById(id.value)
        ?.let {
            with(mapper) { it.toDomain().getOrNull() }
        }

    suspend fun findByAssociatedId(id: AssociationId): List<Tag> {
        return withContext(dispatchersProvider.io) {
            val associations = tagAssociationDataSource.findByAssociatedId(id.value)
            val tagIds = associations.map { it.tagId }
            tagDataSource.findByIds(tagIds).let { entities ->
                entities.mapNotNull {
                    with(mapper) {
                        it.toDomain().getOrNull()
                    }
                }
            }
        }
    }

    suspend fun findByAssociatedId(
        ids: List<AssociationId>
    ): Map<AssociationId, List<Tag>> {
        return ids.chunked(MAX_SQL_LITE_QUERY_SIZE).map {
            withContext(dispatchersProvider.io) {
                async {
                    val associations = tagAssociationDataSource.findByAssociatedIds(it.map { it.value })
                    val groupedAssociations = associations.groupBy { AssociationId(it.associatedId) }

                    groupedAssociations.mapValues { (_, assocs) ->
                        val tagIds = assocs.map { it.tagId }
                        tagDataSource.findByIds(tagIds).mapNotNull {
                            with(mapper) {
                                it.toDomain().getOrNull()
                            }
                        }
                    }
                }
            }
        }.awaitAll().asSequence()
            .flatMap { it.asSequence() }
            .associate { it.key to it.value }
    }

    suspend fun findAll(): List<Tag> = memo.findAll(
        findAllOperation = {
            tagDataSource.findAll().let { entities ->
                entities.mapNotNull {
                    with(mapper) { it.toDomain().getOrNull() }
                }
            }
        },
        sortMemo = {
            sortedByDescending { it.creationTimestamp.epochSecond }
        }
    )

    suspend fun findByText(text: String): List<Tag> {
        return withContext(dispatchersProvider.io) {
            // For Supabase, we'll filter in memory for now
            // Could be optimized with a full-text search query later
            tagDataSource.findAll().filter {
                it.name.contains(text, ignoreCase = true)
            }.let { entities ->
                entities.mapNotNull {
                    with(mapper) { it.toDomain().getOrNull() }
                }
            }
        }
    }

    suspend fun findByAllAssociatedIdForTagId(
        tagIds: List<TagId>
    ): Map<TagId, List<TagAssociation>> {
        return withContext(dispatchersProvider.io) {
            val allAssociations = tagAssociationDataSource.findAll()
            allAssociations.filter { it.tagId in tagIds.map { it.value } }
                .groupBy { TagId(it.tagId) }
                .mapValues { (_, assocs) ->
                    with(mapper) {
                        assocs.map { it.toDomain() }
                    }
                }
        }
    }

    suspend fun findByAllTagsForAssociations(): Map<AssociationId, List<TagAssociation>> {
        return withContext(dispatchersProvider.io) {
            tagAssociationDataSource.findAll().groupBy {
                AssociationId(it.associatedId)
            }.mapValues {
                with(mapper) {
                    it.value.map { it.toDomain() }
                }
            }
        }
    }

    suspend fun associateTagToEntity(associationId: AssociationId, tagId: TagId) {
        withContext(dispatchersProvider.io) {
            tagAssociationDataSource.save(
                with(mapper) {
                    createNewTagAssociation(tagId, associationId).toEntity()
                }
            )
        }
    }

    suspend fun removeTagAssociation(associationId: AssociationId, tagId: TagId) {
        withContext(dispatchersProvider.io) {
            tagAssociationDataSource.deleteByTagIdAndAssociatedId(
                tagId = tagId.value,
                associatedId = associationId.value
            )
        }
    }

    suspend fun save(value: Tag): Unit = memo.save(value) {
        tagDataSource.save(with(mapper) { it.toEntity() })
    }

    suspend fun deleteById(id: TagId): Unit = memo.deleteById(
        id = id,
        deleteByIdOperation = {
            tagAssociationDataSource.deleteByAssociatedId(it.value)
            tagDataSource.deleteById(it.value)
        }
    )

    suspend fun deleteAll(): Unit = memo.deleteAll {
        tagAssociationDataSource.deleteAll()
        tagDataSource.deleteAll()
    }

    companion object {
        private const val MAX_SQL_LITE_QUERY_SIZE = 999
    }
}

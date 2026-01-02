package com.ivy.data.repository

import com.ivy.base.model.TransactionType
import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.entity.TransactionEntity
import com.ivy.data.model.AccountId
import com.ivy.data.model.CategoryId
import com.ivy.data.model.Expense
import com.ivy.data.model.Income
import com.ivy.data.model.TagId
import com.ivy.data.model.Transaction
import com.ivy.data.model.TransactionId
import com.ivy.data.model.Transfer
import com.ivy.data.model.primitive.AssociationId
import com.ivy.data.model.primitive.NonNegativeLong
import com.ivy.data.model.primitive.toNonNegative
import com.ivy.data.repository.mapper.TransactionMapper
import com.ivy.data.supabase.datasource.TransactionSupabaseDataSource
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class TransactionRepository @Inject constructor(
    private val mapper: TransactionMapper,
    private val transactionDataSource: TransactionSupabaseDataSource,
    private val dispatchersProvider: DispatchersProvider,
    private val tagRepository: TagRepository
) {
    suspend fun findAll(): List<Transaction> = withContext(dispatchersProvider.io) {
        val tagMap = async { findAllTagAssociations() }
        retrieveTrns(
            dbCall = transactionDataSource::findAll,
            retrieveTags = {
                tagMap.await()[it.id] ?: emptyList()
            }
        )
    }

    suspend fun findAllIncomeByAccount(
        accountId: AccountId
    ): List<Income> = retrieveTrns(
        dbCall = {
            transactionDataSource.findAllByTypeAndAccount(
                type = TransactionType.INCOME,
                accountId = accountId.value
            )
        }
    ).filterIsInstance<Income>()

    suspend fun findAllExpenseByAccount(
        accountId: AccountId
    ): List<Expense> = retrieveTrns(
        dbCall = {
            transactionDataSource.findAllByTypeAndAccount(
                type = TransactionType.EXPENSE,
                accountId = accountId.value
            )
        }
    ).filterIsInstance<Expense>()

    suspend fun findAllTransferByAccount(
        accountId: AccountId
    ): List<Transfer> = retrieveTrns(
        dbCall = {
            transactionDataSource.findAllByTypeAndAccount(
                type = TransactionType.TRANSFER,
                accountId = accountId.value
            )
        }
    ).filterIsInstance<Transfer>()

    suspend fun findAllTransfersToAccount(
        toAccountId: AccountId
    ): List<Transfer> = retrieveTrns(
        dbCall = {
            transactionDataSource.findAllTransfersToAccount(toAccountId = toAccountId.value)
        }
    ).filterIsInstance<Transfer>()

    suspend fun findAllBetween(
        startDate: Instant,
        endDate: Instant
    ): List<Transaction> = withContext(dispatchersProvider.io) {
        val transactions = transactionDataSource.findAllBetween(startDate, endDate)
        val tagAssociationMap = getTagsForTransactionIds(transactions)
        transactions.mapNotNull {
            val tags = tagAssociationMap[it.id] ?: emptyList()
            with(mapper) { it.toDomain(tags = tags) }.getOrNull()
        }
    }

    suspend fun findAllByAccountAndBetween(
        accountId: AccountId,
        startDate: Instant,
        endDate: Instant
    ): List<Transaction> = retrieveTrns(
        dbCall = {
            transactionDataSource.findAllByAccountAndBetween(
                accountId = accountId.value,
                startDate = startDate,
                endDate = endDate
            )
        }
    )

    suspend fun findAllToAccountAndBetween(
        toAccountId: AccountId,
        startDate: Instant,
        endDate: Instant
    ): List<Transaction> = retrieveTrns(
        dbCall = {
            // This would need to be added to TransactionSupabaseDataSource
            // For now, filter from findAllTransfersToAccount
            transactionDataSource.findAllTransfersToAccount(toAccountId.value)
                .filter { it.dateTime != null && it.dateTime >= startDate && it.dateTime <= endDate }
        }
    )

    suspend fun findAllDueToBetween(
        startDate: Instant,
        endDate: Instant
    ): List<Transaction> = retrieveTrns(
        dbCall = {
            // This would need to be added to TransactionSupabaseDataSource
            // For now, filter from findAllBetween with dueDate
            transactionDataSource.findAll()
                .filter { it.dueDate != null && it.dueDate >= startDate && it.dueDate <= endDate }
        }
    )

    suspend fun findAllDueToBetweenByCategory(
        startDate: Instant,
        endDate: Instant,
        categoryId: CategoryId
    ): List<Transaction> = retrieveTrns(
        dbCall = {
            // This would need to be added to TransactionSupabaseDataSource
            transactionDataSource.findAll()
                .filter {
                    it.categoryId == categoryId.value &&
                        it.dueDate != null && it.dueDate >= startDate && it.dueDate <= endDate
                }
        }
    )

    suspend fun findAllDueToBetweenByCategoryUnspecified(
        startDate: Instant,
        endDate: Instant
    ): List<Transaction> = retrieveTrns(
        dbCall = {
            // This would need to be added to TransactionSupabaseDataSource
            transactionDataSource.findAll()
                .filter {
                    it.categoryId == null &&
                        it.dueDate != null && it.dueDate >= startDate && it.dueDate <= endDate
                }
        }
    )

    suspend fun findAllDueToBetweenByAccount(
        startDate: Instant,
        endDate: Instant,
        accountId: AccountId
    ): List<Transaction> = retrieveTrns(
        dbCall = {
            // This would need to be added to TransactionSupabaseDataSource
            transactionDataSource.findAll()
                .filter {
                    it.accountId == accountId.value &&
                        it.dueDate != null && it.dueDate >= startDate && it.dueDate <= endDate
                }
        }
    )

    suspend fun findAllByCategoryAndTypeAndBetween(
        categoryId: UUID,
        type: TransactionType,
        startDate: Instant,
        endDate: Instant
    ): List<Transaction> = retrieveTrns(
        dbCall = {
            // This would need to be added to TransactionSupabaseDataSource
            transactionDataSource.findAllBetween(startDate, endDate)
                .filter { it.categoryId == categoryId && it.type == type }
        }
    )

    suspend fun findAllUnspecifiedAndTypeAndBetween(
        type: TransactionType,
        startDate: Instant,
        endDate: Instant
    ): List<Transaction> = retrieveTrns(
        dbCall = {
            // This would need to be added to TransactionSupabaseDataSource
            transactionDataSource.findAllBetween(startDate, endDate)
                .filter { it.categoryId == null && it.type == type }
        }
    )

    suspend fun findAllUnspecifiedAndBetween(
        startDate: Instant,
        endDate: Instant
    ): List<Transaction> = retrieveTrns(
        dbCall = {
            // This would need to be added to TransactionSupabaseDataSource
            transactionDataSource.findAllBetween(startDate, endDate)
                .filter { it.categoryId == null }
        }
    )

    suspend fun findAllByCategoryAndBetween(
        categoryId: UUID,
        startDate: Instant,
        endDate: Instant
    ): List<Transaction> = retrieveTrns(
        dbCall = {
            // This would need to be added to TransactionSupabaseDataSource
            transactionDataSource.findAllBetween(startDate, endDate)
                .filter { it.categoryId == categoryId }
        }
    )

    suspend fun findAllByRecurringRuleId(recurringRuleId: UUID): List<Transaction> = retrieveTrns(
        dbCall = {
            transactionDataSource.findAllByRecurringRuleId(recurringRuleId)
        }
    )

    suspend fun findById(
        id: TransactionId
    ): Transaction? = withContext(dispatchersProvider.io) {
        transactionDataSource.findById(id.value)?.let {
            with(mapper) { it.toDomain() }.getOrNull()
        }
    }

    suspend fun findByIds(ids: List<TransactionId>): List<Transaction> {
        return withContext(dispatchersProvider.io) {
            val tagMap = async { findTagsForTransactionIds(ids) }
            retrieveTrns(
                dbCall = {
                    transactionDataSource.findByIds(ids.map { it.value })
                },
                retrieveTags = {
                    tagMap.await()[it.id] ?: emptyList()
                }
            )
        }
    }

    suspend fun save(value: Transaction) {
        withContext(dispatchersProvider.io) {
            transactionDataSource.save(
                with(mapper) { value.toEntity() }
            )
        }
    }

    suspend fun saveMany(value: List<Transaction>) {
        withContext(dispatchersProvider.io) {
            transactionDataSource.saveMany(
                value.map { with(mapper) { it.toEntity() } }
            )
        }
    }

    suspend fun deleteById(id: TransactionId) {
        withContext(dispatchersProvider.io) {
            transactionDataSource.deleteById(id.value)
        }
    }

    suspend fun deleteAllByAccountId(accountId: AccountId) {
        withContext(dispatchersProvider.io) {
            transactionDataSource.deleteAllByAccountId(accountId.value)
        }
    }

    suspend fun deletedByRecurringRuleIdAndNoDateTime(recurringRuleId: UUID) {
        withContext(dispatchersProvider.io) {
            // This would need to be added to TransactionSupabaseDataSource
            val transactions = transactionDataSource.findAllByRecurringRuleId(recurringRuleId)
                .filter { it.dateTime == null }
            transactions.forEach { transactionDataSource.deleteById(it.id) }
        }
    }

    suspend fun deleteAll() {
        withContext(dispatchersProvider.io) {
            transactionDataSource.deleteAll()
        }
    }

    suspend fun countHappenedTransactions(): NonNegativeLong = withContext(dispatchersProvider.io) {
        transactionDataSource.countHappenedTransactions().toNonNegative()
    }

    suspend fun findLoanTransaction(loanId: UUID): Transaction? =
        withContext(dispatchersProvider.io) {
            // This would need to be added to TransactionSupabaseDataSource
            transactionDataSource.findAll()
                .firstOrNull { it.loanId == loanId }
                ?.let { with(mapper) { it.toDomain() }.getOrNull() }
        }

    suspend fun findLoanRecordTransaction(loanRecordId: UUID): Transaction? =
        withContext(dispatchersProvider.io) {
            // This would need to be added to TransactionSupabaseDataSource
            transactionDataSource.findAll()
                .firstOrNull { it.loanRecordId == loanRecordId }
                ?.let { with(mapper) { it.toDomain() }.getOrNull() }
        }

    suspend fun findAllByLoanId(loanId: UUID): List<Transaction> = retrieveTrns(
        dbCall = {
            // This would need to be added to TransactionSupabaseDataSource
            transactionDataSource.findAll()
                .filter { it.loanId == loanId }
        }
    )

    private suspend fun retrieveTrns(
        dbCall: suspend () -> List<TransactionEntity>,
        retrieveTags: suspend (TransactionEntity) -> List<TagId> = { emptyList() },
    ): List<Transaction> = withContext(dispatchersProvider.io) {
        dbCall().mapNotNull {
            with(mapper) { it.toDomain(tags = retrieveTags(it)) }.getOrNull()
        }
    }

    private suspend fun getTagsForTransactionIds(
        transactions: List<TransactionEntity>
    ): Map<UUID, List<TagId>> {
        return findTagsForTransactionIds(transactions.map { TransactionId(it.id) })
    }

    private suspend fun findTagsForTransactionIds(
        transactionIds: List<TransactionId>
    ): Map<UUID, List<TagId>> {
        return tagRepository.findByAssociatedId(transactionIds.map { AssociationId(it.value) })
            .entries.associate {
                it.key.value to it.value.map { ta -> ta.id }
            }
    }

    private suspend fun findAllTagAssociations(): Map<UUID, List<TagId>> {
        return tagRepository.findByAllTagsForAssociations().entries.associate {
            it.key.value to it.value.map { ta -> ta.id }
        }
    }
}
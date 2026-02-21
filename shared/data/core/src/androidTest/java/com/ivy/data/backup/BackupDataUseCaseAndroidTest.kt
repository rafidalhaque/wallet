@file:Suppress("Deprecation")

package com.ivy.data.backup

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ivy.base.TestDispatchersProvider
import com.ivy.base.di.KotlinxSerializationModule
import com.ivy.base.legacy.SharedPrefs
import com.ivy.data.DataObserver
import com.ivy.data.db.IvyRoomDatabase
import com.ivy.data.db.entity.AccountEntity
import com.ivy.data.db.entity.SettingsEntity
import com.ivy.data.file.FileSystem
import com.ivy.data.repository.AccountRepository
import com.ivy.data.repository.CurrencyRepository
import com.ivy.data.repository.fake.fakeRepositoryMemoFactory
import com.ivy.data.repository.mapper.AccountMapper
import com.ivy.data.supabase.datasource.IAccountDataSource
import com.ivy.data.supabase.datasource.ISettingsDataSource
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.ints.shouldBeGreaterThan
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class BackupDataUseCaseAndroidTest {

    private lateinit var db: IvyRoomDatabase
    private lateinit var useCase: BackupDataUseCase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, IvyRoomDatabase::class.java).build()
        val appContext = InstrumentationRegistry.getInstrumentation().context

        val settingsDataSource = object : ISettingsDataSource {
            override suspend fun findFirst(): SettingsEntity? = db.settingsDao.findFirstOrNull()
            override suspend fun findAll(): List<SettingsEntity> = db.settingsDao.findAll()
            override suspend fun save(entity: SettingsEntity) = db.writeSettingsDao.save(entity)
            override suspend fun deleteAll() { /* no-op for tests */ }
        }
        val accountDataSource = object : IAccountDataSource {
            override suspend fun findAll(): List<AccountEntity> = db.accountDao.findAll(deleted = false)
            override suspend fun findById(id: UUID): AccountEntity? = db.accountDao.findById(id)
            override suspend fun findMaxOrderNum(): Double? = db.accountDao.findMaxOrderNum()
            override suspend fun save(entity: AccountEntity) = db.writeAccountDao.save(entity)
            override suspend fun saveMany(entities: List<AccountEntity>) = db.writeAccountDao.saveMany(entities)
            override suspend fun deleteById(id: UUID) = db.writeAccountDao.deleteById(id)
            override suspend fun deleteAll() { /* no-op for tests */ }
        }

        val accountMapper = AccountMapper(
            currencyRepository = CurrencyRepository(
                settingsDataSource = settingsDataSource,
                dispatchersProvider = TestDispatchersProvider,
            )
        )
        useCase = BackupDataUseCase(
            accountDao = db.accountDao,
            budgetDao = db.budgetDao,
            categoryDao = db.categoryDao,
            loanRecordDao = db.loanRecordDao,
            loanDao = db.loanDao,
            plannedPaymentRuleDao = db.plannedPaymentRuleDao,
            settingsDao = db.settingsDao,
            transactionDao = db.transactionDao,
            transactionWriter = db.writeTransactionDao,
            sharedPrefs = SharedPrefs(appContext),
            accountRepository = AccountRepository(
                mapper = accountMapper,
                accountDataSource = accountDataSource,
                dispatchersProvider = TestDispatchersProvider,
                memoFactory = fakeRepositoryMemoFactory(),
            ),
            accountMapper = accountMapper,
            categoryWriter = db.writeCategoryDao,
            settingsWriter = db.writeSettingsDao,
            budgetWriter = db.writeBudgetDao,
            loanWriter = db.writeLoanDao,
            loanRecordWriter = db.writeLoanRecordDao,
            plannedPaymentRuleWriter = db.writePlannedPaymentRuleDao,
            context = appContext,
            json = KotlinxSerializationModule.provideJson(),
            dispatchersProvider = TestDispatchersProvider,
            fileSystem = FileSystem(appContext),
            dataObserver = DataObserver(),
            tagsReader = db.tagDao,
            tagAssociationReader = db.tagAssociationDao,
            tagsWriter = db.writeTagDao,
            tagAssociationWriter = db.writeTagAssociationDao
        )
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun backup450_150() = runBlocking {
        backupTestCase("450-150")
    }

    private suspend fun backupTestCase(version: String) {
        importBackupZipTestCase(version)
        importBackupJsonTestCase(version)
        closeDb()
        createDb()
        exportsAndImportsTestCase(version)
    }

    private suspend fun importBackupZipTestCase(version: String) {
        val backupUri = copyTestResourceToInternalStorage("backups/$version.zip")
        useCase.importBackupFile(backupUri, onProgress = {}).shouldBeSuccessful()
    }

    private suspend fun importBackupJsonTestCase(version: String) {
        val backupUri = copyTestResourceToInternalStorage("backups/$version.json")
        useCase.importBackupFile(backupUri, onProgress = {}).shouldBeSuccessful()
    }

    private suspend fun exportsAndImportsTestCase(version: String) {
        val backupUri = copyTestResourceToInternalStorage("backups/$version.zip")
        useCase.importBackupFile(backupUri, onProgress = {}).shouldBeSuccessful()
        val exportedFileUri = tempAndroidFile("exported", ".zip").toUri()
        useCase.exportToFile(exportedFileUri)
        useCase.importBackupFile(backupUri, onProgress = {}).shouldBeSuccessful()
    }

    private fun copyTestResourceToInternalStorage(resPath: String): Uri {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val inputStream = context.assets.open(resPath)
        val outputFile = tempAndroidFile("temp-backup", resPath.split(".").last())
        outputFile.outputStream().use { it.write(inputStream.readBytes()) }
        return Uri.fromFile(outputFile)
    }

    private fun tempAndroidFile(prefix: String, suffix: String): File {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return File.createTempFile(prefix, suffix, context.filesDir)
    }

    private fun ImportResult.shouldBeSuccessful() {
        failedRows.shouldBeEmpty()
        categoriesImported shouldBeGreaterThan 0
        accountsImported shouldBeGreaterThan 0
        transactionsImported shouldBeGreaterThan 0
    }
}

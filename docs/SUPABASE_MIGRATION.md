# Supabase Migration Guide for Ivy Wallet

This document outlines the migration from Room Database and DataStore to Supabase.

## Overview

Ivy Wallet is being migrated from local-first storage (Room Database + DataStore) to cloud-first storage (Supabase PostgreSQL).

### What's Changing

**Before:**
- Room Database (SQLite) for local data persistence
- DataStore for key-value preferences
- Firebase Firestore for poll voting

**After:**
- Supabase PostgreSQL for all data storage
- Supabase real-time subscriptions for live updates
- Supabase authentication for user management

## Prerequisites

### 1. Supabase Project Setup

1. Create a Supabase project at https://supabase.com
2. Note your project URL and anon key from the Supabase dashboard
3. Set up environment variables:
   ```bash
   export SUPABASE_URL="https://your-project.supabase.co"
   export SUPABASE_ANON_KEY="your-anon-key"
   ```

### 2. Database Schema Setup

The complete database schema is available in [`supabase-schema.sql`](./supabase-schema.sql).

To set up your Supabase database:

1. Open your Supabase project's SQL Editor
2. Copy the contents of `docs/supabase-schema.sql`
3. If using a table prefix (via `SUPABASE_TABLE_PREFIX` environment variable), replace all table names with prefixed versions:
   - Example: `accounts` → `dev_accounts`, `transactions` → `dev_transactions`
4. Execute the SQL script

The schema includes:
- **13 tables** matching Room entities exactly
- **7 indexes** for query performance
- **Row Level Security (RLS)** configuration
- **Example RLS policies** for the accounts table

**Note:** Customize the RLS policies based on your authentication and data access requirements.

## Implementation Status

### ✅ Completed

#### Infrastructure
- [x] Supabase Kotlin client dependency added (v2.6.1)
- [x] Supabase client configuration created
- [x] Hilt module for Supabase DI
- [x] SupabaseTableNames helper for multi-environment support

#### Data Sources (12/12 Complete)
- [x] AccountSupabaseDataSource (replaces AccountDao + WriteAccountDao)
- [x] TransactionSupabaseDataSource (replaces TransactionDao + WriteTransactionDao)
- [x] CategorySupabaseDataSource (replaces CategoryDao + WriteCategoryDao)
- [x] SettingsSupabaseDataSource (replaces SettingsDao + WriteSettingsDao)
- [x] BudgetSupabaseDataSource (replaces BudgetDao + WriteBudgetDao)
- [x] LoanSupabaseDataSource (replaces LoanDao + WriteLoanDao)
- [x] LoanRecordSupabaseDataSource (replaces LoanRecordDao + WriteLoanRecordDao)
- [x] PlannedPaymentRuleSupabaseDataSource (replaces PlannedPaymentRuleDao + WritePlannedPaymentRuleDao)
- [x] TagSupabaseDataSource (replaces TagDao + WriteTagDao)
- [x] TagAssociationSupabaseDataSource (replaces TagAssociationDao + WriteTagAssociationDao)
- [x] ExchangeRateSupabaseDataSource (replaces ExchangeRatesDao + WriteExchangeRatesDao)
- [x] UserSupabaseDataSource (replaces UserDao)

#### Repositories (6/6 Complete)
- [x] AccountRepository updated to use Supabase
- [x] CategoryRepository updated to use Supabase
- [x] TransactionRepository updated to use Supabase
- [x] ExchangeRatesRepository updated to use Supabase
- [x] TagRepository updated to use Supabase
- [x] CurrencyRepository updated to use Supabase

#### Firebase Replacement
- [x] PollRepositoryImpl - Replaced Firebase Firestore with Supabase poll_votes table

#### Database Schema
- [x] Complete SQL schema for 13 tables
- [x] Indexes for query performance
- [x] Row Level Security configuration
- [x] SQL schema extracted to separate file (`supabase-schema.sql`)

#### Documentation
- [x] Complete migration guide
- [x] Implementation summary document
- [x] Environment configuration guide
- [x] Multi-environment table prefix documentation

### 📋 Remaining Tasks (Lower Priority)

#### Testing
- [ ] Unit tests for Supabase data sources
- [ ] Integration tests with test Supabase project
- [ ] End-to-end testing

#### Data Migration
- [ ] Migration utility to export data from Room
- [ ] Migration utility to import data to Supabase
- [ ] Offline-to-online sync strategy

#### Optimization
- [ ] Performance optimization for complex queries
- [ ] Implement caching layer for offline support
- [ ] Real-time subscriptions for live updates

#### Cleanup (Keep for Migration Period)
- [ ] Remove Room database dependencies (after migration period)
- [ ] Remove Firebase Firestore dependencies (keep Crashlytics)
- [ ] Clean up deprecated code

**Note:** Room dependencies are intentionally kept for a transition period to support data migration. DataStore is kept for simple local preferences (device ID, poll voting state) that don't require cloud sync.

## Configuration

### Environment Variables

The app requires three environment variables to connect to Supabase:

- `SUPABASE_URL`: Your Supabase project URL
- `SUPABASE_ANON_KEY`: Your Supabase anonymous (public) key
- `SUPABASE_TABLE_PREFIX`: (Optional) Prefix for all table names (e.g., "dev_", "staging_", "prod_")

For development, you can set these in your IDE's run configuration or in your shell:

```bash
export SUPABASE_URL="https://xxxxxxxxxxxx.supabase.co"
export SUPABASE_ANON_KEY="your-anon-key-here"
export SUPABASE_TABLE_PREFIX="dev_"
```

**Table Prefix Usage:**
- If `SUPABASE_TABLE_PREFIX` is set to "dev_", tables will be named: `dev_accounts`, `dev_transactions`, etc.
- If not set or empty, tables will use default names: `accounts`, `transactions`, etc.
- This allows multiple environments (dev, staging, prod) to share the same Supabase project

For production builds, these should be stored securely and not committed to version control.

## Architecture Changes

### Data Layer

**Before:**
```
UI Layer → Repository → Room DAO → SQLite Database
                     → DataStore → SharedPreferences
```

**After:**
```
UI Layer → Repository → Supabase Data Source → Supabase PostgreSQL
```

### Key Differences

1. **Local vs Cloud**: Room stored data locally on device; Supabase stores data in the cloud
2. **Offline Support**: Need to implement caching and sync strategy for offline scenarios
3. **Authentication**: Supabase provides built-in authentication
4. **Real-time**: Supabase supports real-time subscriptions for live data updates

## Migration Strategy for Existing Users

For users upgrading from the Room-based version:

1. Export existing Room data on app startup (one-time)
2. Upload data to Supabase
3. Verify data integrity
4. Clear local Room database
5. Switch to Supabase-only mode

This will be implemented in a future update.

## Testing

### Unit Tests
Update repository tests to use Supabase data sources with mock SupabaseClient.

### Integration Tests
Requires a test Supabase project for safe testing without affecting production data.

## Rollback Plan

If issues arise:
1. Keep Room dependencies for a few versions
2. Implement feature flag to toggle between Room and Supabase
3. Provide data export functionality

## Performance Considerations

- Network latency: Cloud database calls are slower than local SQLite
- Caching: Implement aggressive caching for frequently accessed data
- Batch operations: Use bulk insert/update operations where possible
- Pagination: Implement pagination for large datasets

## Security

- **RLS (Row Level Security)**: Enable in Supabase to ensure users can only access their own data
- **API Keys**: Store Supabase keys securely, not in version control
- **HTTPS**: All communication with Supabase is encrypted

## Support

For issues or questions:
1. Check Supabase documentation: https://supabase.com/docs
2. Review Ivy Wallet documentation
3. Open an issue on GitHub

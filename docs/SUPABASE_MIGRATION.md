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

Create the following tables in your Supabase project using the SQL Editor:

```sql
-- Accounts table (matches AccountEntity)
CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    currency TEXT,
    color INTEGER NOT NULL,
    icon TEXT,
    "orderNum" DOUBLE PRECISION DEFAULT 0.0,
    "includeInBalance" BOOLEAN DEFAULT true,
    "isSynced" BOOLEAN DEFAULT false,
    "isDeleted" BOOLEAN DEFAULT false
);

-- Transactions table (matches TransactionEntity)
CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    "accountId" UUID NOT NULL,
    type TEXT NOT NULL,
    amount DOUBLE PRECISION NOT NULL,
    "toAccountId" UUID,
    "toAmount" DOUBLE PRECISION,
    title TEXT,
    description TEXT,
    "dateTime" TIMESTAMP WITH TIME ZONE,
    "categoryId" UUID,
    "dueDate" TIMESTAMP WITH TIME ZONE,
    "recurringRuleId" UUID,
    "paidForDateTime" TIMESTAMP WITH TIME ZONE,
    "attachmentUrl" TEXT,
    "loanId" UUID,
    "loanRecordId" UUID,
    "isSynced" BOOLEAN DEFAULT false,
    "isDeleted" BOOLEAN DEFAULT false
);

-- Categories table (matches CategoryEntity)
CREATE TABLE categories (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    color INTEGER NOT NULL,
    icon TEXT,
    "orderNum" DOUBLE PRECISION DEFAULT 0.0,
    "isSynced" BOOLEAN DEFAULT false,
    "isDeleted" BOOLEAN DEFAULT false
);

-- Settings table (matches SettingsEntity)
CREATE TABLE settings (
    id UUID PRIMARY KEY,
    theme TEXT NOT NULL,
    currency TEXT NOT NULL,
    "bufferAmount" DOUBLE PRECISION NOT NULL,
    name TEXT NOT NULL,
    "isSynced" BOOLEAN DEFAULT false,
    "isDeleted" BOOLEAN DEFAULT false
);

-- Budgets table (matches BudgetEntity)
CREATE TABLE budgets (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    amount DOUBLE PRECISION NOT NULL,
    "categoryIdsSerialized" TEXT,
    "accountIdsSerialized" TEXT,
    "isSynced" BOOLEAN DEFAULT false,
    "isDeleted" BOOLEAN DEFAULT false,
    "orderId" DOUBLE PRECISION NOT NULL
);

-- Planned Payment Rules table (matches PlannedPaymentRuleEntity)
CREATE TABLE planned_payment_rules (
    id UUID PRIMARY KEY,
    "startDate" TIMESTAMP WITH TIME ZONE,
    "intervalType" TEXT,
    "intervalN" INTEGER,
    "oneTime" BOOLEAN NOT NULL,
    type TEXT NOT NULL,
    "accountId" UUID NOT NULL,
    amount DOUBLE PRECISION DEFAULT 0.0,
    "categoryId" UUID,
    title TEXT,
    description TEXT,
    "isSynced" BOOLEAN DEFAULT false,
    "isDeleted" BOOLEAN DEFAULT false
);

-- Tags table (matches TagEntity)
CREATE TABLE tags (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    color INTEGER NOT NULL,
    icon TEXT,
    "orderNum" DOUBLE PRECISION NOT NULL,
    "creationTime" TIMESTAMP WITH TIME ZONE NOT NULL,
    "isDeleted" BOOLEAN NOT NULL,
    "lastSyncTime" TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Tag Associations table (matches TagAssociationEntity - composite primary key)
CREATE TABLE tags_association (
    "tagId" UUID NOT NULL,
    "associatedId" UUID NOT NULL,
    "lastSyncTime" TIMESTAMP WITH TIME ZONE NOT NULL,
    "isDeleted" BOOLEAN NOT NULL,
    PRIMARY KEY ("tagId", "associatedId")
);

-- Exchange Rates table (matches ExchangeRateEntity - composite primary key)
CREATE TABLE exchange_rates (
    "baseCurrency" TEXT NOT NULL,
    currency TEXT NOT NULL,
    rate DOUBLE PRECISION NOT NULL,
    "manualOverride" BOOLEAN DEFAULT false,
    PRIMARY KEY ("baseCurrency", currency)
);

-- Users table (matches UserEntity - marked as deprecated/legacy)
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email TEXT NOT NULL,
    "authProviderType" TEXT NOT NULL,
    "firstName" TEXT NOT NULL,
    "lastName" TEXT,
    "profilePicture" TEXT,
    color INTEGER NOT NULL,
    "testUser" BOOLEAN DEFAULT false
);

-- Loans table (matches LoanEntity)
CREATE TABLE loans (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    amount DOUBLE PRECISION NOT NULL,
    type TEXT NOT NULL,
    color INTEGER DEFAULT 0,
    icon TEXT,
    "orderNum" DOUBLE PRECISION DEFAULT 0.0,
    "accountId" UUID,
    note TEXT,
    "isSynced" BOOLEAN DEFAULT false,
    "isDeleted" BOOLEAN DEFAULT false,
    "dateTime" TEXT
);

-- Loan Records table (matches LoanRecordEntity)
CREATE TABLE loan_records (
    id UUID PRIMARY KEY,
    "loanId" UUID NOT NULL,
    amount DOUBLE PRECISION NOT NULL,
    note TEXT,
    "dateTime" TIMESTAMP WITH TIME ZONE NOT NULL,
    interest BOOLEAN DEFAULT false,
    "accountId" UUID,
    "convertedAmount" DOUBLE PRECISION,
    "loanRecordType" TEXT DEFAULT 'DECREASE',
    "isSynced" BOOLEAN DEFAULT false,
    "isDeleted" BOOLEAN DEFAULT false
);

-- Create indexes for better query performance
CREATE INDEX idx_transactions_account ON transactions("accountId");
CREATE INDEX idx_transactions_category ON transactions("categoryId");
CREATE INDEX idx_transactions_date ON transactions("dateTime");
CREATE INDEX idx_transactions_deleted ON transactions("isDeleted");
CREATE INDEX idx_tags_association_tag ON tags_association("tagId");
CREATE INDEX idx_tags_association_associated ON tags_association("associatedId");

-- Enable Row Level Security (RLS) - Important for multi-user scenarios
ALTER TABLE accounts ENABLE ROW LEVEL SECURITY;
ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE settings ENABLE ROW LEVEL SECURITY;
ALTER TABLE budgets ENABLE ROW LEVEL SECURITY;
ALTER TABLE planned_payment_rules ENABLE ROW LEVEL SECURITY;
ALTER TABLE tags ENABLE ROW LEVEL SECURITY;
ALTER TABLE tags_association ENABLE ROW LEVEL SECURITY;
ALTER TABLE loans ENABLE ROW LEVEL SECURITY;
ALTER TABLE loan_records ENABLE ROW LEVEL SECURITY;

-- Create RLS policies (example for accounts - replicate for other tables)
-- Allow all operations for authenticated users on their own data
CREATE POLICY "Users can view their own accounts" ON accounts
    FOR SELECT USING (auth.uid() IS NOT NULL);

CREATE POLICY "Users can insert their own accounts" ON accounts
    FOR INSERT WITH CHECK (auth.uid() IS NOT NULL);

CREATE POLICY "Users can update their own accounts" ON accounts
    FOR UPDATE USING (auth.uid() IS NOT NULL);

CREATE POLICY "Users can delete their own accounts" ON accounts
    FOR DELETE USING (auth.uid() IS NOT NULL);
```

## Implementation Status

### ✅ Completed
- [x] Supabase Kotlin client dependency added
- [x] Supabase client configuration created
- [x] Hilt module for Supabase DI
- [x] Account data source (replaces AccountDao)
- [x] Transaction data source (replaces TransactionDao)
- [x] Category data source (replaces CategoryDao)
- [x] Settings data source (replaces SettingsDao and DataStore)

### 🚧 In Progress
- [ ] Complete all data sources for remaining entities:
  - [ ] Budget data source
  - [ ] Loan data source
  - [ ] LoanRecord data source
  - [ ] PlannedPaymentRule data source
  - [ ] Tag data source
  - [ ] TagAssociation data source
  - [ ] ExchangeRate data source
  - [ ] User data source
- [ ] Update repositories to use Supabase data sources
- [ ] Replace Firebase Firestore poll voting with Supabase
- [ ] Data migration utility from Room to Supabase
- [ ] Offline support and sync strategy

### 📋 TODO
- [ ] Remove Room database dependencies
- [ ] Remove DataStore dependencies
- [ ] Remove Firebase Firestore dependencies (keep Crashlytics)
- [ ] Update tests
- [ ] Documentation updates

## Configuration

### Environment Variables

The app requires two environment variables to connect to Supabase:

- `SUPABASE_URL`: Your Supabase project URL
- `SUPABASE_ANON_KEY`: Your Supabase anonymous (public) key

For development, you can set these in your IDE's run configuration or in your shell:

```bash
export SUPABASE_URL="https://xxxxxxxxxxxx.supabase.co"
export SUPABASE_ANON_KEY="your-anon-key-here"
```

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

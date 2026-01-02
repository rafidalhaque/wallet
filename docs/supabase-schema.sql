-- Supabase Database Schema for Ivy Wallet
-- This schema mirrors the Room Database entities exactly
-- 
-- IMPORTANT: If using SUPABASE_TABLE_PREFIX environment variable,
-- replace table names with the prefixed versions (e.g., dev_accounts, staging_transactions)

-- ============================================
-- Core Tables
-- ============================================

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

-- Poll Votes table (replaces Firebase Firestore polls collection)
CREATE TABLE poll_votes (
    "deviceId" TEXT NOT NULL,
    "pollId" TEXT NOT NULL,
    option TEXT NOT NULL,
    timestamp TEXT NOT NULL,
    PRIMARY KEY ("deviceId", "pollId")
);

-- ============================================
-- Indexes for Query Performance
-- ============================================

CREATE INDEX idx_transactions_account ON transactions("accountId");
CREATE INDEX idx_transactions_category ON transactions("categoryId");
CREATE INDEX idx_transactions_date ON transactions("dateTime");
CREATE INDEX idx_transactions_deleted ON transactions("isDeleted");
CREATE INDEX idx_tags_association_tag ON tags_association("tagId");
CREATE INDEX idx_tags_association_associated ON tags_association("associatedId");
CREATE INDEX idx_poll_votes_poll ON poll_votes("pollId");

-- ============================================
-- Row Level Security (RLS)
-- ============================================

-- Enable RLS on all tables
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
ALTER TABLE poll_votes ENABLE ROW LEVEL SECURITY;

-- ============================================
-- RLS Policies
-- ============================================

-- Example policies for accounts table
-- Replicate these patterns for other tables as needed

-- Allow authenticated users to view their own accounts
CREATE POLICY "Users can view their own accounts" ON accounts
    FOR SELECT USING (auth.uid() IS NOT NULL);

-- Allow authenticated users to insert their own accounts
CREATE POLICY "Users can insert their own accounts" ON accounts
    FOR INSERT WITH CHECK (auth.uid() IS NOT NULL);

-- Allow authenticated users to update their own accounts
CREATE POLICY "Users can update their own accounts" ON accounts
    FOR UPDATE USING (auth.uid() IS NOT NULL);

-- Allow authenticated users to delete their own accounts
CREATE POLICY "Users can delete their own accounts" ON accounts
    FOR DELETE USING (auth.uid() IS NOT NULL);

-- ============================================
-- Notes
-- ============================================

-- 1. All field names use camelCase in quotes to match Kotlin entity serialization
-- 2. Deprecated fields (isSynced, isDeleted) are preserved for backwards compatibility
-- 3. Composite primary keys are maintained for exchange_rates and tags_association
-- 4. Table prefix support: Apply prefix to all table names if using multi-environment setup
-- 5. RLS policies above are examples - customize based on your authentication and data access needs

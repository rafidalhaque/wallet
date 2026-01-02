package com.ivy.data.supabase

/**
 * Exception thrown when a Supabase data operation fails
 */
class SupabaseDataException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

package com.ivy.poll.impl.data

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import arrow.core.Either
import com.ivy.data.datastore.IvyDataStore
import com.ivy.domain.usecase.android.DeviceId
import com.ivy.poll.data.PollRepository
import com.ivy.poll.data.model.PollId
import com.ivy.poll.data.model.PollOptionId
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import java.time.Instant
import javax.inject.Inject

@Serializable
data class PollVoteEntity(
    val deviceId: String,
    val pollId: String,
    val option: String,
    val timestamp: String
)

class PollRepositoryImpl @Inject constructor(
  private val dataStore: IvyDataStore,
  private val supabaseClient: SupabaseClient
) : PollRepository {
  override suspend fun hasVoted(poll: PollId): Boolean {
    return dataStore.data.map { it[votedKey(poll)] ?: false }.first()
  }

  override suspend fun setVoted(poll: PollId, voted: Boolean) {
    dataStore.edit {
      it[votedKey(poll)] = voted
    }
  }

  override suspend fun vote(
    deviceId: DeviceId,
    poll: PollId,
    option: PollOptionId
  ): Either<String, Unit> = try {
    val voteData = PollVoteEntity(
      deviceId = deviceId.value,
      pollId = poll.id,
      option = option.value,
      timestamp = Instant.now().toString()
    )

    supabaseClient.from("poll_votes")
      .upsert(voteData)

    Either.Right(Unit)
  } catch (e: Exception) {
    val message = e.message ?: "Unknown error"
    Either.Left("Supabase - $message")
  }

  private fun votedKey(poll: PollId): Preferences.Key<Boolean> {
    return booleanPreferencesKey("poll.${poll.id}_voted")
  }
}
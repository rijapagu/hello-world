package com.dailystrength.data.repository

import com.dailystrength.data.local.dao.StreakDao
import com.dailystrength.data.mapper.toDomain
import com.dailystrength.data.mapper.toEntity
import com.dailystrength.domain.model.Streak
import com.dailystrength.domain.repository.StreakRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreakRepositoryImpl @Inject constructor(
    private val streakDao: StreakDao,
) : StreakRepository {

    override fun observeStreak(): Flow<Streak> =
        streakDao.observe().map { it?.toDomain() ?: Streak.EMPTY }

    override suspend fun getStreak(): Streak = streakDao.get()?.toDomain() ?: Streak.EMPTY

    override suspend fun saveStreak(streak: Streak) = streakDao.upsert(streak.toEntity())
}

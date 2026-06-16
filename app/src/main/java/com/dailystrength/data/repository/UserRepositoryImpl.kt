package com.dailystrength.data.repository

import com.dailystrength.data.local.dao.UserProfileDao
import com.dailystrength.data.mapper.toDomain
import com.dailystrength.data.mapper.toEntity
import com.dailystrength.domain.model.UserProfile
import com.dailystrength.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userProfileDao: UserProfileDao,
) : UserRepository {

    override fun observeProfile(): Flow<UserProfile?> =
        userProfileDao.observe().map { it?.toDomain() }

    override suspend fun getProfile(): UserProfile? = userProfileDao.get()?.toDomain()

    override suspend fun saveProfile(profile: UserProfile) = userProfileDao.upsert(profile.toEntity())
}

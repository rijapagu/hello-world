package com.dailystrength.data.repository

import com.dailystrength.data.local.dao.ExerciseDao
import com.dailystrength.data.mapper.toDomain
import com.dailystrength.domain.model.Exercise
import com.dailystrength.domain.model.ExerciseCategory
import com.dailystrength.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRepositoryImpl @Inject constructor(
    private val exerciseDao: ExerciseDao,
) : ExerciseRepository {

    override fun observeAll(): Flow<List<Exercise>> =
        exerciseDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getAll(): List<Exercise> = exerciseDao.getAll().map { it.toDomain() }

    override suspend fun getById(id: String): Exercise? = exerciseDao.getById(id)?.toDomain()

    override suspend fun getByCategory(category: ExerciseCategory): List<Exercise> =
        exerciseDao.getByCategory(category).map { it.toDomain() }
}

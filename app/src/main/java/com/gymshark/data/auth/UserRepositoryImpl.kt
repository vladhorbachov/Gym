package com.gymshark.data.auth

import com.gymshark.data.db.dao.UserDao
import com.gymshark.data.db.entity.UserEntity
import kotlinx.coroutines.flow.Flow

class UserRepositoryImpl(
    private val userDao: UserDao,
    private val currentUserStore: CurrentUserStore
) : UserRepository {

    override suspend fun upsert(user: UserEntity) = userDao.upsert(user)
    override suspend fun getById(id: String) = userDao.getById(id)
    override fun observeById(id: String): Flow<UserEntity?> = userDao.observeById(id)
    override suspend fun deleteById(id: String) = userDao.deleteById(id)

    override val currentUserIdFlow: Flow<String?> = currentUserStore.currentUserIdFlow
    override suspend fun setCurrentUserId(id: String) = currentUserStore.setCurrentUserId(id)
    override suspend fun currentUserId(): String? = currentUserStore.currentUserIdOrNull()
}
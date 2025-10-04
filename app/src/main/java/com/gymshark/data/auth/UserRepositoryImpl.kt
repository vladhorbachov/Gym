package com.gymshark.data.auth

import com.gymshark.data.db.dao.UserDao
import com.gymshark.data.db.entity.UserEntity

class UserRepositoryImpl(private val userDao: UserDao) : UserRepository {
    override suspend fun save(user: UserEntity) = userDao.insertRoom(user)
    override suspend fun getById(id: String) = userDao.getUserByIdRoom(id)
}
package com.codedeco.lib.arch.repository.example.repository

import com.codedeco.lib.arch.repository.example.api.user.UserParam
import com.codedeco.lib.arch.repository.example.api.user.UserResponse
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUser(param: UserParam, forceRefresh: Boolean): Flow<UserResponse?>
}
package com.codedeco.lib.arch.repository.example.services

import com.codedeco.lib.arch.repository.example.api.user.UserParam
import com.codedeco.lib.arch.repository.example.api.user.UserResponse

interface UserService {
    fun getUser(ttl: Long, param: UserParam): UserResponse?
}
package com.codedeco.lib.arch.repository.example.services

import com.codedeco.lib.arch.repository.example.api.user.UserParam
import com.codedeco.lib.arch.repository.example.api.user.UserResponse
import java.util.concurrent.ConcurrentHashMap

class UserServiceCache : UserService {
    // Simulate memory cache only
    private val cache = ConcurrentHashMap<UserParam, UserResponse>()

    override fun getUser(ttl: Long, param: UserParam): UserResponse? {
        return cache[param]
    }

    fun put(param: UserParam, response: UserResponse) = apply {
        cache[param] = response
    }
}
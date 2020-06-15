package com.codedeco.lib.arch.repository.example.services

import com.codedeco.lib.arch.repository.example.api.user.UserParam
import com.codedeco.lib.arch.repository.example.api.user.UserResponse

class UserServiceLocal : UserService {
    override fun getUser(ttl: Long, param: UserParam): UserResponse? {
        throw IllegalStateException("User should not have local file")
    }
}
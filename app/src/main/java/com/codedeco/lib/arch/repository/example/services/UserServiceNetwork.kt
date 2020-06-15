package com.codedeco.lib.arch.repository.example.services

import com.codedeco.lib.arch.repository.example.api.user.UserParam
import com.codedeco.lib.arch.repository.example.api.user.UserResponse

class UserServiceNetwork : UserService {
    override fun getUser(ttl: Long, param: UserParam): UserResponse? {
        // Simulate network delay
        Thread.sleep(3000)
        return UserResponse("111222333", "Foo")
    }
}
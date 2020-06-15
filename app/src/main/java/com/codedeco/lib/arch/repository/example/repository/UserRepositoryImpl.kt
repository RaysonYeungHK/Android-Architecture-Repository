package com.codedeco.lib.arch.repository.example.repository

import com.codedeco.lib.arch.repository.CacheStrategy
import com.codedeco.lib.arch.repository.DataSource
import com.codedeco.lib.arch.repository.coroutine.RepositoryHelper
import com.codedeco.lib.arch.repository.example.api.user.UserParam
import com.codedeco.lib.arch.repository.example.api.user.UserResponse
import com.codedeco.lib.arch.repository.example.services.UserService
import com.codedeco.lib.arch.repository.example.services.UserServiceCache
import com.codedeco.lib.arch.repository.example.services.UserServiceLocal
import com.codedeco.lib.arch.repository.example.services.UserServiceNetwork
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class UserRepositoryImpl : UserRepository {

    private val repositoryHelper = RepositoryHelper<UserService>()

    init {
        repositoryHelper.setDataSource(DataSource.Network, UserServiceNetwork())
        repositoryHelper.setDataSource(DataSource.Cache, UserServiceCache())
        repositoryHelper.setDataSource(DataSource.Local, UserServiceLocal())
    }

    override fun getUser(param: UserParam, forceRefresh: Boolean): Flow<UserResponse?> {
        return repositoryHelper.execute(
            if (forceRefresh) {
                CacheStrategy.NetworkReplaceCache
            } else {
                CacheStrategy.Standard
            },
            { service ->
                service.getUser(TimeUnit.DAYS.toMillis(1), param)
            },
            { service, response ->
                if (service is UserServiceCache && response != null) {
                    service.put(param, response)
                }
            }
        )
    }
}
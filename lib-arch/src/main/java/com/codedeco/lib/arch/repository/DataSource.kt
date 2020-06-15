package com.codedeco.lib.arch.repository

/**
 * Definition of different data source
 */
sealed class DataSource(val isCache: Boolean = false) {
    /**
     * Local storage
     */
    object Local : DataSource()

    /**
     * Network
     */
    object Network : DataSource()

    /**
     * Cache, no matter memory cache or disk cache
     */
    object Cache : DataSource(true)
}
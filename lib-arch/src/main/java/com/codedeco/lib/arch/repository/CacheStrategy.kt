package com.codedeco.lib.arch.repository

/**
 * Definition of different cache strategies
 */
class CacheStrategy(
    val isMultipleEmission: Boolean,
    vararg val dataSourceEntries: DataSourceEntry
) {
    companion object {
        /**
         * 1. Try to read from cache
         * 2. Try to read from network if data not found in cache
         * 3. Try to put the data in cache
         */
        val Standard: CacheStrategy = CacheStrategy(
            false,
            DataSourceEntry(DataSource.Cache),
            DataSourceEntry(DataSource.Network)
        )

        /**
         * 1. Try to read from cache
         */
        val CacheOnly: CacheStrategy = CacheStrategy(
            false,
            DataSourceEntry(DataSource.Cache)
        )

        /**
         * 1. Try to read from network
         */
        val NetworkOnly: CacheStrategy = CacheStrategy(
            false,
            DataSourceEntry(DataSource.Network)
        )

        /**
         * 1. Try to read from cache
         * 2. Try to read from network even data found in cache
         * 3. Try to put the data in cache
         */
        val CacheThenNetwork: CacheStrategy = CacheStrategy(
            true,
            DataSourceEntry(DataSource.Cache),
            DataSourceEntry(DataSource.Network)
        )

        /**
         * 1. Try to read from network
         * 2. Try to put the data in cache
         */
        val NetworkReplaceCache: CacheStrategy = CacheStrategy(
            false,
            DataSourceEntry(DataSource.Cache, isIgnoredOnRetrieval = true),
            DataSourceEntry(DataSource.Network)
        )

        /**
         * 1. Try to read from local
         */
        val Local: CacheStrategy = CacheStrategy(
            false,
            DataSourceEntry(DataSource.Local)
        )
    }
}
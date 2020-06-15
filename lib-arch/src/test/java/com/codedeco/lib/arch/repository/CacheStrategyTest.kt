package com.codedeco.lib.arch.repository

import org.junit.Assert
import org.junit.Test

class CacheStrategyTest {
    @Test
    fun assert_Standard() {
        val actual = CacheStrategy.Standard

        Assert.assertEquals(false, actual.isMultipleEmission)
        Assert.assertEquals(2, actual.dataSourceEntries.size)
        Assert.assertEquals(DataSourceEntry(DataSource.Cache), actual.dataSourceEntries[0])
        Assert.assertEquals(DataSourceEntry(DataSource.Network), actual.dataSourceEntries[1])
    }

    @Test
    fun assert_CacheOnly() {
        val actual = CacheStrategy.CacheOnly

        Assert.assertEquals(false, actual.isMultipleEmission)
        Assert.assertEquals(1, actual.dataSourceEntries.size)
        Assert.assertEquals(DataSourceEntry(DataSource.Cache), actual.dataSourceEntries[0])
    }

    @Test
    fun assert_NetworkOnly() {
        val actual = CacheStrategy.NetworkOnly

        Assert.assertEquals(false, actual.isMultipleEmission)
        Assert.assertEquals(1, actual.dataSourceEntries.size)
        Assert.assertEquals(DataSourceEntry(DataSource.Network), actual.dataSourceEntries[0])
    }

    @Test
    fun assert_CacheThenNetwork() {
        val actual = CacheStrategy.CacheThenNetwork

        Assert.assertEquals(true, actual.isMultipleEmission)
        Assert.assertEquals(2, actual.dataSourceEntries.size)
        Assert.assertEquals(DataSourceEntry(DataSource.Cache), actual.dataSourceEntries[0])
        Assert.assertEquals(DataSourceEntry(DataSource.Network), actual.dataSourceEntries[1])
    }

    @Test
    fun assert_NetworkReplaceCache() {
        val actual = CacheStrategy.NetworkReplaceCache

        Assert.assertEquals(false, actual.isMultipleEmission)
        Assert.assertEquals(2, actual.dataSourceEntries.size)
        Assert.assertEquals(DataSourceEntry(DataSource.Cache, isIgnoredOnRetrieval = true), actual.dataSourceEntries[0])
        Assert.assertEquals(DataSourceEntry(DataSource.Network), actual.dataSourceEntries[1])
    }

    @Test
    fun assert_Local() {
        val actual = CacheStrategy.Local

        Assert.assertEquals(false, actual.isMultipleEmission)
        Assert.assertEquals(1, actual.dataSourceEntries.size)
        Assert.assertEquals(DataSourceEntry(DataSource.Local), actual.dataSourceEntries[0])
    }
}
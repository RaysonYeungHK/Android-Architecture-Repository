package com.codedeco.lib.arch.repository

import org.junit.Assert
import org.junit.Test

class DataSourceTest {
    @Test
    fun assert_Local() {
        val actual = DataSource.Local

        Assert.assertEquals(false, actual.isCache)
    }

    @Test
    fun assert_Network() {
        val actual = DataSource.Network

        Assert.assertEquals(false, actual.isCache)
    }

    @Test
    fun assert_Cache() {
        val actual = DataSource.Cache

        Assert.assertEquals(true, actual.isCache)
    }
}
package com.codedeco.lib.arch.repository

/**
 * Wrap the data source with additional retrieval configuration
 */
data class DataSourceEntry(
    val dataSource: DataSource,
    val isIgnoredOnRetrieval: Boolean = false
)
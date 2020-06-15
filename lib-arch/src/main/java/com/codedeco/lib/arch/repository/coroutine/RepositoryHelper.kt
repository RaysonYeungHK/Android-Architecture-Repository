package com.codedeco.lib.arch.repository.coroutine

import android.util.Log
import com.codedeco.lib.arch.repository.CacheStrategy
import com.codedeco.lib.arch.repository.DataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RepositoryHelper<DataProvider : Any> {

    companion object {
        private const val TAG = "RepositoryHelper"
    }

    private val dataSourceMap: MutableMap<DataSource, DataProvider> = HashMap()

    fun setDataSource(dataSource: DataSource, dataProvider: DataProvider) = apply {
        dataSourceMap[dataSource] = dataProvider
    }

    fun removeDataSource(dataSource: DataSource) = apply {
        dataSourceMap.remove(dataSource)
    }

    fun <Response> execute(cacheStrategy: CacheStrategy,
                           retrievalAction: ((dataProvider: DataProvider) -> Response),
                           cacheAction: ((dataProvider: DataProvider, Response) -> Unit)): Flow<Response?> {
        return flow {
            var isEmittedAtLeastOnce = false
            var response: Response? = null

            for (i in cacheStrategy.dataSourceEntries.indices) {
                val cacheEntry = cacheStrategy.dataSourceEntries[i]
                if (cacheEntry.isIgnoredOnRetrieval) {
                    continue
                }
                val dataSource = cacheEntry.dataSource
                val dataProvider: DataProvider? = dataSourceMap[dataSource]
                if (dataProvider == null) {
                    Log.w(TAG, "DataProvider is not set for DataSource: ${dataSource::class.java.simpleName}")
                    continue
                }

                try {
                    response = retrievalAction.invoke(dataProvider)
                    if (response == null) {
                        throw NullPointerException("Data not found in DataSource: ${dataSource::class.java.simpleName}, DataProvider: ${dataProvider::class.java.simpleName}")
                    }
                    Log.d(TAG, "Data found in DataSource: ${dataSource::class.java.simpleName}, DataProvider: ${dataProvider::class.java.simpleName}")

                    for (j in (i - 1) downTo 0) {
                        val ds = cacheStrategy.dataSourceEntries[j].dataSource
                        if (ds.isCache) {
                            dataSourceMap[ds]?.run {
                                cacheAction.invoke(this, response)
                            }
                        }
                    }

                    emit(response)
                    isEmittedAtLeastOnce = true

                    if (!cacheStrategy.isMultipleEmission) {
                        return@flow
                    }

                    Log.d(TAG, "This is multiple emission call, we will keep trying till the end")
                } catch (e: Exception) {
                    if (i == cacheStrategy.dataSourceEntries.size - 1 && !isEmittedAtLeastOnce) {
                        throw NullPointerException("Last DataSource retrieval failed and never get any data, throw exception tp actual caller")
                    }
                }
            }
            if (!isEmittedAtLeastOnce) {
                Log.w(TAG, "Registered DataSource seems less than CacheStrategy DataSource, please double check if your DataSource is registered.")
                throw NullPointerException("Last DataSource retrieval failed and never get any data, throw exception tp actual caller")
            }
        }
    }
}
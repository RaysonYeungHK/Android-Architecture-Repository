package com.codedeco.lib.arch.repository.rx

import android.util.Log
import com.codedeco.lib.arch.repository.CacheStrategy
import com.codedeco.lib.arch.repository.DataSource
import io.reactivex.rxjava3.core.Observable

class RepositoryHelper<DataProvider : Any> {

    companion object {
        private const val TAG = "RepositoryHelper"
    }

    private val dataSourceMap: MutableMap<DataSource, DataProvider> = HashMap()

    fun setDataSource(dataSource: DataSource, service: DataProvider): RepositoryHelper<DataProvider> {
        dataSourceMap[dataSource] = service
        return this
    }

    fun removeDataSource(dataSource: DataSource): RepositoryHelper<DataProvider> {
        dataSourceMap.remove(dataSource)
        return this
    }

    /**
     * Data retrieval handling, hit to return, fail to next
     *
     */
    fun <Response> execute(cacheStrategy: CacheStrategy,
                           action: ((service: DataProvider) -> Response),
                           cacheAction: ((service: DataProvider, Response) -> Unit)): Observable<Response> {
        return Observable.create<Response> {
            var isEmittedAtLeastOnce = false
            var response: Response?

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
                    response = action.invoke(dataProvider)
                    if (response == null) {
                        throw NullPointerException("Data not found in DataSource: ${dataSource::class.java.simpleName}, DataProvider: ${dataProvider::class.java.simpleName}")
                    }
                    Log.d(TAG, "Data found in " + dataProvider::class.java.simpleName)
                    // If we try to get data from, MemoryCache -> DiskCache -> Network
                    // Sure we want to save the response into the cache, next time we can hit the cache
                    // It will be save in DiskCache -> MemoryCache
                    for (j in (i - 1) downTo 0) {
                        val ds = cacheStrategy.dataSourceEntries[j].dataSource
                        if (ds.isCache) {
                            dataSourceMap[ds]?.run {
                                cacheAction.invoke(this, response)
                            }
                        }
                    }

                    it.onNext(response)
                    isEmittedAtLeastOnce = true

                    if (!cacheStrategy.isMultipleEmission) {
                        it.onComplete()
                        return@create
                    }
                    Log.d(TAG, "This is multiple emission call, we will keep trying till the end")
                } catch (e: Exception) {
                    Log.w(TAG, dataProvider::class.java.simpleName + " got exception " + e.message)
                    if (i == cacheStrategy.dataSourceEntries.size - 1 && !isEmittedAtLeastOnce) {
                        Log.w(TAG, "Last data source retrieve failed and never got data, throw exception to actual caller")
                        try {
                            it.onError(e)
                        } catch (ignored: Exception) {
                            Log.w(TAG, "no one is handling the exception: $e")
                        }
                        return@create
                    }
                }
            }
            if (!isEmittedAtLeastOnce) {
                // Let's throw NullPointerException if sth wrong
                Log.w(TAG, "Repository DataSource count: " + dataSourceMap.size + " seems less than CacheStrategy.dataSources count: " + cacheStrategy.dataSourceEntries.size)
                try {
                    it.onError(NullPointerException())
                } catch (ignored: Exception) {
                    Log.w(TAG, "no one is handling the exception: ${NullPointerException()}")
                }
            }
            it.onComplete()
        }
    }
}
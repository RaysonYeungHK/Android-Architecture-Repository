package com.codedeco.lib.arch.repository.rx

import com.codedeco.lib.arch.repository.CacheStrategy
import com.codedeco.lib.arch.repository.DataSource
import io.reactivex.rxjava3.core.Observable
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class RepositoryHelperTest {

    private val sut = RepositoryHelper<TestService>()

    @Mock
    private lateinit var networkService: TestService

    @Mock
    private lateinit var cacheService: CacheService

    @Mock
    private lateinit var localService: TestService

    private val hasDataService = HasDataService()
    private val exceptionService = ExceptionService()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        sut.setDataSource(DataSource.Network, networkService)
        sut.setDataSource(DataSource.Cache, cacheService)
        sut.setDataSource(DataSource.Local, localService)
    }

    private fun getData(cacheStrategy: CacheStrategy): Observable<String?> {
        return sut.execute(cacheStrategy,
                { service -> service.getData() },
                { service, response ->
                    if (response != null && service is TestCacheable) {
                        service.cache(response)
                    }
                })
    }

    @Test
    fun standard_hit_network() {
        Mockito.`when`(networkService.getData()).then { hasDataService.getData() }
        Mockito.`when`(cacheService.getData()).then { exceptionService.getData() }
        Mockito.`when`(localService.getData()).then { exceptionService.getData() }

        // Check data
        getData(CacheStrategy.Standard)
                .test()
                .assertValueCount(1)
                .assertValueAt(0, RESPONSE)
                .assertNoErrors()
                .assertComplete()

        // Check service hit
        Mockito.verify(networkService, Mockito.times(1)).getData()
        Mockito.verifyNoMoreInteractions(networkService)

        Mockito.verify(cacheService, Mockito.times(1)).getData()
        Mockito.verify(cacheService, Mockito.times(1)).cache(RESPONSE)
        Mockito.verifyNoMoreInteractions(cacheService)

        Mockito.verifyNoInteractions(localService)
    }

    @Test
    fun standard_hit_network_failed() {
        Mockito.`when`(networkService.getData()).then { exceptionService.getData() }
        Mockito.`when`(cacheService.getData()).then { exceptionService.getData() }
        Mockito.`when`(localService.getData()).then { exceptionService.getData() }

        // Check data
        getData(CacheStrategy.Standard)
                .test()
                .assertNoValues()
                .assertError(Exception::class.java)

        // Check service hit
        Mockito.verify(networkService, Mockito.times(1)).getData()
        Mockito.verifyNoMoreInteractions(networkService)

        Mockito.verify(cacheService, Mockito.times(1)).getData()
        Mockito.verifyNoMoreInteractions(cacheService)

        Mockito.verifyNoInteractions(localService)
    }

    @Test
    fun standard_hit_network_no_network_service() {
        Mockito.`when`(networkService.getData()).then { hasDataService.getData() }
        Mockito.`when`(cacheService.getData()).then { exceptionService.getData() }
        Mockito.`when`(localService.getData()).then { exceptionService.getData() }

        sut.removeDataSource(DataSource.Network)

        // Check data
        getData(CacheStrategy.Standard)
                .test()
                .assertNoValues()
                .assertError(Exception::class.java)

        // Check service hit
        Mockito.verifyNoInteractions(networkService)

        Mockito.verify(cacheService, Mockito.times(1)).getData()
        Mockito.verifyNoMoreInteractions(cacheService)

        Mockito.verifyNoInteractions(localService)
    }

    @Test
    fun standard_hit_network_no_cache_service() {
        Mockito.`when`(networkService.getData()).then { hasDataService.getData() }
        Mockito.`when`(cacheService.getData()).then { exceptionService.getData() }
        Mockito.`when`(localService.getData()).then { exceptionService.getData() }

        sut.removeDataSource(DataSource.Cache)

        // Check data
        getData(CacheStrategy.Standard)
                .test()
                .assertValueCount(1)
                .assertValueAt(0, RESPONSE)
                .assertNoErrors()
                .assertComplete()

        // Check service hit
        Mockito.verify(networkService, Mockito.times(1)).getData()
        Mockito.verifyNoMoreInteractions(networkService)

        Mockito.verifyNoInteractions(cacheService)

        Mockito.verifyNoInteractions(localService)
    }

    @Test
    fun standard_hit_cache() {
        Mockito.`when`(networkService.getData()).then { hasDataService.getData() }
        Mockito.`when`(cacheService.getData()).then { hasDataService.getData() }
        Mockito.`when`(localService.getData()).then { exceptionService.getData() }

        // Check data
        getData(CacheStrategy.Standard)
                .test()
                .assertValueCount(1)
                .assertValueAt(0, RESPONSE)
                .assertNoErrors()
                .assertComplete()

        // Check service hit
        Mockito.verifyNoInteractions(networkService)

        Mockito.verify(cacheService, Mockito.times(1)).getData()
        Mockito.verifyNoMoreInteractions(cacheService)

        Mockito.verifyNoInteractions(localService)
    }

    @Test
    fun standard_hit_cache_no_cache_service() {
        Mockito.`when`(networkService.getData()).then { exceptionService.getData() }
        Mockito.`when`(cacheService.getData()).then { exceptionService.getData() }
        Mockito.`when`(localService.getData()).then { exceptionService.getData() }

        sut.removeDataSource(DataSource.Cache)

        // Check data
        getData(CacheStrategy.Standard)
                .test()
                .assertNoValues()
                .assertError(Exception::class.java)

        // Check service hit
        Mockito.verify(networkService, Mockito.times(1)).getData()
        Mockito.verifyNoMoreInteractions(networkService)

        Mockito.verifyNoInteractions(cacheService)

        Mockito.verifyNoInteractions(localService)
    }

    @Test
    fun cacheOnly_hit_cache() {
        Mockito.`when`(networkService.getData()).then { hasDataService.getData() }
        Mockito.`when`(cacheService.getData()).then { hasDataService.getData() }
        Mockito.`when`(localService.getData()).then { exceptionService.getData() }

        // Check data
        getData(CacheStrategy.CacheOnly)
                .test()
                .assertValueCount(1)
                .assertValueAt(0, RESPONSE)
                .assertComplete()

        // Check service hit
        Mockito.verifyNoInteractions(networkService)

        Mockito.verify(cacheService, Mockito.times(1)).getData()
        Mockito.verifyNoMoreInteractions(cacheService)

        Mockito.verifyNoInteractions(localService)
    }

    @Test
    fun cacheOnly_hit_cache_failed() {
        Mockito.`when`(networkService.getData()).then { this.hasDataService.getData() }
        Mockito.`when`(cacheService.getData()).then { exceptionService.getData() }
        Mockito.`when`(localService.getData()).then { exceptionService.getData() }

        // Check data
        getData(CacheStrategy.CacheOnly)
                .test()
                .assertNoValues()
                .assertError(Exception::class.java)

        // Check service hit
        Mockito.verifyNoInteractions(networkService)

        Mockito.verify(cacheService, Mockito.times(1)).getData()
        Mockito.verifyNoMoreInteractions(cacheService)

        Mockito.verifyNoInteractions(localService)
    }

    @Test
    fun cacheOnly_no_cache_service() {
        Mockito.`when`(networkService.getData()).then { this.hasDataService.getData() }
        Mockito.`when`(cacheService.getData()).then { exceptionService.getData() }
        Mockito.`when`(localService.getData()).then { exceptionService.getData() }

        sut.removeDataSource(DataSource.Cache)

        // Check data
        getData(CacheStrategy.CacheOnly)
                .test()
                .assertNoValues()
                .assertError(Exception::class.java)

        // Check service hit
        Mockito.verifyNoInteractions(networkService)

        Mockito.verifyNoInteractions(cacheService)

        Mockito.verifyNoInteractions(localService)
    }

    @Test
    fun networkOnly_hit_network() {
        Mockito.`when`(networkService.getData()).then { hasDataService.getData() }
        Mockito.`when`(cacheService.getData()).then { exceptionService.getData() }
        Mockito.`when`(localService.getData()).then { exceptionService.getData() }

        // Check data
        getData(CacheStrategy.NetworkOnly)
                .test()
                .assertValueCount(1)
                .assertValueAt(0, RESPONSE)
                .assertComplete()

        // Check service hit
        Mockito.verify(networkService, Mockito.times(1)).getData()
        Mockito.verifyNoMoreInteractions(networkService)

        Mockito.verifyNoInteractions(cacheService)

        Mockito.verifyNoInteractions(localService)
    }

    @Test
    fun networkOnly_hit_network_failed() {
        Mockito.`when`(networkService.getData()).then { this.exceptionService.getData() }
        Mockito.`when`(cacheService.getData()).then { exceptionService.getData() }
        Mockito.`when`(localService.getData()).then { exceptionService.getData() }

        // Check data
        getData(CacheStrategy.NetworkOnly)
                .test()
                .assertNoValues()
                .assertError(Exception::class.java)

        // Check service hit
        Mockito.verify(networkService, Mockito.times(1)).getData()
        Mockito.verifyNoMoreInteractions(networkService)

        Mockito.verifyNoInteractions(cacheService)

        Mockito.verifyNoInteractions(localService)
    }

    @Test
    fun networkOnly_no_network_service() {
        Mockito.`when`(networkService.getData()).then { this.hasDataService.getData() }
        Mockito.`when`(cacheService.getData()).then { exceptionService.getData() }
        Mockito.`when`(localService.getData()).then { exceptionService.getData() }

        sut.removeDataSource(DataSource.Network)

        // Check data
        getData(CacheStrategy.NetworkOnly)
                .test()
                .assertNoValues()
                .assertError(Exception::class.java)

        // Check service hit
        Mockito.verifyNoInteractions(networkService)

        Mockito.verifyNoInteractions(cacheService)

        Mockito.verifyNoInteractions(localService)
    }

    @Test
    fun cacheThenNetwork_hit_network_hit_cache() {
        Mockito.`when`(networkService.getData()).then { hasDataService.getData() }
        Mockito.`when`(cacheService.getData()).then { hasDataService.getData() }
        Mockito.`when`(localService.getData()).then { exceptionService.getData() }

        // Check data
        getData(CacheStrategy.CacheThenNetwork)
                .test()
                .assertValueCount(2)
                .assertValueAt(0, RESPONSE)
                .assertValueAt(1, RESPONSE)
                .assertComplete()

        // Check service hit
        Mockito.verify(networkService, Mockito.times(1)).getData()
        Mockito.verifyNoMoreInteractions(networkService)

        Mockito.verify(cacheService, Mockito.times(1)).getData()
        Mockito.verify(cacheService, Mockito.times(1)).cache(RESPONSE)
        Mockito.verifyNoMoreInteractions(cacheService)

        Mockito.verifyNoInteractions(localService)
    }

    @Test
    fun cacheThenNetwork_hit_network() {
        Mockito.`when`(networkService.getData()).then { this.hasDataService.getData() }
        Mockito.`when`(cacheService.getData()).then { exceptionService.getData() }
        Mockito.`when`(localService.getData()).then { exceptionService.getData() }

        // Check data
        getData(CacheStrategy.CacheThenNetwork)
                .test()
                .assertValueCount(1)
                .assertValueAt(0, RESPONSE)
                .assertComplete()

        // Check service hit
        Mockito.verify(networkService, Mockito.times(1)).getData()
        Mockito.verifyNoMoreInteractions(networkService)

        Mockito.verify(cacheService, Mockito.times(1)).getData()
        Mockito.verify(cacheService, Mockito.times(1)).cache(RESPONSE)
        Mockito.verifyNoMoreInteractions(cacheService)

        Mockito.verifyNoInteractions(localService)
    }

    @Test
    fun cacheThenNetwork_hit_cache() {
        Mockito.`when`(networkService.getData()).then { this.exceptionService.getData() }
        Mockito.`when`(cacheService.getData()).then { hasDataService.getData() }
        Mockito.`when`(localService.getData()).then { exceptionService.getData() }

        // Check data
        getData(CacheStrategy.CacheThenNetwork)
                .test()
                .assertValueCount(1)
                .assertValueAt(0, RESPONSE)
                .assertComplete()

        // Check service hit
        Mockito.verify(networkService, Mockito.times(1)).getData()
        Mockito.verifyNoMoreInteractions(networkService)

        Mockito.verify(cacheService, Mockito.times(1)).getData()
        Mockito.verifyNoMoreInteractions(cacheService)

        Mockito.verifyNoInteractions(localService)
    }

    @Test
    fun cacheThenNetwork_hit_network_no_cache_service() {
        Mockito.`when`(networkService.getData()).then { hasDataService.getData() }
        Mockito.`when`(cacheService.getData()).then { hasDataService.getData() }
        Mockito.`when`(localService.getData()).then { exceptionService.getData() }

        sut.removeDataSource(DataSource.Cache)

        // Check data
        getData(CacheStrategy.CacheThenNetwork)
                .test()
                .assertValueCount(1)
                .assertValueAt(0, RESPONSE)
                .assertComplete()

        // Check service hit
        Mockito.verify(networkService, Mockito.times(1)).getData()
        Mockito.verifyNoMoreInteractions(networkService)

        Mockito.verifyNoInteractions(cacheService)

        Mockito.verifyNoInteractions(localService)
    }

    @Test
    fun cacheThenNetwork_hit_cache_no_network_service() {
        Mockito.`when`(networkService.getData()).then { hasDataService.getData() }
        Mockito.`when`(cacheService.getData()).then { hasDataService.getData() }
        Mockito.`when`(localService.getData()).then { exceptionService.getData() }

        sut.removeDataSource(DataSource.Network)

        // Check data
        getData(CacheStrategy.CacheThenNetwork)
                .test()
                .assertValueCount(1)
                .assertValueAt(0, RESPONSE)
                .assertComplete()

        // Check service hit
        Mockito.verifyNoInteractions(networkService)

        Mockito.verify(cacheService, Mockito.times(1)).getData()
        Mockito.verifyNoMoreInteractions(cacheService)

        Mockito.verifyNoInteractions(localService)
    }

    @Test
    fun cacheThenNetwork_no_network_service_no_cache_service() {
        Mockito.`when`(networkService.getData()).then { hasDataService.getData() }
        Mockito.`when`(cacheService.getData()).then { hasDataService.getData() }
        Mockito.`when`(localService.getData()).then { exceptionService.getData() }

        sut.removeDataSource(DataSource.Network)
        sut.removeDataSource(DataSource.Cache)

        // Check data
        getData(CacheStrategy.CacheThenNetwork)
                .test()
                .assertNoValues()
                .assertError(Exception::class.java)

        // Check service hit
        Mockito.verifyNoInteractions(networkService)

        Mockito.verifyNoInteractions(cacheService)

        Mockito.verifyNoInteractions(localService)
    }

    @Test
    fun networkReplaceCache_hit_network() {
        Mockito.`when`(networkService.getData()).then { hasDataService.getData() }
        Mockito.`when`(cacheService.getData()).then { hasDataService.getData() }
        Mockito.`when`(localService.getData()).then { exceptionService.getData() }

        // Check data
        getData(CacheStrategy.NetworkReplaceCache)
                .test()
                .assertValueCount(1)
                .assertValueAt(0, RESPONSE)
                .assertComplete()

        // Check service hit
        Mockito.verify(networkService, Mockito.times(1)).getData()
        Mockito.verifyNoMoreInteractions(networkService)

        Mockito.verify(cacheService, Mockito.times(1)).cache(RESPONSE)
        Mockito.verifyNoMoreInteractions(cacheService)

        Mockito.verifyNoInteractions(localService)
    }

    @Test
    fun networkReplaceCache_no_network_service() {
        Mockito.`when`(networkService.getData()).then { hasDataService.getData() }
        Mockito.`when`(cacheService.getData()).then { hasDataService.getData() }
        Mockito.`when`(localService.getData()).then { exceptionService.getData() }

        sut.removeDataSource(DataSource.Network)

        // Check data
        getData(CacheStrategy.NetworkReplaceCache)
                .test()
                .assertNoValues()
                .assertError(Exception::class.java)

        // Check service hit
        Mockito.verifyNoInteractions(networkService)

        Mockito.verifyNoInteractions(cacheService)

        Mockito.verifyNoInteractions(localService)
    }

    @Test
    fun networkReplaceCache_hit_network_no_cache_service() {
        Mockito.`when`(networkService.getData()).then { hasDataService.getData() }
        Mockito.`when`(cacheService.getData()).then { hasDataService.getData() }
        Mockito.`when`(localService.getData()).then { exceptionService.getData() }

        sut.removeDataSource(DataSource.Cache)

        // Check data
        getData(CacheStrategy.NetworkReplaceCache)
                .test()
                .assertValueCount(1)
                .assertValueAt(0, RESPONSE)
                .assertComplete()

        // Check service hit
        Mockito.verify(networkService, Mockito.times(1)).getData()
        Mockito.verifyNoMoreInteractions(networkService)

        Mockito.verifyNoInteractions(cacheService)

        Mockito.verifyNoInteractions(localService)
    }

    @Test
    fun networkReplaceCache_no_network_service_no_cache_service() {
        Mockito.`when`(networkService.getData()).then { hasDataService.getData() }
        Mockito.`when`(cacheService.getData()).then { hasDataService.getData() }
        Mockito.`when`(localService.getData()).then { exceptionService.getData() }

        sut.removeDataSource(DataSource.Network)
        sut.removeDataSource(DataSource.Cache)

        // Check data
        getData(CacheStrategy.NetworkReplaceCache)
                .test()
                .assertNoValues()
                .assertError(Exception::class.java)

        // Check service hit
        Mockito.verifyNoInteractions(networkService)

        Mockito.verifyNoInteractions(cacheService)

        Mockito.verifyNoInteractions(localService)
    }

    @Test
    fun local_hit_local() {
        Mockito.`when`(networkService.getData()).then { exceptionService.getData() }
        Mockito.`when`(cacheService.getData()).then { exceptionService.getData() }
        Mockito.`when`(localService.getData()).then { hasDataService.getData() }

        // Check data
        getData(CacheStrategy.Local)
                .test()
                .assertValueCount(1)
                .assertValueAt(0, RESPONSE)
                .assertComplete()

        // Check service hit
        Mockito.verifyNoInteractions(networkService)

        Mockito.verifyNoInteractions(cacheService)

        Mockito.verify(localService, Mockito.times(1)).getData()
        Mockito.verifyNoMoreInteractions(localService)
    }

    @Test
    fun local_hit_local_failed() {
        Mockito.`when`(networkService.getData()).then { this.exceptionService.getData() }
        Mockito.`when`(cacheService.getData()).then { exceptionService.getData() }
        Mockito.`when`(localService.getData()).then { exceptionService.getData() }

        // Check data
        getData(CacheStrategy.Local)
                .test()
                .assertNoValues()
                .assertError(Exception::class.java)

        // Check service hit
        Mockito.verifyNoInteractions(networkService)

        Mockito.verifyNoInteractions(cacheService)

        Mockito.verify(localService, Mockito.times(1)).getData()
        Mockito.verifyNoMoreInteractions(localService)
    }

    @Test
    fun local_no_local_service() {
        Mockito.`when`(networkService.getData()).then { this.exceptionService.getData() }
        Mockito.`when`(cacheService.getData()).then { exceptionService.getData() }
        Mockito.`when`(localService.getData()).then { exceptionService.getData() }

        sut.removeDataSource(DataSource.Local)

        // Check data
        getData(CacheStrategy.Local)
                .test()
                .assertNoValues()
                .assertError(Exception::class.java)

        // Check service hit
        Mockito.verifyNoInteractions(networkService)

        Mockito.verifyNoInteractions(cacheService)

        Mockito.verifyNoInteractions(localService)
    }
}

private const val RESPONSE = "Response"

private interface TestService {
    fun getData(): String?
}

private open class HasDataService : TestService {
    override fun getData(): String? {
        // Assume API always success
        return RESPONSE
    }
}

private open class ExceptionService : TestService {
    override fun getData(): String? {
        throw Exception("Something is wrong")
    }
}

/**
 * Test only dummy cacheable class
 */
private interface TestCacheable {
    fun cache(data: String)
}

private open class CacheService : TestService, TestCacheable {
    private var cacheData: String? = null

    override fun getData(): String? {
        return cacheData
    }

    override fun cache(data: String) {
        cacheData = data
    }
}

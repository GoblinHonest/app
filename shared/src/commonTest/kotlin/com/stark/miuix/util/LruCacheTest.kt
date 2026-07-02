package com.stark.miuix.util

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LruCacheTest {

    @Test
    fun putAndGet() = runTest {
        val cache = LruCache<String>(maxSize = 10)
        cache.put("key1", "value1")
        assertEquals("value1", cache.get("key1"))
    }

    @Test
    fun getMissReturnsNull() = runTest {
        val cache = LruCache<String>(maxSize = 10)
        assertNull(cache.get("missing"))
    }

    @Test
    fun evictsOldestWhenFull() = runTest {
        val cache = LruCache<String>(maxSize = 2)
        cache.put("a", "1")
        cache.put("b", "2")
        cache.put("c", "3")

        assertNull(cache.get("a"))
        assertEquals("2", cache.get("b"))
        assertEquals("3", cache.get("c"))
    }

    @Test
    fun getOrPutCachesResult() = runTest {
        val cache = LruCache<Int>(maxSize = 10)
        var callCount = 0
        val loader = suspend { callCount++; 42 }

        val first = cache.getOrPut("key", loader)
        val second = cache.getOrPut("key", loader)

        assertEquals(42, first)
        assertEquals(42, second)
        assertEquals(1, callCount)
    }

    @Test
    fun clearRemovesAll() = runTest {
        val cache = LruCache<String>(maxSize = 10)
        cache.put("a", "1")
        cache.put("b", "2")
        cache.clear()

        assertNull(cache.get("a"))
        assertNull(cache.get("b"))
    }
}

package com.stark.miuix.data.repository

import com.stark.miuix.data.model.VideoSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SourceRepositoryTest {

    @Test
    fun addAndRetrieveSource() {
        val repo = SourceRepository()
        val source = VideoSource(sourceName = "TestSource", sourceUrl = "https://test.com")
        repo.addSource(source)

        assertEquals(1, repo.sources.value.size)
        assertNotNull(repo.getSourceByName("TestSource"))
    }

    @Test
    fun addDuplicateOverwrites() {
        val repo = SourceRepository()
        repo.addSource(VideoSource(sourceName = "S1", sourceUrl = "https://v1.com", version = 1))
        repo.addSource(VideoSource(sourceName = "S1", sourceUrl = "https://v2.com", version = 2))

        assertEquals(1, repo.sources.value.size)
        assertEquals(2, repo.getSourceByName("S1")?.version)
    }

    @Test
    fun toggleDisablesSource() {
        val repo = SourceRepository()
        repo.addSource(VideoSource(sourceName = "S", sourceUrl = "https://s.com", enabled = true))

        repo.toggleSource("S")
        assertEquals(false, repo.getSourceByName("S")?.enabled)

        repo.toggleSource("S")
        assertEquals(true, repo.getSourceByName("S")?.enabled)
    }

    @Test
    fun removeSource() {
        val repo = SourceRepository()
        repo.addSource(VideoSource(sourceName = "Del", sourceUrl = "https://del.com"))
        repo.removeSource("Del")

        assertEquals(0, repo.sources.value.size)
        assertNull(repo.getSourceByName("Del"))
    }

    @Test
    fun getEnabledOnly() {
        val repo = SourceRepository()
        repo.addSource(VideoSource(sourceName = "A", sourceUrl = "a", enabled = true))
        repo.addSource(VideoSource(sourceName = "B", sourceUrl = "b", enabled = false))

        val enabled = repo.getEnabledSources()
        assertEquals(1, enabled.size)
        assertEquals("A", enabled[0].sourceName)
    }

    @Test
    fun importFromJsonArray() {
        val repo = SourceRepository()
        val json = """[
            {"sourceName":"X","sourceUrl":"https://x.com"},
            {"sourceName":"Y","sourceUrl":"https://y.com"}
        ]"""
        val result = repo.importFromJson(json)
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull())
        assertEquals(2, repo.sources.value.size)
    }

    @Test
    fun importInvalidJsonFails() {
        val repo = SourceRepository()
        val result = repo.importFromJson("not json at all")
        assertTrue(result.isFailure)
    }
}

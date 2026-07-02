package com.stark.miuix.data.repository

import com.stark.miuix.data.model.Favorite
import com.stark.miuix.data.model.WatchHistory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserDataRepositoryTest {

    @Test
    fun addHistoryAppearsInList() {
        val repo = UserDataRepository()
        repo.addWatchHistory(makeHistory("v1", "Video 1"))

        assertEquals(1, repo.watchHistory.value.size)
        assertEquals("Video 1", repo.watchHistory.value[0].title)
    }

    @Test
    fun duplicateHistoryMovesToTop() {
        val repo = UserDataRepository()
        repo.addWatchHistory(makeHistory("v1", "First"))
        repo.addWatchHistory(makeHistory("v2", "Second"))
        repo.addWatchHistory(makeHistory("v1", "First Updated"))

        assertEquals(2, repo.watchHistory.value.size)
        assertEquals("First Updated", repo.watchHistory.value[0].title)
    }

    @Test
    fun clearHistoryEmptiesList() {
        val repo = UserDataRepository()
        repo.addWatchHistory(makeHistory("v1", "A"))
        repo.addWatchHistory(makeHistory("v2", "B"))
        repo.clearHistory()

        assertTrue(repo.watchHistory.value.isEmpty())
    }

    @Test
    fun toggleFavoriteAddsAndRemoves() {
        val repo = UserDataRepository()
        val fav = Favorite(videoId = "f1", title = "Fav", cover = "", sourceName = "S", detailUrl = "/d")

        val added = repo.toggleFavorite(fav)
        assertTrue(added)
        assertTrue(repo.isFavorite("f1"))

        val removed = repo.toggleFavorite(fav)
        assertFalse(removed)
        assertFalse(repo.isFavorite("f1"))
    }

    @Test
    fun removeFavoriteById() {
        val repo = UserDataRepository()
        repo.toggleFavorite(Favorite("f1", "A", "", "S", "/a"))
        repo.toggleFavorite(Favorite("f2", "B", "", "S", "/b"))

        repo.removeFavorite("f1")
        assertEquals(1, repo.favorites.value.size)
        assertEquals("B", repo.favorites.value[0].title)
    }

    @Test
    fun historyLimitedTo200() {
        val repo = UserDataRepository()
        repeat(210) { i ->
            repo.addWatchHistory(makeHistory("v$i", "Video $i"))
        }
        assertTrue(repo.watchHistory.value.size <= 200)
    }

    private fun makeHistory(id: String, title: String) = WatchHistory(
        videoId = id,
        title = title,
        cover = "",
        sourceName = "TestSource",
        detailUrl = "/detail/$id"
    )
}

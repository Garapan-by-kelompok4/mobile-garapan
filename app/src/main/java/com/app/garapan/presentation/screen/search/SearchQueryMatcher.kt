package com.app.garapan.presentation.screen.search

import com.app.garapan.domain.model.Jasa
import com.app.garapan.domain.model.Project

/** Matches backend: queries shorter than this are not applied as text search. */
const val MIN_SEARCH_QUERY_LENGTH = 3
const val SEARCH_DEBOUNCE_MS = 350L

object SearchQueryMatcher {
    fun isLongEnough(query: String): Boolean =
        query.trim().length >= MIN_SEARCH_QUERY_LENGTH

    fun filterJasa(items: List<Jasa>, query: String): List<Jasa> {
        val normalized = query.trim()
        if (!isLongEnough(normalized)) return items
        return items
            .filter { jasa -> jasa.matchesQuery(normalized) }
            .sortedByDescending { jasa -> jasa.relevanceScore(normalized) }
    }

    fun filterProjects(items: List<Project>, query: String): List<Project> {
        val normalized = query.trim()
        if (!isLongEnough(normalized)) return items
        return items
            .filter { project -> project.matchesQuery(normalized) }
            .sortedByDescending { project -> project.relevanceScore(normalized) }
    }

    private fun Jasa.matchesQuery(query: String): Boolean =
        relevanceScore(query) > 0

    private fun Project.matchesQuery(query: String): Boolean =
        relevanceScore(query) > 0

    private fun Jasa.relevanceScore(query: String): Int {
        val q = query.lowercase()
        return maxOf(
            fieldScore(title, q, primary = true),
            fieldScore(workerName, q, primary = true),
            fieldScore(kategoriName, q, primary = true),
            fieldScore(description, q, primary = false)
        )
    }

    private fun Project.relevanceScore(query: String): Int {
        val q = query.lowercase()
        return maxOf(
            fieldScore(title, q, primary = true),
            fieldScore(clientName, q, primary = true),
            fieldScore(kategoriName, q, primary = true),
            fieldScore(description, q, primary = false)
        )
    }

    private fun fieldScore(value: String, query: String, primary: Boolean): Int {
        if (value.isBlank() || query.isBlank()) return 0
        val normalized = value.lowercase()
        return when {
            normalized == query -> if (primary) 100 else 60
            normalized.startsWith(query) -> if (primary) 80 else 40
            primary && normalized.contains(query) -> 50
            !primary && containsTokenPrefix(normalized, query) -> 30
            !primary && normalized.contains(query) -> 10
            else -> 0
        }
    }

    private fun containsTokenPrefix(text: String, query: String): Boolean =
        text.split(Regex("\\s+"))
            .any { token -> token.startsWith(query) }
}

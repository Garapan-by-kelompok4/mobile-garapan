package com.app.garapan.domain.model

/**
 * One page of a support thread. Page 1 holds the newest [limit] messages
 * (the backend serves newest-first then reverses); higher pages walk
 * backwards into older history. [total] is the full message count, used to
 * decide whether more older pages remain.
 */
data class SupportThreadPage(
    val messages: List<SupportMessage>,
    val total: Int,
    val page: Int,
    val limit: Int
)

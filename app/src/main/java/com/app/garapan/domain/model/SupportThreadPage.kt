package com.app.garapan.domain.model

/**
 * One page of a support thread. Page 1 holds the newest [limit] messages
 * (the backend serves newest-first then reverses); higher pages walk
 * backwards into older history. [total] is the full message count, used to
 * decide whether more older pages remain.
 *
 * The [agentName] / [agentOnline] / [supportOnline] / [unreadCount] fields are
 * thread-level metadata the backend attaches to the user's own thread so the
 * chat header reflects the real support agent and connection state instead of
 * hardcoded values. They are page-independent; only page 1 (the live tail) is
 * used to drive the header.
 */
data class SupportThreadPage(
    val messages: List<SupportMessage>,
    val total: Int,
    val page: Int,
    val limit: Int,
    val agentName: String? = null,
    val agentOnline: Boolean = false,
    val supportOnline: Boolean = false,
    val unreadCount: Int = 0
)

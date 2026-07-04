package com.app.garapan.presentation.screen.blog_detail

object BlogContentFormatter {
    private val blockRegex = Regex(
        pattern = "<(h[1-6]|blockquote|p|ul|ol)\\b[^>]*>(.*?)</\\1>",
        options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
    )
    private val listItemRegex = Regex(
        pattern = "<li\\b[^>]*>(.*?)</li>",
        options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
    )
    private val tagRegex = Regex("<[^>]+>")
    private val blockBoundaryRegex = Regex(
        pattern = "</?(p|h[1-6]|blockquote|li|ul|ol|br)\\b[^>]*>",
        options = setOf(RegexOption.IGNORE_CASE)
    )
    private val whitespaceRegex = Regex("\\s+")

    fun toBlocks(content: String): List<BlogBodyBlock> {
        if (content.isBlank()) return emptyList()
        if (!content.contains('<') || !content.contains('>')) return markdownLikeBlocks(content)

        val blocks = blockRegex.findAll(content).mapNotNull { match ->
            val tag = match.groupValues[1].lowercase()
            val innerHtml = match.groupValues[2]
            val text = toPlainText(innerHtml)
            if (text.isBlank()) return@mapNotNull null

            when {
                tag.startsWith("h") -> BlogBodyBlock.Heading(
                    level = tag.removePrefix("h").toIntOrNull()?.coerceIn(1, 6) ?: 2,
                    text = text
                )
                tag == "blockquote" -> BlogBodyBlock.Quote(text)
                tag == "ul" -> listItems(innerHtml)
                    .takeIf { it.isNotEmpty() }
                    ?.let(BlogBodyBlock::BulletList)
                tag == "ol" -> listItems(innerHtml)
                    .takeIf { it.isNotEmpty() }
                    ?.let(BlogBodyBlock::OrderedList)
                else -> BlogBodyBlock.Paragraph(text)
            }
        }.toList()

        return blocks.ifEmpty {
            toPlainText(content)
                .takeIf { it.isNotBlank() }
                ?.let { listOf(BlogBodyBlock.Paragraph(it)) }
                .orEmpty()
        }
    }

    fun toPlainText(content: String): String =
        content
            .replace(blockBoundaryRegex, " ")
            .replace(tagRegex, "")
            .decodeHtmlEntities()
            .replace(whitespaceRegex, " ")
            .trim()

    fun excerpt(content: String, maxLength: Int): String {
        val plainText = toPlainText(content)
        if (plainText.length <= maxLength) return plainText
        return plainText.take(maxLength).trimEnd()
    }

    private fun markdownLikeBlocks(content: String): List<BlogBodyBlock> =
        content.trim()
            .split(Regex("\n\n+"))
            .mapNotNull { block ->
                val trimmed = block.trim()
                when {
                    trimmed.isBlank() -> null
                    trimmed.startsWith(">") -> BlogBodyBlock.Quote(trimmed.removePrefix(">").trim())
                    trimmed.lines().all { it.trim().matches(Regex("[-*]\\s+.+")) } -> {
                        BlogBodyBlock.BulletList(
                            trimmed.lines().map { it.trim().replace(Regex("^[-*]\\s+"), "") }
                        )
                    }
                    trimmed.lines().all { it.trim().matches(Regex("\\d+[.)]\\s+.+")) } -> {
                        BlogBodyBlock.OrderedList(
                            trimmed.lines().map { it.trim().replace(Regex("^\\d+[.)]\\s+"), "") }
                        )
                    }
                    trimmed.startsWith("# ") -> BlogBodyBlock.Heading(
                        level = 1,
                        text = trimmed.removePrefix("# ").trim()
                    )
                    trimmed.startsWith("## ") -> BlogBodyBlock.Heading(
                        level = 2,
                        text = trimmed.removePrefix("## ").trim()
                    )
                    else -> BlogBodyBlock.Paragraph(trimmed.replace("\n", " "))
                }
            }

    private fun listItems(content: String): List<String> =
        listItemRegex.findAll(content)
            .map { toPlainText(it.groupValues[1]) }
            .filter { it.isNotBlank() }
            .toList()

    private fun String.decodeHtmlEntities(): String =
        replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
}

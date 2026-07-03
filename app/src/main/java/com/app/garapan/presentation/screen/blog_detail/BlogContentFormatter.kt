package com.app.garapan.presentation.screen.blog_detail

object BlogContentFormatter {
    private val blockRegex = Regex(
        pattern = "<(h[1-6]|blockquote|p|li)\\b[^>]*>(.*?)</\\1>",
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

        var headingNumber = 1
        val blocks = blockRegex.findAll(content).mapNotNull { match ->
            val tag = match.groupValues[1].lowercase()
            val text = toPlainText(match.groupValues[2])
            if (text.isBlank()) return@mapNotNull null

            when {
                tag.startsWith("h") -> BlogBodyBlock.Heading(
                    number = headingNumber++,
                    text = text
                )
                tag == "blockquote" -> BlogBodyBlock.Quote(text)
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
                    trimmed.startsWith("## ") -> BlogBodyBlock.Heading(
                        number = 1,
                        text = trimmed.removePrefix("## ").trim()
                    )
                    else -> BlogBodyBlock.Paragraph(trimmed.replace("\n", " "))
                }
            }

    private fun String.decodeHtmlEntities(): String =
        replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
}

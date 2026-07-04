package com.app.garapan.presentation.screen.blog_detail

object BlogContentFormatter {
    private data class InlineText(
        val text: String,
        val styles: List<BlogInlineStyle>
    )

    private data class InlineFlags(
        val bold: Boolean = false,
        val italic: Boolean = false
    )

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
    private val inlineTagRegex = Regex(
        pattern = "<(/?)([a-z][a-z0-9]*)\\b[^>]*>",
        options = setOf(RegexOption.IGNORE_CASE)
    )

    fun toBlocks(content: String): List<BlogBodyBlock> {
        if (content.isBlank()) return emptyList()
        if (!content.contains('<') || !content.contains('>')) return markdownLikeBlocks(content)

        val blocks = blockRegex.findAll(content).mapNotNull { match ->
            val tag = match.groupValues[1].lowercase()
            val innerHtml = match.groupValues[2]
            val inlineText = toInlineText(innerHtml)
            if (inlineText.text.isBlank()) return@mapNotNull null

            when {
                tag.startsWith("h") -> BlogBodyBlock.Heading(
                    level = tag.removePrefix("h").toIntOrNull()?.coerceIn(1, 6) ?: 2,
                    text = inlineText.text,
                    styles = inlineText.styles
                )
                tag == "blockquote" -> BlogBodyBlock.Quote(inlineText.text, inlineText.styles)
                tag == "ul" -> listItems(innerHtml)
                    .takeIf { it.isNotEmpty() }
                    ?.let { items ->
                        BlogBodyBlock.BulletList(
                            items = items.map { it.text },
                            itemStyles = listItemStyles(items)
                        )
                    }
                tag == "ol" -> listItems(innerHtml)
                    .takeIf { it.isNotEmpty() }
                    ?.let { items ->
                        BlogBodyBlock.OrderedList(
                            items = items.map { it.text },
                            itemStyles = listItemStyles(items)
                        )
                    }
                else -> BlogBodyBlock.Paragraph(inlineText.text, inlineText.styles)
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

    private fun listItems(content: String): List<InlineText> =
        listItemRegex.findAll(content)
            .map { toInlineText(it.groupValues[1]) }
            .filter { it.text.isNotBlank() }
            .toList()

    private fun listItemStyles(items: List<InlineText>): List<List<BlogInlineStyle>> {
        val styles = items.map { it.styles }
        return styles.takeUnless { itemStyles -> itemStyles.all { it.isEmpty() } }.orEmpty()
    }

    private fun toInlineText(content: String): InlineText {
        val rawText = StringBuilder()
        val rawFlags = mutableListOf<InlineFlags>()
        var boldDepth = 0
        var italicDepth = 0
        var lastIndex = 0

        fun appendText(text: String) {
            val decoded = text.decodeHtmlEntities()
            val flags = InlineFlags(bold = boldDepth > 0, italic = italicDepth > 0)
            rawText.append(decoded)
            repeat(decoded.length) {
                rawFlags += flags
            }
        }

        inlineTagRegex.findAll(content).forEach { match ->
            appendText(content.substring(lastIndex, match.range.first))
            val isClosing = match.groupValues[1] == "/"
            when (match.groupValues[2].lowercase()) {
                "strong", "b" -> if (isClosing) boldDepth = (boldDepth - 1).coerceAtLeast(0) else boldDepth += 1
                "em", "i" -> if (isClosing) italicDepth = (italicDepth - 1).coerceAtLeast(0) else italicDepth += 1
            }
            lastIndex = match.range.last + 1
        }
        appendText(content.substring(lastIndex))

        return normalizeInlineText(rawText.toString(), rawFlags)
    }

    private fun normalizeInlineText(text: String, flags: List<InlineFlags>): InlineText {
        val normalizedText = StringBuilder()
        val normalizedFlags = mutableListOf<InlineFlags>()
        var lastWasWhitespace = true

        text.forEachIndexed { index, char ->
            val charFlags = flags.getOrElse(index) { InlineFlags() }
            if (char.isWhitespace()) {
                if (!lastWasWhitespace) {
                    normalizedText.append(' ')
                    normalizedFlags += charFlags
                    lastWasWhitespace = true
                }
            } else {
                normalizedText.append(char)
                normalizedFlags += charFlags
                lastWasWhitespace = false
            }
        }

        if (normalizedText.isNotEmpty() && normalizedText.last().isWhitespace()) {
            normalizedText.deleteAt(normalizedText.lastIndex)
            normalizedFlags.removeAt(normalizedFlags.lastIndex)
        }

        return InlineText(
            text = normalizedText.toString(),
            styles = normalizedFlags.toInlineStyles()
        )
    }

    private fun List<InlineFlags>.toInlineStyles(): List<BlogInlineStyle> {
        val styles = mutableListOf<BlogInlineStyle>()
        var rangeStart: Int? = null
        var currentFlags = InlineFlags()

        fun closeRange(end: Int) {
            val start = rangeStart ?: return
            if (start < end) {
                styles += BlogInlineStyle(
                    start = start,
                    end = end,
                    bold = currentFlags.bold,
                    italic = currentFlags.italic
                )
            }
            rangeStart = null
        }

        forEachIndexed { index, flags ->
            val hasStyle = flags.bold || flags.italic
            when {
                !hasStyle -> closeRange(index)
                rangeStart == null -> {
                    rangeStart = index
                    currentFlags = flags
                }
                flags != currentFlags -> {
                    closeRange(index)
                    rangeStart = index
                    currentFlags = flags
                }
            }
        }
        closeRange(size)

        return styles
    }

    private fun String.decodeHtmlEntities(): String =
        replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
}

package com.app.garapan.presentation.screen.blog_detail

import org.junit.Assert.assertEquals
import org.junit.Test

class BlogContentFormatterTest {

    @Test
    fun `toPlainText removes html tags and preserves readable spacing`() {
        val html = "<h1><strong>kus<em>ssss</em></strong></h1><h1><strong>adadadadassda</strong></h1><p></p>"

        assertEquals(
            "kusssss adadadadassda",
            BlogContentFormatter.toPlainText(html)
        )
    }

    @Test
    fun `toBlocks maps tiptap headings paragraphs blockquotes and lists`() {
        val html = """
            <h1>Judul Utama</h1>
            <h2>Judul Bagian</h2>
            <p>Paragraf <strong>penting</strong>.</p>
            <blockquote><p>Kutipan editor</p></blockquote>
            <ul><li>Poin pertama</li><li>Poin kedua</li></ul>
            <ol><li>Langkah pertama</li><li>Langkah kedua</li></ol>
        """.trimIndent()

        assertEquals(
            listOf(
                BlogBodyBlock.Heading(level = 1, text = "Judul Utama"),
                BlogBodyBlock.Heading(level = 2, text = "Judul Bagian"),
                BlogBodyBlock.Paragraph(
                    text = "Paragraf penting.",
                    styles = listOf(BlogInlineStyle(start = 9, end = 16, bold = true))
                ),
                BlogBodyBlock.Quote("Kutipan editor"),
                BlogBodyBlock.BulletList(listOf("Poin pertama", "Poin kedua")),
                BlogBodyBlock.OrderedList(listOf("Langkah pertama", "Langkah kedua"))
            ),
            BlogContentFormatter.toBlocks(html)
        )
    }

    @Test
    fun `toBlocks preserves bold and italic inline marks`() {
        val html = """
            <h2>Judul <em>miring</em></h2>
            <p>Ini <strong>tebal</strong> dan <em>miring</em>.</p>
            <blockquote><p>Kutipan <strong><em>penting</em></strong></p></blockquote>
            <ul><li>Poin <strong>satu</strong></li></ul>
        """.trimIndent()

        val blocks = BlogContentFormatter.toBlocks(html)

        assertEquals(
            listOf(BlogInlineStyle(start = 6, end = 12, italic = true)),
            (blocks[0] as BlogBodyBlock.Heading).styles
        )
        assertEquals(
            listOf(
                BlogInlineStyle(start = 4, end = 9, bold = true),
                BlogInlineStyle(start = 14, end = 20, italic = true)
            ),
            (blocks[1] as BlogBodyBlock.Paragraph).styles
        )
        assertEquals(
            listOf(BlogInlineStyle(start = 8, end = 15, bold = true, italic = true)),
            (blocks[2] as BlogBodyBlock.Quote).styles
        )
        assertEquals(
            listOf(listOf(BlogInlineStyle(start = 5, end = 9, bold = true))),
            (blocks[3] as BlogBodyBlock.BulletList).itemStyles
        )
    }

    @Test
    fun `excerpt is based on plain text instead of raw html`() {
        assertEquals("kata kata kata kata", BlogContentFormatter.excerpt("<p>kata kata kata kata</p>", maxLength = 100))
    }
}

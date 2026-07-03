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
            <h2>Judul Bagian</h2>
            <p>Paragraf <strong>penting</strong>.</p>
            <blockquote><p>Kutipan editor</p></blockquote>
            <ul><li>Poin pertama</li><li>Poin kedua</li></ul>
        """.trimIndent()

        assertEquals(
            listOf(
                BlogBodyBlock.Heading(number = 1, text = "Judul Bagian"),
                BlogBodyBlock.Paragraph("Paragraf penting."),
                BlogBodyBlock.Quote("Kutipan editor"),
                BlogBodyBlock.Paragraph("Poin pertama"),
                BlogBodyBlock.Paragraph("Poin kedua")
            ),
            BlogContentFormatter.toBlocks(html)
        )
    }

    @Test
    fun `excerpt is based on plain text instead of raw html`() {
        assertEquals("kata kata kata kata", BlogContentFormatter.excerpt("<p>kata kata kata kata</p>", maxLength = 100))
    }
}

package com.app.garapan.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.garapan.ui.theme.AccentBlue
import com.app.garapan.ui.theme.BrandNavy
import com.app.garapan.ui.theme.SecondaryText

@Composable
fun PesananStatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bg, textColor) = statusBadgeColors(status)
    Text(
        text = status,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            color = textColor
        ),
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    )
}

private fun statusBadgeColors(status: String): Pair<Color, Color> = when (status) {
    "MENUNGGU BAYAR" -> Color(0xFFFFF3D6) to Color(0xFFB45309)
    "DIPROSES" -> Color(0xFFE8EDFF) to AccentBlue
    "DIKIRIM" -> Color(0xFFE6F7F1) to Color(0xFF047857)
    "SELESAI" -> Color(0xFFE3E8F8) to BrandNavy
    "DISPUTE", "DIBATALKAN" -> Color(0xFFFFE1E1) to Color(0xFFE31B23)
    else -> Color(0xFFE6E6EE) to SecondaryText
}

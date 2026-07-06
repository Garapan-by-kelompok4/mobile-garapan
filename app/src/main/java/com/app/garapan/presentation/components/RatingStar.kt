package com.app.garapan.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.app.garapan.ui.theme.LightGray
import com.app.garapan.ui.theme.StarYellow
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Star

private val FilledStar: ImageVector by lazy {
    ImageVector.Builder(
        name = "filled_star",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 2f)
            lineTo(15.09f, 8.26f)
            lineTo(22f, 9.27f)
            lineTo(17f, 14.14f)
            lineTo(18.18f, 21.02f)
            lineTo(12f, 17.77f)
            lineTo(5.82f, 21.02f)
            lineTo(7f, 14.14f)
            lineTo(2f, 9.27f)
            lineTo(8.91f, 8.26f)
            lineTo(12f, 2f)
            close()
        }
    }.build()
}

@Composable
fun RatingStar(
    filled: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    filledColor: Color = StarYellow,
    outlineColor: Color = LightGray,
    contentDescription: String? = null
) {
    Icon(
        imageVector = if (filled) FilledStar else Lucide.Star,
        contentDescription = contentDescription,
        tint = if (filled) filledColor else outlineColor,
        modifier = modifier.size(size)
    )
}

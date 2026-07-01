package com.app.garapan.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.app.garapan.ui.theme.BorderColor
import com.app.garapan.ui.theme.White

val AppCardShape: Shape = RoundedCornerShape(12.dp)
private val CardShadowColor = Color.Black.copy(alpha = 0.06f)

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    shape: Shape = AppCardShape,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = shape,
                ambientColor = CardShadowColor,
                spotColor = CardShadowColor
            )
            .clip(shape)
            .background(White)
            .border(BorderStroke(1.dp, BorderColor), shape)
    ) {
        content()
    }
}

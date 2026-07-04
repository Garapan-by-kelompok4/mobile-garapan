package com.app.garapan.presentation.screen.blog_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.garapan.presentation.components.AppCard
import com.app.garapan.presentation.components.AppTopBar
import com.app.garapan.presentation.navigation.Routes
import com.app.garapan.ui.theme.BrandNavy
import com.app.garapan.ui.theme.AccentBlue
import com.app.garapan.ui.theme.BorderColor
import com.app.garapan.ui.theme.PrimaryText
import com.app.garapan.ui.theme.SecondaryText
import com.app.garapan.ui.theme.Surface
import com.app.garapan.ui.theme.White
import coil3.compose.AsyncImage

@Composable
fun BlogDetailScreen(
    navController: NavController,
    viewModel: BlogDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().background(White)) {
        BlogDetailTopBar(onBack = { navController.navigateUp() })

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandNavy)
                }
            }
            uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = viewModel::retry,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandNavy)
                    ) {
                        Text(text = "Coba Lagi")
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    BlogHeroSection(uiState = uiState)

                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Spacer(modifier = Modifier.height(20.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AccentBlue.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = uiState.category,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AccentBlue
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "·",
                                style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = uiState.date,
                                style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = uiState.title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryText
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = uiState.heroSubtitle,
                            style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        AuthorRow(uiState = uiState)

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = BorderColor)
                        Spacer(modifier = Modifier.height(20.dp))

                        uiState.body.forEach { block ->
                            BlogBlock(block = block)
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = "Rekomendasi Artikel",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryText
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        uiState.recommendations.forEachIndexed { index, item ->
                            RecommendationCard(
                                item = item,
                                onClick = {
                                    navController.navigate(Routes.blogDetailRoute(item.id))
                                }
                            )
                            if (index < uiState.recommendations.lastIndex) {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Spacer(modifier = Modifier.navigationBarsPadding())
                    }
                }
            }
        }
    }
}

@Composable
private fun BlogDetailTopBar(onBack: () -> Unit) {
    AppTopBar(
        title = BlogArticleDefaults.CATEGORY,
        onBack = onBack
    )
}

@Composable
private fun BlogHeroSection(uiState: BlogDetailUiState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        val imageUrl = uiState.imageUrl
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = uiState.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(BrandNavy, AccentBlue)
                        )
                    )
            )
        }

        // Bottom gradient fade for text legibility
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            if (!imageUrl.isNullOrBlank()) Color.Black.copy(alpha = 0.65f) else BrandNavy.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // Text overlaid at bottom-left
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(White.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = uiState.category,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = White
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uiState.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = White
                ),
                maxLines = 2
            )
        }
    }
}

@Composable
private fun AuthorRow(uiState: BlogDetailUiState) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(AccentBlue),
            contentAlignment = Alignment.Center
        ) {
            val avatarUrl = uiState.authorAvatarUrl
            if (!avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = uiState.authorName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = authorInitials(uiState.authorName),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = White
                    )
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = uiState.authorName,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText
                )
            )
            Text(
                text = uiState.authorRole,
                style = MaterialTheme.typography.labelSmall.copy(color = SecondaryText)
            )
        }
    }
}

private fun authorInitials(name: String): String {
    val parts = name.split(Regex("\\s+")).filter { it.isNotBlank() }
    return parts
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
        .joinToString("")
        .ifBlank { BlogArticleDefaults.AUTHOR_INITIALS }
}

@Composable
private fun BlogBlock(block: BlogBodyBlock) {
    when (block) {
        is BlogBodyBlock.Paragraph -> {
            Text(
                text = block.text,
                style = MaterialTheme.typography.bodyMedium.copy(color = PrimaryText)
            )
            Spacer(modifier = Modifier.height(14.dp))
        }
        is BlogBodyBlock.Heading -> {
            Text(
                text = block.text,
                style = when (block.level) {
                    1 -> MaterialTheme.typography.headlineSmall
                    2 -> MaterialTheme.typography.titleLarge
                    3 -> MaterialTheme.typography.titleMedium
                    else -> MaterialTheme.typography.titleSmall
                }.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandNavy
                )
            )
            Spacer(modifier = Modifier.height(if (block.level <= 2) 16.dp else 12.dp))
        }
        is BlogBodyBlock.Quote -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                    .background(Surface)
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(60.dp)
                        .background(AccentBlue)
                )
                Text(
                    text = block.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = SecondaryText,
                        fontStyle = FontStyle.Italic
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 12.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }
        is BlogBodyBlock.BulletList -> {
            BlogList(items = block.items, ordered = false)
        }
        is BlogBodyBlock.OrderedList -> {
            BlogList(items = block.items, ordered = true)
        }
    }
}

@Composable
private fun BlogList(
    items: List<String>,
    ordered: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = if (ordered) "${index + 1}." else "•",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = BrandNavy
                    ),
                    modifier = Modifier.width(28.dp)
                )
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium.copy(color = PrimaryText),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(14.dp))
}

@Composable
private fun RecommendationCard(
    item: RecommendationItem,
    onClick: () -> Unit
) {
    AppCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(BrandNavy.copy(alpha = 0.15f), AccentBlue.copy(alpha = 0.08f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                val imageUrl = item.imageUrl
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentBlue
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    ),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.excerpt,
                    style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText),
                    maxLines = 2
                )
            }
        }
    }
}

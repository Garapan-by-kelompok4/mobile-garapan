package com.app.garapan.presentation.screen.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Trash2
import com.composables.icons.lucide.Pencil
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.app.garapan.presentation.components.AppCard
import com.app.garapan.presentation.navigation.NavResults
import com.app.garapan.presentation.navigation.Routes
import com.app.garapan.ui.theme.AccentBlue
import com.app.garapan.ui.theme.BrandNavy
import com.app.garapan.ui.theme.ErrorRed
import com.app.garapan.ui.theme.MutedText
import com.app.garapan.ui.theme.PrimaryText
import com.app.garapan.ui.theme.SecondaryText
import com.app.garapan.ui.theme.Surface
import com.app.garapan.ui.theme.White

@Composable
fun PortfolioScreen(
    navController: NavController,
    viewModel: PortfolioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val backStackEntry = navController.currentBackStackEntry

    LaunchedEffect(backStackEntry) {
        val savedStateHandle = backStackEntry?.savedStateHandle ?: return@LaunchedEffect
        savedStateHandle.getStateFlow(NavResults.PORTFOLIO_REFRESH, false)
            .collect { shouldRefresh ->
                if (shouldRefresh) {
                    viewModel.loadPortofolio()
                    savedStateHandle[NavResults.PORTFOLIO_REFRESH] = false
                }
            }
    }

    Scaffold(containerColor = Surface) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 18.dp, top = 8.dp, end = 18.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                PortfolioTopBar(onBack = { navController.navigateUp() })
                Spacer(modifier = Modifier.height(24.dp))
                PortfolioHero(onAddClick = { navController.navigate(Routes.ADD_PORTFOLIO) })
            }

            if (uiState.isLoading) {
                item {
                    Text(
                        text = "Memuat portofolio...",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MutedText)
                    )
                }
            }

            uiState.errorMessage?.let { message ->
                item {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium.copy(color = ErrorRed)
                    )
                }
            }

            items(uiState.items, key = { it.id }) { item ->
                PortfolioCard(
                    item = item,
                    onEditClick = {
                        navController.navigate(Routes.editPortfolioRoute(item.id))
                    },
                    onDeleteClick = { viewModel.onDeletePortfolio(item.id) }
                )
            }
        }
    }
}

@Composable
private fun PortfolioTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(34.dp)) {
            Icon(
                imageVector = Lucide.ArrowLeft,
                contentDescription = "Back",
                tint = AccentBlue,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = "Manajemen Portofolio",
            style = MaterialTheme.typography.labelMedium.copy(
                color = AccentBlue,
                fontWeight = FontWeight.ExtraBold
            )
        )
    }
}

@Composable
private fun PortfolioHero(onAddClick: () -> Unit) {
    Column {
        Text(
            text = "Portofolio Anda",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = PrimaryText,
                fontWeight = FontWeight.ExtraBold
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Tampilkan portofolio anda kepada publik",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = SecondaryText,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .clickable(onClick = onAddClick)
                .background(BrandNavy)
                .padding(horizontal = 18.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Lucide.Plus,
                contentDescription = null,
                tint = White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Tambah Portofolio Baru",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = White,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }
    }
}

@Composable
private fun PortfolioCard(
    item: PortfolioItem,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            PortfolioCover(item = item)
            Column(
                modifier = Modifier.padding(start = 16.dp, top = 14.dp, end = 12.dp, bottom = 14.dp)
            ) {
                PortfolioTags(tags = item.tags)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = PrimaryText,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SecondaryText,
                        lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CardActionIcon(
                        icon = Lucide.Pencil,
                        tint = AccentBlue,
                        contentDescription = "Edit portofolio",
                        onClick = onEditClick
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    CardActionIcon(
                        icon = Lucide.Trash2,
                        tint = ErrorRed,
                        contentDescription = "Hapus portofolio",
                        onClick = onDeleteClick
                    )
                }
            }
        }
    }
}

@Composable
private fun PortfolioCover(item: PortfolioItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .background(item.coverColor),
        contentAlignment = Alignment.Center
    ) {
        if (item.imageUrl.isNotBlank()) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            PortfolioMockupContent(item)
        }
    }
}

@Composable
private fun PortfolioMockupContent(item: PortfolioItem) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.76f)
                .height(112.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(White)
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = item.mockupTitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = PrimaryText,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.mockupSubtitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = PrimaryText,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.42f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(item.accentColor)
                )
            }
        }
    }
}

@Composable
private fun PortfolioTags(tags: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        tags.forEach { tag ->
            Text(
                text = tag,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = SecondaryText,
                    fontWeight = FontWeight.ExtraBold
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Surface)
                    .padding(horizontal = 9.dp, vertical = 5.dp)
            )
        }
    }
}

@Composable
private fun CardActionIcon(
    icon: ImageVector,
    tint: Color,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(30.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
    }
}

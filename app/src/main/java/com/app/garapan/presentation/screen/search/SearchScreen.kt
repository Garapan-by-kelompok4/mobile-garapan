package com.app.garapan.presentation.screen.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.garapan.presentation.navigation.Routes
import com.app.garapan.ui.theme.AccentBlue
import com.app.garapan.ui.theme.BorderColor
import com.app.garapan.ui.theme.BrandNavy
import com.app.garapan.ui.theme.LightGray
import com.app.garapan.ui.theme.MutedText
import com.app.garapan.ui.theme.PrimaryText
import com.app.garapan.ui.theme.SecondaryText
import com.app.garapan.ui.theme.StarYellow
import com.app.garapan.ui.theme.Surface
import com.app.garapan.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (uiState.showFilterSheet) {
        FilterSortBottomSheet(
            state = uiState.filter,
            sheetState = sheetState,
            onDismiss = viewModel::onDismissFilter,
            onTypeSelected = viewModel::onFilterTypeSelected,
            onCategorySelected = viewModel::onCategorySelected,
            onMinPriceChanged = viewModel::onMinPriceChanged,
            onMaxPriceChanged = viewModel::onMaxPriceChanged,
            onSortSelected = viewModel::onSortSelected,
            onApply = viewModel::onApplyFilter
        )
    }

    Scaffold(
        bottomBar = {
            SearchBottomNav(navController = navController)
        },
        containerColor = White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SearchTopBar(onBack = { navController.navigateUp() })

            Spacer(modifier = Modifier.height(12.dp))

            SearchBar(
                query = uiState.query,
                onQueryChanged = viewModel::onQueryChanged,
                onFilterClick = viewModel::onShowFilter,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.showResults) {
                SearchResultsList(
                    results = uiState.results,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    SearchEmptyState()
                }
            }
        }
    }
}

@Composable
private fun SearchTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = PrimaryText
            )
        }
        Text(
            text = "Cari Layanan",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            ),
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = PrimaryText
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = MutedText,
            modifier = Modifier.size(20.dp)
        )
        BasicTextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = PrimaryText),
            singleLine = true,
            cursorBrush = SolidColor(BrandNavy),
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text(
                        text = "Cari Jasa / Proyek..",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MutedText)
                    )
                }
                inner()
            }
        )
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(White)
                .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                .clickable { onFilterClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Filter",
                tint = PrimaryText,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SearchResultsList(
    results: List<SearchResultItem>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 16.dp,
            vertical = 8.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "${results.size} Layanan Ditemukan",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        items(results) { item ->
            SearchResultCard(item = item)
        }
    }
}

@Composable
private fun SearchResultCard(item: SearchResultItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LightGray)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryText
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(BrandNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.workerName.first().toString(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.workerName,
                        style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = StarYellow,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${item.rating}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PrimaryText,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = "  (${item.reviewCount})",
                        style = MaterialTheme.typography.bodySmall.copy(color = MutedText)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.price,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AccentBlue
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MutedText,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = item.duration,
                        style = MaterialTheme.typography.bodySmall.copy(color = MutedText)
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchEmptyState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = 40.dp)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(AccentBlue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(BrandNavy),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Mau cari apa hari ini?",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Temukan jasa atau tawaran proyek terbaru di sini. Hubungkan keahlianmu sekarang!",
            style = MaterialTheme.typography.bodySmall.copy(
                color = SecondaryText,
                lineHeight = MaterialTheme.typography.bodySmall.lineHeight
            ),
            textAlign = TextAlign.Center
        )
    }
}

private data class NavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
private fun SearchBottomNav(navController: NavController) {
    val navItems = listOf(
        NavItem("Home", Icons.Filled.Home, Icons.Outlined.Home),
        NavItem("Search", Icons.Filled.Search, Icons.Outlined.Search),
        NavItem("New", Icons.Default.Add, Icons.Default.Add),
        NavItem("Pesan", Icons.Outlined.ChatBubbleOutline, Icons.Outlined.ChatBubbleOutline),
        NavItem("Profile", Icons.Filled.Person, Icons.Outlined.Person),
    )
    val selectedIndex = 1

    NavigationBar(
        containerColor = White,
        tonalElevation = 0.dp,
        modifier = Modifier.border(width = 1.dp, color = BorderColor, shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
    ) {
        navItems.forEachIndexed { index, item ->
            if (index == 2) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(BrandNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = item.label,
                            tint = White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            } else {
                NavigationBarItem(
                    selected = selectedIndex == index,
                    onClick = {
                        when (index) {
                            0 -> navController.navigate(Routes.HOME) {
                                popUpTo(Routes.HOME) { inclusive = true }
                            }
                            4 -> navController.navigate(Routes.PROFILE)
                            else -> {}
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (selectedIndex == index) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandNavy,
                        selectedTextColor = BrandNavy,
                        unselectedIconColor = MutedText,
                        unselectedTextColor = MutedText,
                        indicatorColor = BrandNavy.copy(alpha = 0.1f)
                    )
                )
            }
        }
    }
}

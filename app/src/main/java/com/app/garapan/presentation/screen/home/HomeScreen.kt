package com.app.garapan.presentation.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil3.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.garapan.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.garapan.presentation.components.NotificationBellButton
import com.app.garapan.presentation.navigation.NavResults
import com.app.garapan.presentation.navigation.Routes
import com.app.garapan.presentation.util.RatingFormatter
import com.app.garapan.ui.theme.AccentBlue
import com.app.garapan.ui.theme.BorderColor
import com.app.garapan.ui.theme.BrandNavy
import com.app.garapan.ui.theme.LightGray
import com.app.garapan.ui.theme.MutedText
import com.app.garapan.ui.theme.OnPrimary
import com.app.garapan.ui.theme.PrimaryText
import com.app.garapan.ui.theme.SecondaryText
import com.app.garapan.ui.theme.StarYellow
import com.app.garapan.ui.theme.Surface
import com.app.garapan.ui.theme.White

@Composable
fun HomeScreen(
    navController: NavController,
    onNavigateTab: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val refreshStateHandle = remember(navController) {
        runCatching { navController.getBackStackEntry(Routes.MAIN).savedStateHandle }.getOrNull()
    }

    LaunchedEffect(refreshStateHandle) {
        val handle = refreshStateHandle ?: return@LaunchedEffect
        handle.getStateFlow(NavResults.PROJECT_REFRESH, false).collect { shouldRefresh ->
            if (!shouldRefresh) return@collect
            NavResults.clearProjectRefresh(handle)
            viewModel.refreshProjects()
            viewModel.refreshServices()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshProjects()
                viewModel.refreshServices()
                viewModel.refreshNotificationBadge()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        containerColor = Surface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            HomeTopBar(
                unreadNotificationCount = uiState.unreadNotificationCount,
                onSearchClick = { onNavigateTab(Routes.searchRoute()) },
                onNotificationsClick = { navController.navigate(Routes.NOTIFICATIONS) }
            )
            Spacer(modifier = Modifier.height(16.dp))

            HeroBanner(onGetStarted = { onNavigateTab(Routes.searchRoute()) })
            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(
                title = "Proyek Tersedia",
                onSeeAll = { onNavigateTab(Routes.searchRoute()) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            when {
                uiState.isProjectsLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BrandNavy)
                    }
                }
                uiState.projectsError != null -> {
                    SectionErrorMessage(
                        message = uiState.projectsError.orEmpty(),
                        onRetry = viewModel::retryProjects,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                uiState.projects.isEmpty() -> {
                    Text(
                        text = "Belum ada proyek tersedia.",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                    )
                }
                else -> {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.projects, key = { it.id }) { project ->
                            ProjectCard(
                                project = project,
                                onClick = { navController.navigate(Routes.projectDetailRoute(project.id)) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(
                title = "Layanan Jasa",
                onSeeAll = { onNavigateTab(Routes.searchRoute(Routes.SEARCH_FOCUS_JASA)) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            when {
                uiState.isServicesLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BrandNavy)
                    }
                }
                uiState.servicesError != null -> {
                    SectionErrorMessage(
                        message = uiState.servicesError.orEmpty(),
                        onRetry = viewModel::retryServices,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                else -> {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.services, key = { it.id }) { service ->
                            ServiceCard(
                                service = service,
                                onClick = { navController.navigate(Routes.jasaDetailRoute(service.id)) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(
                title = "Top Workers",
                onSeeAll = { navController.navigate(Routes.TOP_WORKERS) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when {
                    uiState.isTopWorkersLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = BrandNavy)
                        }
                    }
                    uiState.topWorkersError != null -> {
                        SectionErrorMessage(
                            message = uiState.topWorkersError.orEmpty(),
                            onRetry = viewModel::retryTopWorkers
                        )
                    }
                    else -> {
                        uiState.topWorkers.forEach { worker ->
                            TopWorkerCard(
                                worker = worker,
                                onClick = {
                                    navController.navigate(Routes.publicProfileRoute(worker.userId))
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(
                title = "Blog",
                onSeeAll = { navController.navigate(Routes.ARTICLE_LIST) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when {
                    uiState.isBlogsLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = BrandNavy)
                        }
                    }
                    uiState.blogsError != null -> {
                        SectionErrorMessage(
                            message = uiState.blogsError.orEmpty(),
                            onRetry = viewModel::retryBlogs
                        )
                    }
                    else -> {
                        uiState.blogs.forEach { blog ->
                            BlogCard(
                                blog = blog,
                                onClick = { navController.navigate(Routes.blogDetailRoute(blog.id)) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HomeTopBar(
    unreadNotificationCount: Int,
    onSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.logo_garapan),
            contentDescription = "Garapan Logo",
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "GARAPAN",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = BrandNavy,
                letterSpacing = 1.sp
            ),
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onSearchClick) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = PrimaryText
            )
        }
        NotificationBellButton(
            unreadCount = unreadNotificationCount,
            onClick = onNotificationsClick
        )
    }
}

@Composable
private fun HeroBanner(onGetStarted: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(BrandNavy, AccentBlue),
                    start = Offset(0f, 0f),
                    end = Offset.Infinite
                )
            )
    ) {
        // Decorative circles add depth and atmosphere instead of a flat block.
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 48.dp, y = (-48).dp)
                .size(150.dp)
                .clip(CircleShape)
                .background(White.copy(alpha = 0.08f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 28.dp, y = 36.dp)
                .size(104.dp)
                .clip(CircleShape)
                .background(White.copy(alpha = 0.06f))
        )
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Temukan Freelancer\nIT Terbaik",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = White,
                    lineHeight = 32.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Hubungkan proyek Anda dengan talenta\nmahasiswa terbaik Indonesia",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = White.copy(alpha = 0.8f),
                    lineHeight = 20.sp
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onGetStarted,
                colors = ButtonDefaults.buttonColors(
                    containerColor = White,
                    contentColor = BrandNavy
                ),
                shape = RoundedCornerShape(50.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Mulai Sekarang",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            ),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Lihat Semua",
            modifier = Modifier.clickable(onClick = onSeeAll),
            style = MaterialTheme.typography.bodySmall.copy(
                color = AccentBlue,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun ProjectCard(project: ProjectItem, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.width(280.dp),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (project.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = project.imageUrl,
                        contentDescription = project.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Subtle scrim so the image always reads cleanly regardless of user content.
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.22f)
                                    )
                                )
                            )
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        AccentBlue.copy(alpha = 0.10f),
                                        BrandNavy.copy(alpha = 0.06f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ImageNotSupported,
                            contentDescription = null,
                            tint = MutedText,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    ),
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MutedText,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = project.deadline,
                        style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Paid,
                        contentDescription = null,
                        tint = MutedText,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = project.budget,
                        style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = project.category,
                        style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = project.clientName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryText
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ServiceCard(service: ServiceItem, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.width(180.dp),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (service.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = service.imageUrl,
                        contentDescription = service.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = service.category,
                        style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = service.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryText
                    ),
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = service.workerName,
                    style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = service.price,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AccentBlue
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = StarYellow,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = RatingFormatter.format(service.rating),
                        style = MaterialTheme.typography.labelSmall.copy(color = SecondaryText)
                    )
                }
            }
        }
    }
}

@Composable
fun TopWorkerCard(worker: TopWorkerItem, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(BrandNavy),
                contentAlignment = Alignment.Center
            ) {
                val avatarUrl = worker.avatarUrl
                if (!avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = worker.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = worker.name.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = worker.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryText
                    )
                )
                Text(
                    text = worker.skill,
                    style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = StarYellow,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = RatingFormatter.format(worker.rating),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryText
                        )
                    )
                }
                Text(
                    text = "${worker.projectsDone} proyek",
                    style = MaterialTheme.typography.labelSmall.copy(color = MutedText)
                )
            }
        }
    }
}

@Composable
fun BlogCard(blog: BlogItem, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LightGray)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(AccentBlue.copy(alpha = 0.1f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = blog.category,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = AccentBlue,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = blog.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryText
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${blog.readTime} baca",
                    style = MaterialTheme.typography.labelSmall.copy(color = MutedText)
                )
            }
        }
    }
}

@Composable
private fun SectionErrorMessage(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = BrandNavy)
        ) {
            Text(text = "Coba Lagi")
        }
    }
}

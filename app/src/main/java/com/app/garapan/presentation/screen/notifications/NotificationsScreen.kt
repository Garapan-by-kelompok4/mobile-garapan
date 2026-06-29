package com.app.garapan.presentation.screen.notifications

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.garapan.presentation.navigation.Routes
import com.app.garapan.ui.theme.AccentBlue
import com.app.garapan.ui.theme.BrandNavy
import com.app.garapan.ui.theme.PrimaryText
import com.app.garapan.ui.theme.SecondaryText
import com.app.garapan.ui.theme.Surface
import com.app.garapan.ui.theme.White

@Composable
fun NotificationsScreen(
    navController: NavController,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is NotificationsEvent.NavigateToOrderDetail -> {
                    navController.navigate(Routes.orderDetailRoute(event.pesananId))
                }
                is NotificationsEvent.NavigateToAllReviews -> {
                    navController.navigate(Routes.allReviewsRoute(event.jasaId))
                }
                is NotificationsEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                NotificationsEvent.NavigateToOrderHistory -> {
                    navController.navigate(Routes.ORDER_HISTORY)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Surface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Surface)
        ) {
            NotificationsTopBar(
                unreadCount = uiState.unreadCount,
                isMarkingAllRead = uiState.isMarkingAllRead,
                onBack = { navController.navigateUp() },
                onMarkAllRead = viewModel::markAllRead
            )

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
                        Spacer(modifier = Modifier.padding(8.dp))
                        Button(
                            onClick = viewModel::retry,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandNavy)
                        ) {
                            Text("Coba Lagi")
                        }
                    }
                }
                uiState.notifications.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada notifikasi.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText)
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.notifications, key = { it.id }) { notification ->
                            NotificationCard(
                                notification = notification,
                                onClick = { viewModel.onNotificationClick(notification) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationsTopBar(
    unreadCount: Int,
    isMarkingAllRead: Boolean,
    onBack: () -> Unit,
    onMarkAllRead: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .statusBarsPadding()
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
            text = "Notifikasi",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            ),
            modifier = Modifier.weight(1f)
        )
        if (unreadCount > 0) {
            TextButton(
                onClick = onMarkAllRead,
                enabled = !isMarkingAllRead
            ) {
                if (isMarkingAllRead) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = BrandNavy,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Tandai dibaca",
                        style = MaterialTheme.typography.labelLarge.copy(color = BrandNavy)
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.read) White else AccentBlue.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (!notification.read) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(BrandNavy)
                )
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = if (notification.read) FontWeight.Medium else FontWeight.Bold,
                            color = PrimaryText
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = notification.timeLabel,
                        style = MaterialTheme.typography.labelSmall.copy(color = SecondaryText)
                    )
                }
                Spacer(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

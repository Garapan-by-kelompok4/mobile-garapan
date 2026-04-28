package com.app.garapan.presentation.screen.order_history

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.garapan.ui.theme.AccentBlue
import com.app.garapan.ui.theme.BrandNavy
import com.app.garapan.ui.theme.PrimaryText
import com.app.garapan.ui.theme.SecondaryText
import com.app.garapan.ui.theme.White

@Composable
fun OrderHistoryScreen(
    navController: NavController,
    viewModel: OrderHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(containerColor = Color(0xFFFAF8FF)) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OrderHistoryTopBar(onBack = { navController.navigateUp() })
            MonthChip()
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(uiState.items) { item ->
                    OrderHistoryCard(item = item)
                }
            }
        }
    }
}

@Composable
private fun OrderHistoryTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = AccentBlue
            )
        }
        Text(
            text = "Riwayat Pesanan",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = AccentBlue
            ),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Filled.FilterList,
                contentDescription = "Filter",
                tint = AccentBlue
            )
        }
    }
}

@Composable
private fun MonthChip() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "BULAN INI",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = SecondaryText
            ),
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(Color(0xFFF0EEF8))
                .padding(horizontal = 18.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun OrderHistoryCard(item: OrderHistoryItem) {
    val amountColor = if (item.isIncome) BrandNavy else Color(0xFFE31B23)
    val iconColor = if (item.isIncome) AccentBlue else Color(0xFFE31B23)
    val iconBg = if (item.isIncome) Color(0xFFE7EEFF) else Color(0xFFFFE7E8)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(White)
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (item.isIncome) Icons.Filled.SouthWest else Icons.Filled.NorthEast,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    ),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = item.amount,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = amountColor
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${item.workerName}  •  ${item.time}",
                style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
            )
            Spacer(modifier = Modifier.height(8.dp))
            StatusBadge(status = item.status)
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (bg, textColor) = when (status) {
        "SELESAI" -> Color(0xFFE3E8F8) to BrandNavy
        "DIBATALKAN" -> Color(0xFFFFE1E1) to Color(0xFFE31B23)
        else -> Color(0xFFE6E6EE) to SecondaryText
    }

    Text(
        text = status,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            color = textColor
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    )
}

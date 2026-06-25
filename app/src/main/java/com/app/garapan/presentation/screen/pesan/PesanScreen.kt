package com.app.garapan.presentation.screen.pesan

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.garapan.R
import com.app.garapan.presentation.navigation.Routes
import com.app.garapan.ui.theme.AccentBlue
import com.app.garapan.ui.theme.BorderColor
import com.app.garapan.ui.theme.BrandNavy
import com.app.garapan.ui.theme.MutedText
import com.app.garapan.ui.theme.PrimaryText
import com.app.garapan.ui.theme.SecondaryText
import com.app.garapan.ui.theme.Surface
import com.app.garapan.ui.theme.White

@Composable
fun PesanScreen(
    navController: NavController,
    viewModel: PesanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Surface
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 18.dp)
        ) {
            item {
                PesanHeader()
                Spacer(modifier = Modifier.height(16.dp))
                PesanSearchField(
                    query = uiState.query,
                    onQueryChanged = viewModel::onQueryChanged,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                SectionLabel(
                    title = "Admin",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            items(uiState.adminChats) { chat ->
                ChatPreviewCard(
                    chat = chat,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onClick = { navController.navigate(Routes.chatRoute(chat.id)) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(26.dp))
                SectionLabel(
                    title = "Percakapan",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            items(uiState.peopleChats) { chat ->
                ChatPreviewCard(
                    chat = chat,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun PesanHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.logo_garapan),
            contentDescription = "Garapan Logo",
            modifier = Modifier.size(34.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Pesan",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = BrandNavy
            )
        )
    }
}

@Composable
private fun PesanSearchField(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(White)
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = MutedText,
            modifier = Modifier.size(20.dp)
        )
        BasicTextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = PrimaryText),
            singleLine = true,
            cursorBrush = SolidColor(BrandNavy),
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text(
                        text = "Cari pesan atau nama...",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MutedText)
                    )
                }
                inner()
            }
        )
    }
}

@Composable
private fun SectionLabel(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.ExtraBold,
            color = SecondaryText,
            letterSpacing = 0.sp
        ),
        modifier = modifier
    )
}

@Composable
private fun ChatPreviewCard(
    chat: ChatPreviewItem,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChatAvatar(chat = chat)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chat.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryText
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = chat.preview,
                    style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (chat.statusLabel != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    StatusChip(label = chat.statusLabel, admin = chat.isAdmin)
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(
                modifier = Modifier
                    .width(72.dp)
                    .height(58.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = chat.time,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Medium,
                        color = if (chat.unreadCount > 0) AccentBlue else SecondaryText
                    ),
                    maxLines = 1
                )
                if (chat.unreadCount > 0) {
                    UnreadBadge(count = chat.unreadCount)
                } else {
                    Spacer(modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ChatAvatar(chat: ChatPreviewItem) {
    val avatarColor = when (chat.accent) {
        ChatAccent.BLUE -> AccentBlue
        ChatAccent.NAVY -> BrandNavy
        ChatAccent.CORAL -> Color(0xFFE94B5F)
        ChatAccent.GREEN -> Color(0xFF0F766E)
    }
    val initials = chat.name
        .split(" ")
        .take(2)
        .joinToString("") { it.first().uppercase() }

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(avatarColor.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        if (chat.isAdmin) {
            Icon(
                imageVector = Icons.Filled.SupportAgent,
                contentDescription = null,
                tint = avatarColor,
                modifier = Modifier.size(28.dp)
            )
        } else {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = avatarColor
                )
            )
        }
    }
}

@Composable
private fun StatusChip(
    label: String,
    admin: Boolean
) {
    val chipBackground = when {
        admin -> BrandNavy.copy(alpha = 0.1f)
        label.equals("Selesai", ignoreCase = true) -> Color(0xFF22C55E)
        else -> AccentBlue
    }
    val chipContent = if (admin) BrandNavy else White

    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            color = chipContent
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(chipBackground)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
private fun UnreadBadge(count: Int) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(AccentBlue),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = White
            )
        )
    }
}

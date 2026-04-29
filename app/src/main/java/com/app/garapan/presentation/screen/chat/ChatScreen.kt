package com.app.garapan.presentation.screen.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.garapan.ui.theme.AccentBlue
import com.app.garapan.ui.theme.BorderColor
import com.app.garapan.ui.theme.BrandNavy
import com.app.garapan.ui.theme.LightGray
import com.app.garapan.ui.theme.MutedText
import com.app.garapan.ui.theme.PrimaryText
import com.app.garapan.ui.theme.SecondaryText
import com.app.garapan.ui.theme.Surface
import com.app.garapan.ui.theme.White

@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel(),
    onCheckout: () -> Unit = { navController.navigate(com.app.garapan.presentation.navigation.Routes.CHECKOUT) }
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    Scaffold(
        containerColor = Surface,
        topBar = {
            ChatTopBar(
                name = uiState.workerName,
                initials = uiState.workerInitials,
                isOnline = uiState.isOnline,
                isAdminSupport = uiState.isAdminSupport,
                supportLabel = uiState.supportLabel,
                onBack = { navController.navigateUp() }
            )
        },
        bottomBar = {
            ChatInputBar(
                value = uiState.inputText,
                onValueChange = viewModel::onInputChanged,
                onSend = viewModel::onSend
            )
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
        ) {
            item {
                DateSeparator(label = uiState.dateSeparator)
            }
            items(uiState.messages) { message ->
                when (message) {
                    is ChatMessage.JasaCard -> JasaContextCard(message = message)
                    is ChatMessage.Sent -> SentBubble(message = message)
                    is ChatMessage.Received -> ReceivedBubble(
                        message = message,
                        isAdminSupport = uiState.isAdminSupport
                    )
                    is ChatMessage.FileAndOrderConfirmation -> FileAndOrderConfirmationBubble(message = message, onCheckout = onCheckout)
                }
            }
        }
    }
}

@Composable
private fun ChatTopBar(
    name: String,
    initials: String,
    isOnline: Boolean,
    isAdminSupport: Boolean,
    supportLabel: String?,
    onBack: () -> Unit
) {
    val statusColor = if (isOnline) Color(0xFF16A34A) else Color(0xFFDC2626)
    val connectionText = if (isOnline) "Tersambung" else "Terputus"
    val subtitle = supportLabel?.let { "$it - $connectionText" } ?: connectionText

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
        Box(
            modifier = Modifier
                .size(40.dp)
            .clip(CircleShape)
            .background(if (isAdminSupport) AccentBlue.copy(alpha = 0.12f) else LightGray)
            .border(1.dp, BorderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
            if (isAdminSupport) {
                Icon(
                    imageVector = Icons.Filled.SupportAgent,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy
                    )
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
private fun DateSeparator(label: String) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(MutedText.copy(alpha = 0.15f))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(color = SecondaryText)
            )
        }
    }
}

@Composable
private fun JasaContextCard(message: ChatMessage.JasaCard) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                .background(White)
        ) {
            Column {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "//",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MutedText,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = message.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryText
                            ),
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = message.price,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = AccentBlue
                            )
                        )
                    }
                }
                HorizontalDivider(color = BorderColor)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.RemoveRedEye,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Lihat Detail Jasa",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = AccentBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = message.time,
            style = MaterialTheme.typography.labelSmall.copy(color = MutedText),
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@Composable
private fun SentBubble(message: ChatMessage.Sent) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(horizontalAlignment = Alignment.End) {
            Box(
                modifier = Modifier
                    .widthIn(max = 260.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp))
                    .background(BrandNavy)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium.copy(color = White),
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message.time,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = White.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(AccentBlue.copy(alpha = 0.15f))
                .border(1.dp, BorderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "PT",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = AccentBlue,
                    fontSize = 9.sp
                )
            )
        }
    }
}

@Composable
private fun ReceivedBubble(
    message: ChatMessage.Received,
    isAdminSupport: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isAdminSupport) AccentBlue.copy(alpha = 0.12f) else LightGray)
                .border(1.dp, BorderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isAdminSupport) {
                Icon(
                    imageVector = Icons.Filled.SupportAgent,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text = message.senderInitials,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy,
                        fontSize = 9.sp
                    )
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .widthIn(max = 260.dp)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                .background(White)
                .border(1.dp, BorderColor, RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(color = PrimaryText),
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.time,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MutedText,
                        fontSize = 10.sp
                    ),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun FileAndOrderConfirmationBubble(message: ChatMessage.FileAndOrderConfirmation, onCheckout: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(LightGray)
                .border(1.dp, BorderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message.senderInitials,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = BrandNavy,
                    fontSize = 9.sp
                )
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Column(
                modifier = Modifier.widthIn(max = 280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // File attachment
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(White)
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentBlue.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = message.fileName,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryText
                                ),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                text = message.fileSize,
                                style = MaterialTheme.typography.labelSmall.copy(color = MutedText)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Outlined.FileDownload,
                            contentDescription = "Download",
                            tint = AccentBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Order confirmation card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AccentBlue.copy(alpha = 0.15f))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                tint = BrandNavy,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Konfirmasi Pesanan",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BrandNavy
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = message.serviceName,
                                style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                            )
                            Text(
                                text = message.servicePrice,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryText
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = message.extras,
                                style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                            )
                            Text(
                                text = "Termasuk",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryText
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = BorderColor)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText)
                            )
                            Text(
                                text = message.total,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AccentBlue
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(50.dp))
                                .background(BrandNavy)
                                .clickable(onClick = onCheckout)
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Konfirmasi Pesanan",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = White
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message.time,
                style = MaterialTheme.typography.labelSmall.copy(color = MutedText)
            )
        }
    }
}

@Composable
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MutedText.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Attach",
                tint = PrimaryText,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(Surface)
                .border(1.dp, BorderColor, RoundedCornerShape(24.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            if (value.isEmpty()) {
                Text(
                    text = "Ketik pesan Anda...",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MutedText)
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = PrimaryText),
                cursorBrush = SolidColor(BrandNavy)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(BrandNavy),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onSend) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

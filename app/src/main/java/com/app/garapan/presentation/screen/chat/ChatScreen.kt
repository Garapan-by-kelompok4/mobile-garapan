package com.app.garapan.presentation.screen.chat

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
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
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Send
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.File
import com.composables.icons.lucide.Image
import com.composables.icons.lucide.Receipt
import com.composables.icons.lucide.Headset
import com.composables.icons.lucide.Download
import com.composables.icons.lucide.Eye
import com.composables.icons.lucide.Paperclip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.app.garapan.domain.model.ActiveOrder
import com.app.garapan.domain.model.PesananStatus
import com.app.garapan.presentation.navigation.Routes
import com.app.garapan.ui.theme.AccentBlue
import com.app.garapan.ui.theme.BorderColor
import com.app.garapan.ui.theme.BrandNavy
import com.app.garapan.ui.theme.ErrorRed
import com.app.garapan.ui.theme.LightGray
import com.app.garapan.ui.theme.MutedText
import com.app.garapan.ui.theme.PrimaryText
import com.app.garapan.ui.theme.SecondaryText
import com.app.garapan.ui.theme.SuccessGreen
import com.app.garapan.ui.theme.Surface
import com.app.garapan.ui.theme.White
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel(),
    onCheckout: () -> Unit = { navController.navigate(com.app.garapan.presentation.navigation.Routes.CHECKOUT) }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val lifecycleOwner = LocalLifecycleOwner.current
    var didInitialSupportScroll by remember { mutableStateOf(false) }

    var showAttachSheet by remember { mutableStateOf(false) }
    val attachSheetState = rememberModalBottomSheetState()
    val photoPicker = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        uri?.let(viewModel::onPhotoPicked)
    }
    val documentPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let(viewModel::onDocumentPicked)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.startPolling()
                Lifecycle.Event.ON_PAUSE -> viewModel.stopPolling()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopPolling()
        }
    }

    // Trigger loading older history when the user scrolls near the top of the log.
    val shouldLoadOlder by remember {
        derivedStateOf {
            didInitialSupportScroll && uiState.isAdminSupport && uiState.hasMore && !uiState.isLoadingOlder &&
                listState.firstVisibleItemIndex <= 1
        }
    }
    LaunchedEffect(shouldLoadOlder) {
        if (shouldLoadOlder) viewModel.loadOlderMessages()
    }

    LaunchedEffect(uiState.isAdminSupport, uiState.isLoading, uiState.messages.size) {
        if (
            uiState.isAdminSupport &&
            !uiState.isLoading &&
            uiState.messages.isNotEmpty() &&
            !didInitialSupportScroll
        ) {
            val itemCount = snapshotFlow { listState.layoutInfo.totalItemsCount }
                .first { it > 0 }
            listState.scrollToItem(itemCount - 1)
            didInitialSupportScroll = true
        }
    }

    // Keep the same message under the user's eyes after older content is prepended.
    LaunchedEffect(Unit) {
        viewModel.prependEvents.collect { added ->
            listState.scrollToItem(
                listState.firstVisibleItemIndex + added,
                listState.firstVisibleItemScrollOffset
            )
        }
    }

    Scaffold(
        containerColor = Surface,
        topBar = {
            ChatTopBar(
                name = uiState.workerName,
                initials = uiState.workerInitials,
                isOnline = uiState.isOnline,
                isAdminSupport = uiState.isAdminSupport,
                supportLabel = uiState.supportLabel,
                showStatus = uiState.showStatus,
                onBack = { navController.navigateUp() }
            )
        },
        bottomBar = {
            ChatInputBar(
                value = uiState.inputText,
                onValueChange = viewModel::onInputChanged,
                onSend = viewModel::onSend,
                enabled = !uiState.isLoading && !uiState.isSending,
                isSending = uiState.isSending,
                onAttachClick = { showAttachSheet = true }.takeIf { uiState.canAttach && !uiState.isSending }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BrandNavy)
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
            ) {
                if (!uiState.isAdminSupport) {
                    uiState.activeOrder?.let { order ->
                        item {
                            OrderContextBanner(
                                activeOrder = order,
                                onClick = {
                                    navController.navigate(Routes.orderDetailRoute(order.pesananId))
                                }
                            )
                        }
                    }
                }
                if (uiState.isAdminSupport && (uiState.hasMore || uiState.isLoadingOlder)) {
                    item {
                        LoadOlderIndicator(isLoading = uiState.isLoadingOlder)
                    }
                }
                uiState.errorMessage?.let { message ->
                    item {
                        ErrorBanner(
                            message = message,
                            onRetry = viewModel::retry
                        )
                    }
                }
                item {
                    DateSeparator(label = uiState.dateSeparator)
                }
                items(uiState.messages) { message ->
                    when (message) {
                        is ChatMessage.JasaCard -> JasaContextCard(message = message)
                        is ChatMessage.Sent -> SentBubble(
                            message = message,
                            currentUserProfile = uiState.currentUserProfile
                        )
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

    if (showAttachSheet) {
        AttachmentOptionsSheet(
            sheetState = attachSheetState,
            onDismiss = { showAttachSheet = false },
            onPickPhoto = {
                showAttachSheet = false
                photoPicker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
            },
            onPickDocument = {
                showAttachSheet = false
                documentPicker.launch(CHAT_DOCUMENT_MIME_TYPES)
            }
        )
    }
}

private val CHAT_DOCUMENT_MIME_TYPES = arrayOf(
    "application/pdf",
    "application/zip",
    "application/x-zip-compressed",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttachmentOptionsSheet(
    sheetState: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit,
    onPickPhoto: () -> Unit,
    onPickDocument: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text(
                text = "Kirim Lampiran",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            AttachmentOptionRow(
                icon = Lucide.Image,
                label = "Foto",
                onClick = onPickPhoto
            )
            AttachmentOptionRow(
                icon = Lucide.File,
                label = "Dokumen",
                onClick = onPickDocument
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AttachmentOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(AccentBlue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = PrimaryText
            )
        )
    }
}

@Composable
private fun OrderContextBanner(
    activeOrder: ActiveOrder,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .background(White)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Lucide.Receipt,
            contentDescription = null,
            tint = BrandNavy,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activeOrder.title?.takeIf { it.isNotBlank() } ?: "Pesanan aktif",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText
                ),
                maxLines = 1
            )
            Text(
                text = "Ketuk untuk lihat detail pesanan",
                style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = activeOrder.status.toChatLabel(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = BrandNavy
            ),
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(AccentBlue.copy(alpha = 0.12f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        )
        Icon(
            imageVector = Lucide.ArrowRight,
            contentDescription = null,
            tint = MutedText,
            modifier = Modifier.size(18.dp)
        )
    }
}

private fun PesananStatus.toChatLabel(): String = when (this) {
    PesananStatus.PENDING -> "Menunggu"
    PesananStatus.PAID -> "Dibayar"
    PesananStatus.IN_PROGRESS -> "Proyek Aktif"
    PesananStatus.DELIVERED -> "Dikirim"
    PesananStatus.COMPLETED -> "Selesai"
    PesananStatus.DISPUTED -> "Sengketa"
    PesananStatus.CANCELLED -> "Dibatalkan"
}

@Composable
private fun ErrorBanner(
    message: String,
    onRetry: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ErrorRed.copy(alpha = 0.1f))
            .border(1.dp, ErrorRed.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = message,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall.copy(color = PrimaryText)
        )
        TextButton(onClick = onRetry) {
            Text(
                text = "Coba lagi",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = AccentBlue,
                    fontWeight = FontWeight.Bold
                )
            )
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
    showStatus: Boolean,
    onBack: () -> Unit
) {
    val statusColor = if (isOnline) SuccessGreen else ErrorRed
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
                imageVector = Lucide.ArrowLeft,
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
                    imageVector = Lucide.Headset,
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
            if (showStatus) {
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
}

@Composable
private fun LoadOlderIndicator(isLoading: Boolean) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        if (isLoading) {
            CircularProgressIndicator(
                color = BrandNavy,
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Text(
                text = "Gulir ke atas untuk pesan lama",
                style = MaterialTheme.typography.labelSmall.copy(color = MutedText)
            )
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
                        imageVector = Lucide.Eye,
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

/** Opens an attachment URL in whatever app can view it (browser, PDF viewer, gallery, etc). */
private fun openAttachment(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Tidak ada aplikasi untuk membuka file ini.", Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun AttachmentRow(fileName: String, contentColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Lucide.Paperclip,
            contentDescription = "Lampiran",
            tint = contentColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = fileName,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = contentColor,
                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
            ),
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SentBubble(
    message: ChatMessage.Sent,
    currentUserProfile: ChatCurrentUserProfile
) {
    val context = LocalContext.current
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
                    .let { base ->
                        val url = message.attachmentUrl
                        if (url != null) base.clickable { openAttachment(context, url) } else base
                    }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    if (message.attachmentUrl != null) {
                        AttachmentRow(fileName = message.attachmentName ?: message.text, contentColor = White)
                    } else {
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyMedium.copy(color = White),
                            lineHeight = 20.sp
                        )
                    }
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
            if (currentUserProfile.avatarUrl != null) {
                AsyncImage(
                    model = currentUserProfile.avatarUrl,
                    contentDescription = "Profile photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                )
            } else {
                Text(
                    text = currentUserProfile.initials,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = AccentBlue,
                        fontSize = 9.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun ReceivedBubble(
    message: ChatMessage.Received,
    isAdminSupport: Boolean
) {
    val context = LocalContext.current
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
                    imageVector = Lucide.Headset,
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
                .let { base ->
                    val url = message.attachmentUrl
                    if (url != null) base.clickable { openAttachment(context, url) } else base
                }
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                if (message.attachmentUrl != null) {
                    AttachmentRow(fileName = message.attachmentName ?: message.text, contentColor = AccentBlue)
                } else {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium.copy(color = PrimaryText),
                        lineHeight = 20.sp
                    )
                }
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
                                imageVector = Lucide.FileText,
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
                            imageVector = Lucide.Download,
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
                                imageVector = Lucide.Receipt,
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
                                    imageVector = Lucide.ArrowRight,
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
    onSend: () -> Unit,
    enabled: Boolean,
    isSending: Boolean,
    onAttachClick: (() -> Unit)? = null
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
                .background(MutedText.copy(alpha = 0.15f))
                .let { base -> onAttachClick?.let { base.clickable(onClick = it) } ?: base },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Lucide.Plus,
                contentDescription = "Attach",
                tint = if (onAttachClick != null) PrimaryText else MutedText,
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
                cursorBrush = SolidColor(BrandNavy),
                enabled = enabled
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (enabled) BrandNavy else MutedText),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onSend,
                enabled = enabled
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        color = White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Icon(
                        imageVector = Lucide.Send,
                        contentDescription = "Send",
                        tint = White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

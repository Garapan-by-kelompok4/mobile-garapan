package com.app.garapan.presentation.screen.project_detail

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.consumeWindowInsets
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.CircleCheckBig
import com.composables.icons.lucide.Code
import com.composables.icons.lucide.Users
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.CircleDollarSign
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.app.garapan.domain.model.ProjectProposal
import com.app.garapan.domain.model.ProjectStatus
import com.app.garapan.domain.model.ProposalStatus
import com.app.garapan.presentation.components.AppPrimaryButton
import com.app.garapan.presentation.components.AppTopBar
import com.app.garapan.presentation.navigation.NavResults
import com.app.garapan.presentation.navigation.Routes
import com.app.garapan.presentation.util.CurrencyFormatter
import com.app.garapan.ui.theme.AccentBlue
import com.app.garapan.ui.theme.BorderColor
import com.app.garapan.ui.theme.BrandNavy
import com.app.garapan.ui.theme.ErrorRed
import com.app.garapan.ui.theme.LightGray
import com.app.garapan.ui.theme.MutedText
import com.app.garapan.ui.theme.OnPrimary
import com.app.garapan.ui.theme.PrimaryText
import com.app.garapan.ui.theme.SecondaryText
import com.app.garapan.ui.theme.Surface
import com.app.garapan.ui.theme.White

@Composable
fun ProjectDetailScreen(
    navController: NavController,
    viewModel: ProjectDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(navController.currentBackStackEntry) {
        val handle = navController.currentBackStackEntry?.savedStateHandle ?: return@LaunchedEffect
        handle.getStateFlow(NavResults.PROJECT_REFRESH, false).collect { shouldRefresh ->
            if (!shouldRefresh) return@collect
            NavResults.clearProjectRefresh(handle)
            viewModel.retry()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProjectDetailEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is ProjectDetailEvent.NavigateToOrder -> {
                    navController.navigate(Routes.orderDetailRoute(event.pesananId))
                }
            }
        }
    }

    Scaffold(
        topBar = {
            ProjectDetailTopBar(
                onBack = { navController.navigateUp() },
                showEditButton = uiState.showEditButton,
                onEdit = { navController.navigate(Routes.editProjectRoute(uiState.id)) }
            )
        },
        containerColor = Surface
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandNavy)
                }
            }
            uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = viewModel::retry) {
                        Text("Coba Lagi")
                    }
                }
            }
            else -> ProjectDetailContent(
                uiState = uiState,
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun ProjectDetailContent(
    uiState: ProjectDetailUiState,
    viewModel: ProjectDetailViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
            if (uiState.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = uiState.imageUrl,
                    contentDescription = uiState.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // Title + meta section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                CategoryChip(category = uiState.category)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                MetaRow(icon = Lucide.Clock, text = uiState.deadline)
                Spacer(modifier = Modifier.height(8.dp))
                MetaRow(icon = Lucide.CircleDollarSign, text = uiState.budget)
                Spacer(modifier = Modifier.height(8.dp))
                MetaRow(icon = Lucide.Users, text = uiState.teamSize)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Client card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                        .background(Surface)
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(LightGray)
                                .border(1.dp, BorderColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.clientName
                                    .split(" ")
                                    .take(2)
                                    .joinToString("") { it.first().uppercase() },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BrandNavy
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = uiState.clientName,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = uiState.clientType,
                            style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                        )
                        if (uiState.isVerified) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Lucide.CircleCheckBig,
                                    contentDescription = null,
                                    tint = AccentBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Klien Terverifikasi",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = AccentBlue,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Text(
                    text = "Deskripsi Proyek",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = BorderColor)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = SecondaryText,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.isMahasiswa && (uiState.canPropose || uiState.myProposalStatus != null)) {
                MahasiswaProposalSection(uiState = uiState, viewModel = viewModel)
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (uiState.isKlienOwner && uiState.status == ProjectStatus.OPEN) {
                KlienProposalsSection(uiState = uiState, viewModel = viewModel)
                Spacer(modifier = Modifier.height(12.dp))
            }
    }
}

@Composable
private fun MahasiswaProposalSection(
    uiState: ProjectDetailUiState,
    viewModel: ProjectDetailViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Text(
            text = "Proposal Saya",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(color = BorderColor)
        Spacer(modifier = Modifier.height(14.dp))

        when (uiState.myProposalStatus) {
            ProposalStatus.ACCEPTED -> {
                ProposalStatusBanner(
                    label = "Proposal Diterima",
                    detail = "Klien menerima proposal Anda. Cek Riwayat Pesanan untuk melanjutkan.",
                    color = AccentBlue
                )
            }
            ProposalStatus.REJECTED -> {
                ProposalStatusBanner(
                    label = "Proposal Ditolak",
                    detail = "Klien memilih proposal lain untuk proyek ini.",
                    color = ErrorRed
                )
            }
            else -> {
                if (uiState.myProposalStatus == ProposalStatus.PENDING) {
                    ProposalStatusBanner(
                        label = "Menunggu Respons Klien",
                        detail = "Anda dapat mengubah proposal ini selama klien belum meresponsnya.",
                        color = AccentBlue
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
                OutlinedTextField(
                    value = uiState.proposalMessageInput,
                    onValueChange = viewModel::onProposalMessageChanged,
                    label = { Text("Pesan untuk klien") },
                    placeholder = { Text("Jelaskan mengapa Anda cocok untuk proyek ini") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.proposalPriceInput,
                    onValueChange = viewModel::onProposalPriceChanged,
                    label = { Text("Harga yang diajukan (Rp)") },
                    placeholder = { Text("500000") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = ThousandSeparatorTransformation
                )
                Spacer(modifier = Modifier.height(14.dp))
                AppPrimaryButton(
                    text = if (uiState.myProposalStatus == ProposalStatus.PENDING) "Perbarui Proposal" else "Ajukan Proposal",
                    onClick = viewModel::onSubmitProposal,
                    enabled = !uiState.isSubmittingProposal,
                    isLoading = uiState.isSubmittingProposal
                )
                if (uiState.myProposalStatus == ProposalStatus.PENDING) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = viewModel::onWithdrawProposal,
                        enabled = !uiState.isWithdrawingProposal,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isWithdrawingProposal) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = ErrorRed, strokeWidth = 2.dp)
                        } else {
                            Text("Tarik Proposal", color = ErrorRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProposalStatusBanner(label: String, detail: String, color: androidx.compose.ui.graphics.Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(14.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = color)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = detail,
            style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
        )
    }
}

@Composable
private fun KlienProposalsSection(
    uiState: ProjectDetailUiState,
    viewModel: ProjectDetailViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Text(
            text = "Proposal Masuk (${uiState.proposals.size})",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(color = BorderColor)
        Spacer(modifier = Modifier.height(14.dp))

        when {
            uiState.isLoadingProposals -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandNavy, modifier = Modifier.size(24.dp))
                }
            }
            uiState.proposals.isEmpty() -> {
                Text(
                    text = "Belum ada proposal masuk untuk proyek ini.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText)
                )
            }
            else -> {
                uiState.proposals.forEachIndexed { index, proposal ->
                    ProposalCard(
                        proposal = proposal,
                        isResponding = uiState.respondingProposalId == proposal.id,
                        respondingDisabled = uiState.respondingProposalId != null,
                        onAccept = { viewModel.onAcceptProposal(proposal.id) },
                        onReject = { viewModel.onRejectProposal(proposal.id) }
                    )
                    if (index != uiState.proposals.lastIndex) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProposalCard(
    proposal: ProjectProposal,
    isResponding: Boolean,
    respondingDisabled: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Text(
            text = proposal.mahasiswaName.ifBlank { "Mahasiswa" },
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = PrimaryText)
        )
        if (proposal.mahasiswaUniversity.isNotBlank()) {
            Text(
                text = proposal.mahasiswaUniversity,
                style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = proposal.message,
            style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Harga diajukan: ${CurrencyFormatter.formatRupiah(proposal.proposedPrice)}",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = AccentBlue)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onAccept,
                enabled = !respondingDisabled,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = BrandNavy, contentColor = OnPrimary)
            ) {
                if (isResponding) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = OnPrimary, strokeWidth = 2.dp)
                } else {
                    Text("Terima")
                }
            }
            OutlinedButton(
                onClick = onReject,
                enabled = !respondingDisabled,
                modifier = Modifier.weight(1f)
            ) {
                Text("Tolak", color = ErrorRed)
            }
        }
    }
}

@Composable
private fun ProjectDetailTopBar(
    onBack: () -> Unit,
    showEditButton: Boolean = false,
    onEdit: () -> Unit = {}
) {
    AppTopBar(
        title = "Detail Proyek",
        onBack = onBack,
        trailing = {
            if (showEditButton) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Lucide.Pencil,
                        contentDescription = "Edit proyek",
                        tint = PrimaryText
                    )
                }
            }
        }
    )
}

@Composable
private fun CategoryChip(category: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(AccentBlue.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Lucide.Code,
            contentDescription = null,
            tint = AccentBlue,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = category,
            style = MaterialTheme.typography.labelMedium.copy(
                color = AccentBlue,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Composable
private fun MetaRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MutedText,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText)
        )
    }
}

private object ThousandSeparatorTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val formatted = digits.reversed().chunked(3).joinToString(".").reversed()
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val dotsAdded = ((offset - 1) / 3).coerceAtLeast(0)
                return (offset + dotsAdded).coerceAtMost(formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int =
                formatted.substring(0, offset.coerceAtMost(formatted.length))
                    .count(Char::isDigit)
                    .coerceAtMost(digits.length)
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

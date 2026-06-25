package com.app.garapan.presentation.screen.skills

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.garapan.ui.theme.BorderColor
import com.app.garapan.ui.theme.BrandNavy
import com.app.garapan.ui.theme.ErrorRed
import com.app.garapan.ui.theme.OnPrimary
import com.app.garapan.ui.theme.PrimaryText
import com.app.garapan.ui.theme.SecondaryText
import com.app.garapan.ui.theme.Surface
import com.app.garapan.ui.theme.White

@Composable
fun SkillsScreen(
    navController: NavController,
    viewModel: SkillsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                SkillsEvent.Saved -> navController.navigateUp()
            }
        }
    }

    Scaffold(containerColor = Surface) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            SkillsTopBar(onBack = { navController.navigateUp() })
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Keahlian Profil",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryText
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pilih keahlian profil Anda. Ini terpisah dari kategori jasa/proyek di marketplace.",
                style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText)
            )
            Spacer(modifier = Modifier.height(20.dp))
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BrandNavy)
                    }
                }
                uiState.errorMessage != null && uiState.options.isEmpty() -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.errorMessage.orEmpty(),
                            style = MaterialTheme.typography.bodySmall.copy(color = ErrorRed)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = viewModel::retry) {
                            Text("Coba Lagi")
                        }
                    }
                }
                else -> {
                    SkillChipGroup(
                        options = uiState.options,
                        selected = uiState.selectedSkills,
                        onToggle = viewModel::onToggleSkill
                    )
                }
            }
            if (uiState.errorMessage != null && uiState.options.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.errorMessage.orEmpty(),
                    style = MaterialTheme.typography.bodySmall.copy(color = ErrorRed)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = viewModel::onSave,
                enabled = uiState.selectedSkills.isNotEmpty() && !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandNavy,
                    contentColor = OnPrimary
                )
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(22.dp),
                        strokeWidth = 2.dp,
                        color = OnPrimary
                    )
                } else {
                    Text(
                        text = "Simpan Keahlian",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SkillsTopBar(onBack: () -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = BrandNavy
            )
        }
        Text(
            text = "Edit Keahlian",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = BrandNavy
            )
        )
    }
}

@Composable
private fun SkillChipGroup(
    options: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        options.forEach { option ->
            val isSelected = option in selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(if (isSelected) BrandNavy else White)
                    .border(1.dp, if (isSelected) BrandNavy else BorderColor, RoundedCornerShape(50.dp))
                    .clickable { onToggle(option) }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isSelected) OnPrimary else PrimaryText,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

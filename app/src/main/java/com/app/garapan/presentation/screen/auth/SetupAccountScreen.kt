package com.app.garapan.presentation.screen.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.garapan.presentation.navigation.Routes
import com.app.garapan.ui.theme.AccentBlue
import com.app.garapan.ui.theme.BrandNavy
import com.app.garapan.ui.theme.BorderColor
import com.app.garapan.ui.theme.MutedText
import com.app.garapan.ui.theme.OnPrimary
import com.app.garapan.ui.theme.PrimaryText
import com.app.garapan.ui.theme.SecondaryText
import com.app.garapan.ui.theme.Surface
import com.app.garapan.ui.theme.White

@Composable
fun SetupAccountScreen(
    navController: NavController,
    role: String,
    viewModel: SetupAccountViewModel = hiltViewModel()
) {
    val studentState by viewModel.student.collectAsStateWithLifecycle()
    val clientState by viewModel.client.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SetupAccountEvent.Navigate -> navController.navigate(event.route) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .verticalScroll(rememberScrollState())
    ) {
        SetupHeader(
            title = if (role == "student") "Student Profile Setup" else "Complete Your\nProfile"
        )

        if (role == "student") {
            StudentSetupForm(
                state = studentState,
                skillOptions = uiState.skillOptions,
                isSkillOptionsLoading = uiState.isSkillOptionsLoading,
                skillOptionsError = uiState.skillOptionsError,
                onRetrySkillOptions = viewModel::retrySkillOptions,
                onFullNameChanged = viewModel::onStudentFullNameChanged,
                onUniversityChanged = viewModel::onUniversityChanged,
                onMajorChanged = viewModel::onMajorChanged,
                onYearsSelected = viewModel::onYearsSelected,
                onYearsDropdownToggle = viewModel::onYearsDropdownToggle,
                onToggleExpertise = viewModel::onToggleStudentExpertise,
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
                onComplete = { viewModel.onComplete(role) }
            )
        } else {
            ClientSetupForm(
                state = clientState,
                skillOptions = uiState.skillOptions,
                isSkillOptionsLoading = uiState.isSkillOptionsLoading,
                skillOptionsError = uiState.skillOptionsError,
                onRetrySkillOptions = viewModel::retrySkillOptions,
                onFullNameChanged = viewModel::onClientFullNameChanged,
                onStatusSelected = viewModel::onStatusSelected,
                onStatusDropdownToggle = viewModel::onStatusDropdownToggle,
                onIndustrySelected = viewModel::onIndustrySelected,
                onIndustryDropdownToggle = viewModel::onIndustryDropdownToggle,
                onCompanyNameChanged = viewModel::onCompanyProjectNameChanged,
                onToggleService = viewModel::onToggleClientService,
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
                onComplete = { viewModel.onComplete(role) }
            )
        }
    }
}

@Composable
private fun SetupHeader(title: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "GARAPAN",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = BrandNavy,
                letterSpacing = 2.sp
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "The IT Project Marketplace",
            style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = PrimaryText,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
private fun StudentSetupForm(
    state: StudentSetupState,
    skillOptions: List<String>,
    isSkillOptionsLoading: Boolean,
    skillOptionsError: String?,
    onRetrySkillOptions: () -> Unit,
    onFullNameChanged: (String) -> Unit,
    onUniversityChanged: (String) -> Unit,
    onMajorChanged: (String) -> Unit,
    onYearsSelected: (String) -> Unit,
    onYearsDropdownToggle: () -> Unit,
    onToggleExpertise: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onComplete: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(containerColor = White),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileField(label = "Full Name", value = state.fullName, onValueChange = onFullNameChanged, placeholder = "Haykal Rafi")
                Spacer(modifier = Modifier.height(12.dp))
                ProfileField(label = "Universitas", value = state.university, onValueChange = onUniversityChanged, placeholder = "e.g. UPN Veteran Jakarta")
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        ProfileField(label = "Major/Program of Study", value = state.major, onValueChange = onMajorChanged, placeholder = "e.g. Information Systems")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Years of Experience",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PrimaryText,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        SetupDropdown(
                            value = state.yearsOfExperience.ifEmpty { "0-1 years" },
                            expanded = state.isYearsDropdownExpanded,
                            options = yearsOfExperienceOptions,
                            onToggle = onYearsDropdownToggle,
                            onSelect = onYearsSelected
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(containerColor = White),
            border = BorderStroke(2.dp, AccentBlue)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select Main Expertise",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Choose your main expertise focus for collaborators to find you easily. (can choose more than one)",
                    style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                )
                Spacer(modifier = Modifier.height(12.dp))
                SkillOptionsSection(
                    skillOptions = skillOptions,
                    isLoading = isSkillOptionsLoading,
                    errorMessage = skillOptionsError,
                    onRetry = onRetrySkillOptions,
                    selected = state.selectedExpertise,
                    onToggle = onToggleExpertise
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = com.app.garapan.ui.theme.ErrorRed,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = onComplete,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandNavy, contentColor = OnPrimary)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.width(20.dp),
                    strokeWidth = 2.dp,
                    color = OnPrimary
                )
            } else {
                Text(
                    text = "Complete Profile",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "By Registering. You agree to the Terms and Conditions and Privacy policy of GARAPAN.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = SecondaryText,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ClientSetupForm(
    state: ClientSetupState,
    skillOptions: List<String>,
    isSkillOptionsLoading: Boolean,
    skillOptionsError: String?,
    onRetrySkillOptions: () -> Unit,
    onFullNameChanged: (String) -> Unit,
    onStatusSelected: (String) -> Unit,
    onStatusDropdownToggle: () -> Unit,
    onIndustrySelected: (String) -> Unit,
    onIndustryDropdownToggle: () -> Unit,
    onCompanyNameChanged: (String) -> Unit,
    onToggleService: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onComplete: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {

        SetupLabeledField(label = "Full Name", value = state.fullName, onValueChange = onFullNameChanged, placeholder = "Name")

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = PrimaryText,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                SetupDropdown(
                    value = state.status.ifEmpty { "Individual" },
                    expanded = state.isStatusDropdownExpanded,
                    options = statusOptions,
                    onToggle = onStatusDropdownToggle,
                    onSelect = onStatusSelected
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Industry",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = PrimaryText,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                SetupDropdown(
                    value = state.industry.ifEmpty { "Select" },
                    expanded = state.isIndustryDropdownExpanded,
                    options = industryOptions,
                    onToggle = onIndustryDropdownToggle,
                    onSelect = onIndustrySelected
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SetupLabeledField(label = "Company/Project Name", value = state.companyProjectName, onValueChange = onCompanyNameChanged, placeholder = "Delta Project")

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "What are you looking for?",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        SkillOptionsSection(
            skillOptions = skillOptions,
            isLoading = isSkillOptionsLoading,
            errorMessage = skillOptionsError,
            onRetry = onRetrySkillOptions,
            selected = state.selectedServices,
            onToggle = onToggleService
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = com.app.garapan.ui.theme.ErrorRed,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = onComplete,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandNavy, contentColor = OnPrimary)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.width(20.dp),
                    strokeWidth = 2.dp,
                    color = OnPrimary
                )
            } else {
                Text(
                    text = "Complete Profile",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = PrimaryText,
                fontWeight = FontWeight.SemiBold
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)),
            placeholder = { Text(text = placeholder, color = MutedText, style = MaterialTheme.typography.bodySmall) },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = PrimaryText,
                unfocusedTextColor = PrimaryText,
            ),
            shape = RoundedCornerShape(6.dp),
            textStyle = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun SetupLabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = PrimaryText,
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
            placeholder = { Text(text = placeholder, color = MutedText) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = PrimaryText,
                unfocusedTextColor = PrimaryText,
            ),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetupDropdown(
    value: String,
    expanded: Boolean,
    options: List<String>,
    onToggle: () -> Unit,
    onSelect: (String) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { onToggle() }
    ) {
        TextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = PrimaryText,
                unfocusedTextColor = PrimaryText,
            ),
            shape = RoundedCornerShape(8.dp),
            textStyle = MaterialTheme.typography.bodySmall
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = onToggle) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onSelect(option) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SkillOptionsSection(
    skillOptions: List<String>,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    when {
        isLoading -> {
            CircularProgressIndicator(
                modifier = Modifier.height(24.dp),
                strokeWidth = 2.dp,
                color = BrandNavy
            )
        }
        errorMessage != null -> {
            Column {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall.copy(color = com.app.garapan.ui.theme.ErrorRed)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Coba lagi",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = AccentBlue,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.clickable(onClick = onRetry)
                )
            }
        }
        else -> {
            ChipGroup(
                options = skillOptions,
                selected = selected,
                onToggle = onToggle
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipGroup(
    options: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = option in selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(if (isSelected) BrandNavy else Surface)
                    .border(1.dp, if (isSelected) BrandNavy else BorderColor, RoundedCornerShape(50.dp))
                    .clickable { onToggle(option) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
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

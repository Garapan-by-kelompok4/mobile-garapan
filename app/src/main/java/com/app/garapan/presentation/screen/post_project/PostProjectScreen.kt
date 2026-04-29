package com.app.garapan.presentation.screen.post_project

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.garapan.presentation.navigation.Routes
import com.app.garapan.ui.theme.AccentBlue
import com.app.garapan.ui.theme.BorderColor
import com.app.garapan.ui.theme.BrandNavy
import com.app.garapan.ui.theme.MutedText
import com.app.garapan.ui.theme.PrimaryText
import com.app.garapan.ui.theme.SecondaryText
import com.app.garapan.ui.theme.Surface
import com.app.garapan.ui.theme.White
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PostProjectScreen(
    navController: NavController,
    viewModel: PostProjectViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeadlinePicker by remember { mutableStateOf(false) }

    if (showDeadlinePicker) {
        DeadlineDatePickerDialog(
            onDismiss = { showDeadlinePicker = false },
            onDateSelected = {
                viewModel.onDeadlineChanged(it)
                showDeadlinePicker = false
            }
        )
    }

    Scaffold(
        bottomBar = {
            PostProjectBottomNav(navController = navController)
        },
        containerColor = Surface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PostProjectTopBar(onBack = { navController.navigateUp() })
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    ProjectHero()
                }
                item {
                    ProjectFormCard(title = "Informasi Dasar Proyek") {
                        PostProjectField(
                            label = "Judul Proyek",
                            value = uiState.title,
                            placeholder = "Misal: Pembuatan Aplikasi E-Commerce Berbasis React Native",
                            onValueChange = viewModel::onTitleChanged
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "Kategori Keahlian",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = SecondaryText,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        CategoryChips(
                            categories = viewModel.categories,
                            selectedCategory = uiState.selectedCategory,
                            onCategorySelected = viewModel::onCategorySelected
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        TeamSizeDropdown(
                            value = uiState.teamSize,
                            options = viewModel.teamOptions,
                            onSelected = viewModel::onTeamSizeSelected
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        PostProjectField(
                            label = "Deskripsi Detail",
                            value = uiState.description,
                            placeholder = "Jelaskan secara detail kebutuhan proyek, fitur utama, dan ekspektasi Anda terhadap freelancer...",
                            onValueChange = viewModel::onDescriptionChanged,
                            minHeight = 108.dp,
                            singleLine = false
                        )
                    }
                }
                item {
                    ProjectFormCard(title = "Ruang Lingkup Pekerjaan") {
                        Text(
                            text = "Anggaran (Range)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = SecondaryText,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        BudgetRangeFields(
                            minimumBudget = uiState.minimumBudget,
                            maximumBudget = uiState.maximumBudget,
                            onMinimumBudgetChanged = viewModel::onMinimumBudgetChanged,
                            onMaximumBudgetChanged = viewModel::onMaximumBudgetChanged
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        ReadOnlyPostProjectField(
                            label = "Tenggat Waktu (Deadline)",
                            value = uiState.deadline,
                            placeholder = "mm/dd/yyyy",
                            leadingIcon = Icons.Filled.CalendarToday,
                            onClick = { showDeadlinePicker = true }
                        )
                    }
                }
                item {
                    Button(
                        onClick = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandNavy,
                            contentColor = White
                        ),
                        shape = RoundedCornerShape(50.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp)
                    ) {
                        Text(
                            text = "Publikasikan Proyek",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PostProjectTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .padding(horizontal = 4.dp, vertical = 6.dp),
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
            text = "Post Proyek Baru",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = AccentBlue
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ProjectHero() {
    Column {
        Row {
            Text(
                text = "Wujudkan ",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryText,
                    lineHeight = 30.sp
                )
            )
            Text(
                text = "Proyek",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentBlue,
                    lineHeight = 30.sp
                )
            )
            Text(
                text = " mu",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryText,
                    lineHeight = 30.sp
                )
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Temukan talenta IT terbaik untuk mewujudkan ide digital Anda.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = SecondaryText,
                lineHeight = 20.sp
            )
        )
    }
}

@Composable
private fun ProjectFormCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(White)
            .padding(22.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = PrimaryText
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BorderColor.copy(alpha = 0.7f))
        )
        Spacer(modifier = Modifier.height(18.dp))
        content()
    }
}

@Composable
private fun BudgetRangeFields(
    minimumBudget: String,
    maximumBudget: String,
    onMinimumBudgetChanged: (String) -> Unit,
    onMaximumBudgetChanged: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PriceField(
            value = minimumBudget,
            onValueChange = onMinimumBudgetChanged,
            placeholder = "100.000",
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "  —  ",
            style = MaterialTheme.typography.bodyMedium.copy(color = MutedText)
        )
        PriceField(
            value = maximumBudget,
            onValueChange = onMaximumBudgetChanged,
            placeholder = "5.000.000",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PriceField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFE1E4E6))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Rp",
            style = MaterialTheme.typography.bodySmall.copy(
                color = SecondaryText,
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        BasicTextField(
            value = value,
            onValueChange = { raw -> onValueChange(raw.filter(Char::isDigit).take(9)) },
            modifier = Modifier.weight(1f),
            textStyle = MaterialTheme.typography.bodySmall.copy(
                color = PrimaryText,
                fontWeight = FontWeight.SemiBold
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = ThousandSeparatorTransformation,
            cursorBrush = SolidColor(BrandNavy),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodySmall.copy(color = MutedText),
                        maxLines = 1
                    )
                }
                inner()
            }
        )
    }
}

@Composable
private fun CategoryChips(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            val selected = category == selectedCategory
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .clickable { onCategorySelected(category) }
                    .background(if (selected) AccentBlue.copy(alpha = 0.14f) else Color(0xFFE4E7EA))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (selected) AccentBlue else SecondaryText
                    )
                )
            }
        }
    }
}

@Composable
private fun TeamSizeDropdown(
    value: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "Kebutuhan Tim",
            style = MaterialTheme.typography.labelMedium.copy(
                color = SecondaryText,
                fontWeight = FontWeight.SemiBold
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { expanded = true }
                    .background(Color(0xFFE1E4E6))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Group,
                    contentDescription = null,
                    tint = SecondaryText,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = value.ifEmpty { "Pilih kebutuhan tim" },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (value.isEmpty()) MutedText else PrimaryText,
                        fontWeight = if (value.isEmpty()) FontWeight.Normal else FontWeight.SemiBold
                    ),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = SecondaryText,
                    modifier = Modifier.size(22.dp)
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyMedium.copy(color = PrimaryText)
                            )
                        },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReadOnlyPostProjectField(
    label: String,
    value: String,
    placeholder: String,
    leadingIcon: ImageVector,
    onClick: () -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = SecondaryText,
                fontWeight = FontWeight.SemiBold
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onClick)
                .background(Color(0xFFE1E4E6))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = SecondaryText,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = value.ifEmpty { placeholder },
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (value.isEmpty()) MutedText else PrimaryText
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeadlineDatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val datePickerState = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(formatDeadline(millis))
                    } ?: onDismiss()
                }
            ) {
                Text("Pilih")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

private fun formatDeadline(millis: Long): String =
    SimpleDateFormat("MM/dd/yyyy", Locale.US).format(Date(millis))

@Composable
private fun PostProjectField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    minHeight: androidx.compose.ui.unit.Dp = 52.dp,
    singleLine: Boolean = true,
    leadingText: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = SecondaryText,
                fontWeight = FontWeight.SemiBold
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(minHeight)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFE1E4E6))
                .padding(horizontal = 12.dp, vertical = if (singleLine) 0.dp else 12.dp),
            verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = SecondaryText,
                    modifier = Modifier
                        .padding(top = if (singleLine) 0.dp else 2.dp)
                        .size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            if (leadingText != null) {
                Text(
                    text = leadingText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = PrimaryText,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = PrimaryText),
                singleLine = singleLine,
                cursorBrush = SolidColor(BrandNavy),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MutedText,
                                lineHeight = 20.sp
                            ),
                            maxLines = if (singleLine) 1 else 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    inner()
                }
            )
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = SecondaryText,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

private data class NavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

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

@Composable
private fun PostProjectBottomNav(navController: NavController) {
    val navItems = listOf(
        NavItem("Home", Icons.Filled.Home, Icons.Outlined.Home),
        NavItem("Search", Icons.Filled.Search, Icons.Outlined.Search),
        NavItem("New", Icons.Default.Add, Icons.Default.Add),
        NavItem("Pesan", Icons.Outlined.ChatBubbleOutline, Icons.Outlined.ChatBubbleOutline),
        NavItem("Profile", Icons.Filled.Person, Icons.Outlined.Person),
    )
    val selectedIndex = 2

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
                            1 -> navController.navigate(Routes.SEARCH)
                            3 -> navController.navigate(Routes.PESAN)
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

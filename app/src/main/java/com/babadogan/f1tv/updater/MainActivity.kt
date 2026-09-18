package com.babadogan.f1tv.updater

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

val F1Red = Color(0xFFE10600)
val F1Dark = Color(0xFF15151E)
val F1DarkCard = Color(0xFF1F1F2C)
val F1Gray = Color(0xFF949498)
val F1White = Color(0xFFFFFFFF)

class MainActivity : ComponentActivity() {

    private val viewModel: UpdaterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var currentLanguage by remember {
                mutableStateOf(Locale.getDefault().language) // "tr" or "en"
            }

            val context = LocalContext.current
            val configuration = LocalConfiguration.current
            val localizedConfiguration = remember(currentLanguage, configuration) {
                Configuration(configuration).apply {
                    val locale = Locale(currentLanguage)
                    setLocale(locale)
                    Locale.setDefault(locale)
                }
            }

            CompositionLocalProvider(
                LocalConfiguration provides localizedConfiguration,
                LocalContext provides context.createConfigurationContext(localizedConfiguration)
            ) {
                F1TVUpdaterScreen(
                    viewModel = viewModel,
                    currentLanguage = currentLanguage,
                    onToggleLanguage = {
                        val newLang = if (currentLanguage == "tr") "en" else "tr"
                        updateLocale(newLang)
                        currentLanguage = newLang
                        viewModel.refreshInstalledState()
                        viewModel.checkForUpdates()
                    }
                )
            }
        }
    }

    private fun updateLocale(lang: String) {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onAppResumed(this)
    }
}

@Composable
fun F1TVUpdaterScreen(
    viewModel: UpdaterViewModel,
    currentLanguage: String,
    onToggleLanguage: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showInfoDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(F1Dark)
            .padding(48.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(32.dp)
                                .background(F1Red, RoundedCornerShape(3.dp))
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = stringResource(R.string.header_title),
                            color = F1White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    val model = android.os.Build.MODEL
                    val product = android.os.Build.PRODUCT
                    val abi = android.os.Build.SUPPORTED_ABIS.firstOrNull() ?: "arm"
                    Text(
                        text = "Philips $model • $product ($abi)",
                        color = F1Gray,
                        fontSize = 16.sp
                    )
                }

                // Butonlar: Info, Dil Değiştirme ve Canlı F1 TV Aç
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bilgi Butonu
                    TvActionButton(
                        text = stringResource(R.string.btn_info),
                        backgroundColor = F1DarkCard,
                        focusedBorderColor = Color(0xFF64B5F6),
                        onClick = { showInfoDialog = true }
                    )

                    // Dil Butonu (TR / EN)
                    TvActionButton(
                        text = if (currentLanguage == "tr") "🌐 EN" else "🌐 TR",
                        backgroundColor = F1DarkCard,
                        focusedBorderColor = Color(0xFF00ADB5),
                        onClick = onToggleLanguage
                    )

                    // F1 TV Başlat Butonu
                    TvActionButton(
                        text = if (uiState.isInstalled) stringResource(R.string.btn_launch_f1) else stringResource(R.string.btn_not_installed),
                        backgroundColor = F1DarkCard,
                        focusedBorderColor = F1White,
                        enabled = uiState.isInstalled,
                        onClick = { viewModel.launchF1TV(context) }
                    )
                }
            }

            // Info Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                InfoCard(
                    title = stringResource(R.string.card_installed_version),
                    value = if (uiState.isInstalled) uiState.installedVersion else stringResource(R.string.btn_not_installed),
                    valueColor = if (uiState.isInstalled) F1White else Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )

                InfoCard(
                    title = stringResource(R.string.card_latest_version),
                    value = uiState.latestVersion.ifEmpty { stringResource(R.string.status_not_checked) },
                    modifier = Modifier.weight(1f)
                )

                val statusColor = when {
                    !uiState.isInstalled -> Color(0xFFFF9800)
                    uiState.isUpdateAvailable -> F1Red
                    else -> Color(0xFF4CAF50)
                }

                val statusText = when {
                    uiState.isLoading -> stringResource(R.string.status_checking)
                    uiState.statusArg.isNotEmpty() -> {
                        val intArg = uiState.statusArg.toIntOrNull()
                        if (intArg != null) {
                            stringResource(uiState.statusResId, intArg)
                        } else {
                            stringResource(uiState.statusResId, uiState.statusArg)
                        }
                    }
                    else -> stringResource(uiState.statusResId)
                }

                InfoCard(
                    title = stringResource(R.string.card_status),
                    value = statusText,
                    valueColor = statusColor,
                    isLoading = uiState.isLoading,
                    modifier = Modifier.weight(1.2f)
                )
            }

            // Progress Bar if Downloading
            if (uiState.isDownloading) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    LinearProgressIndicator(
                        progress = { uiState.downloadProgress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = F1Red,
                        trackColor = F1DarkCard
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.status_downloading, uiState.downloadProgress),
                        color = F1Gray,
                        fontSize = 14.sp
                    )
                }
            }

            // Bottom Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TvActionButton(
                    text = if (uiState.isLoading) stringResource(R.string.status_checking) else stringResource(R.string.btn_check_updates),
                    backgroundColor = F1DarkCard,
                    focusedBorderColor = Color(0xFFE0E0E0),
                    enabled = !uiState.isLoading && !uiState.isDownloading,
                    isLoading = uiState.isLoading,
                    onClick = { viewModel.checkForUpdates() }
                )

                if (!uiState.isInstalled || uiState.isUpdateAvailable || uiState.downloadUrl.isNotEmpty()) {
                    val buttonText = when {
                        uiState.isDownloading -> stringResource(R.string.btn_downloading)
                        !uiState.isInstalled -> stringResource(R.string.btn_download_install)
                        uiState.isUpdateAvailable -> stringResource(R.string.btn_download_update)
                        else -> stringResource(R.string.btn_reinstall)
                    }

                    TvActionButton(
                        text = buttonText,
                        backgroundColor = F1Red,
                        focusedBorderColor = Color(0xFFFFD700),
                        enabled = !uiState.isDownloading && uiState.downloadUrl.isNotEmpty(),
                        isLoading = uiState.isDownloading,
                        onClick = { viewModel.downloadAndInstall(context) }
                    )
                }
            }
        }

        if (showInfoDialog) {
            BackHandler { showInfoDialog = false }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .width(540.dp)
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = F1DarkCard)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Title
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .width(5.dp)
                                    .height(24.dp)
                                    .background(F1Red, RoundedCornerShape(2.5.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(R.string.dialog_about_title),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = F1White
                            )
                        }

                        // Developer
                        Column {
                            Text(
                                text = stringResource(R.string.dialog_developer_label),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = F1Gray
                            )
                            Text(
                                text = stringResource(R.string.dialog_developer_name),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = F1White
                            )
                        }

                        // Website
                        Column {
                            Text(
                                text = stringResource(R.string.dialog_website_label),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = F1Gray
                            )
                            Text(
                                text = stringResource(R.string.dialog_website_url),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64B5F6)
                            )
                        }

                        // Patch Author
                        Column {
                            Text(
                                text = stringResource(R.string.dialog_patch_label),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = F1Gray
                            )
                            Text(
                                text = stringResource(R.string.dialog_patch_author),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = F1White
                            )
                        }

                        // Device & Platform
                        Column {
                            Text(
                                text = stringResource(R.string.dialog_device_label),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = F1Gray
                            )
                            val model = android.os.Build.MODEL
                            val product = android.os.Build.PRODUCT
                            val abi = android.os.Build.SUPPORTED_ABIS.firstOrNull() ?: "arm"
                            Text(
                                text = "Philips $model • $product ($abi)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = F1White
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Close button
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            TvActionButton(
                                text = stringResource(R.string.btn_close),
                                backgroundColor = F1Red,
                                focusedBorderColor = Color(0xFFFFD700),
                                onClick = { showInfoDialog = false }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = F1White,
    isLoading: Boolean = false
) {
    Card(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = F1DarkCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = F1Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = F1Red,
                        strokeWidth = 2.5.dp
                    )
                }
                Text(
                    text = value,
                    color = valueColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
fun TvActionButton(
    text: String,
    backgroundColor: Color,
    focusedBorderColor: Color,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isFocused) focusedBorderColor else backgroundColor,
            contentColor = if (isFocused) F1Dark else F1White,
            disabledContainerColor = backgroundColor.copy(alpha = 0.6f),
            disabledContentColor = F1White.copy(alpha = 0.6f)
        ),
        modifier = Modifier
            .height(52.dp)
            .focusable(interactionSource = interactionSource)
            .then(
                if (isFocused) Modifier.border(3.dp, focusedBorderColor, RoundedCornerShape(8.dp))
                else Modifier
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = if (isFocused) F1Dark else F1White,
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

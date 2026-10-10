package com.berkbb.f1tv.manager

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.edit
import java.util.Locale

val F1Red = Color(0xFFE10600)
val F1Dark = Color(0xFF15151E)
val F1DarkCard = Color(0xFF1F1F2C)
val F1Gray = Color(0xFF949498)
val F1White = Color(0xFFFFFFFF)

class MainActivity : ComponentActivity() {

    private val viewModel: UpdaterViewModel by viewModels()

    companion object {
        private const val PREFS_NAME = "f1tv_updater_prefs"
        private const val KEY_SELECTED_LANGUAGE = "selected_language"
    }

    private fun getSavedLanguage(): String? {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SELECTED_LANGUAGE, null)
    }

    private fun saveLanguagePreference(lang: String) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(KEY_SELECTED_LANGUAGE, lang) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val savedLang = getSavedLanguage()
        val systemLang = Locale.getDefault().language
        val initialLang = LocaleHelper.resolveInitialLanguage(savedLang, systemLang)

        setContent {
            var currentLanguage by remember {
                mutableStateOf(initialLang)
            }

            val context = LocalContext.current
            val configuration = LocalConfiguration.current
            val localizedConfiguration = remember(currentLanguage, configuration) {
                Configuration(configuration).apply {
                    setLocale(Locale.forLanguageTag(currentLanguage))
                }
            }
            val localizedContext = remember(context, localizedConfiguration) {
                context.createConfigurationContext(localizedConfiguration)
            }

            CompositionLocalProvider(
                LocalConfiguration provides localizedConfiguration,
                LocalContext provides localizedContext
            ) {
                F1TVManagerScreen(
                    viewModel = viewModel,
                    currentLanguage = currentLanguage,
                    onToggleLanguage = {
                        val newLang = LocaleHelper.getNextLanguage(currentLanguage)
                        saveLanguagePreference(newLang)
                        // Durum metinleri kaynak ID'si olarak tutulduğu için yeniden çizim yeterli; ağ çağrısı gerekmez
                        currentLanguage = newLang
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onAppResumed()
    }
}

@Composable
fun F1TVManagerScreen(
    viewModel: UpdaterViewModel,
    currentLanguage: String,
    onToggleLanguage: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
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
                    val manufacturer = android.os.Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
                    val model = android.os.Build.MODEL
                    val product = android.os.Build.PRODUCT
                    val abi = android.os.Build.SUPPORTED_ABIS.firstOrNull() ?: "arm"
                    Text(
                        text = stringResource(R.string.header_subtitle, manufacturer, model, product, abi),
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

                    // Dil Butonu (EN / TR / RO)
                    TvActionButton(
                        text = stringResource(R.string.btn_language, currentLanguage.uppercase(Locale.ROOT)),
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
                        onClick = { viewModel.launchF1TV() }
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
                    uiState.isSignatureConflict -> F1Red
                    !uiState.isInstalled -> Color(0xFFFF9800)
                    uiState.isUpdateAvailable -> F1Red
                    else -> Color(0xFF4CAF50)
                }

                val statusText = when {
                    uiState.isLoading -> stringResource(R.string.status_checking)
                    // statusArg tipi (Int / String) ilgili metnin %1$d / %1$s biçimiyle eşleşir
                    else -> uiState.statusArg?.let { arg -> stringResource(uiState.statusResId, arg) }
                        ?: stringResource(uiState.statusResId)
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
            val cancelFocusRequester = remember { FocusRequester() }
            LaunchedEffect(uiState.isDownloading) {
                // İndirme başlayınca ana buton devre dışı kalır; odağı İptal butonuna taşı
                if (uiState.isDownloading) runCatching { cancelFocusRequester.requestFocus() }
            }
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

                if (uiState.isSignatureConflict && uiState.isInstalled) {
                    // Farklı imza anahtarıyla kurulu F1 TV önce kaldırılmalı
                    TvActionButton(
                        text = stringResource(R.string.btn_uninstall_f1),
                        backgroundColor = F1Red,
                        focusedBorderColor = Color(0xFFFFD700),
                        onClick = { viewModel.uninstallF1TV() }
                    )
                } else if (!uiState.isInstalled || uiState.isUpdateAvailable || uiState.downloadUrl.isNotEmpty()) {
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
                        onClick = { viewModel.downloadAndInstall() }
                    )

                    if (uiState.isDownloading) {
                        TvActionButton(
                            text = stringResource(R.string.btn_cancel_download),
                            backgroundColor = F1DarkCard,
                            focusedBorderColor = Color(0xFFE0E0E0),
                            modifier = Modifier.focusRequester(cancelFocusRequester),
                            onClick = { viewModel.cancelDownload() }
                        )
                    }
                }
            }
        }

        if (showInfoDialog) {
            // Ayrı pencere: D-pad odağı diyalog içinde kalır, arkadaki butonlara geçemez; Geri tuşu kapatır
            val closeFocusRequester = remember { FocusRequester() }
            Dialog(
                onDismissRequest = { showInfoDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                LaunchedEffect(Unit) {
                    runCatching { closeFocusRequester.requestFocus() }
                }
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
                            val manufacturer = android.os.Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
                            val model = android.os.Build.MODEL
                            val product = android.os.Build.PRODUCT
                            val abi = android.os.Build.SUPPORTED_ABIS.firstOrNull() ?: "arm"
                            Text(
                                text = stringResource(R.string.header_subtitle, manufacturer, model, product, abi),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = F1White
                            )
                        }

                        // Disclaimer / Legal Note
                        Text(
                            text = stringResource(R.string.dialog_disclaimer),
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = F1Gray
                        )

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
                                modifier = Modifier.focusRequester(closeFocusRequester),
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
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    // Button zaten odaklanabilir; ek .focusable() ikinci bir odak durağı yaratıyordu
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
        modifier = modifier
            .height(52.dp)
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

package com.babadogan.f1tv.updater

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class UpdaterUiState(
    val installedVersion: String = "",
    val latestVersion: String = "",
    val downloadUrl: String = "",
    val isInstalled: Boolean = false,
    val isUpdateAvailable: Boolean = false,
    val isLoading: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Int = 0,
    val statusResId: Int = R.string.status_detecting,
    val statusArg: String = ""
)

class UpdaterViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(UpdaterUiState())
    val uiState: StateFlow<UpdaterUiState> = _uiState.asStateFlow()

    init {
        refreshInstalledState()
        checkForUpdates()
        observeInstallEvents()
    }

    private fun observeInstallEvents() {
        viewModelScope.launch {
            InstallEvents.events.collect { event ->
                when (event) {
                    is InstallEvent.Success -> {
                        refreshInstalledState(preferredStatus = R.string.status_installed_success)
                    }
                    is InstallEvent.Failure -> {
                        _uiState.value = _uiState.value.copy(
                            isDownloading = false,
                            statusResId = R.string.toast_install_failed,
                            statusArg = event.message
                        )
                    }
                }
            }
        }
    }

    fun onAppResumed(context: Context) {
        val canInstall = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }

        val preferred = if (canInstall && _uiState.value.statusResId == R.string.status_permission_required) {
            R.string.status_permission_granted
        } else null

        refreshInstalledState(preferredStatus = preferred)
    }

    fun refreshInstalledState(preferredStatus: Int? = null) {
        val pm = getApplication<Application>().packageManager
        val (version, installed) = try {
            val pInfo = pm.getPackageInfo("com.formulaone.production", 0)
            Pair(pInfo.versionName ?: "N/A", true)
        } catch (e: PackageManager.NameNotFoundException) {
            Pair("", false)
        }

        val latest = _uiState.value.latestVersion.replace("v", "").trim()
        val isAlreadyInstalled = latest.isNotEmpty() && version.contains(latest)
        val isNewer = latest.isNotEmpty() && !isAlreadyInstalled && installed
        val needsInstall = !installed

        val statusRes = when {
            preferredStatus != null -> preferredStatus
            needsInstall -> R.string.status_not_installed
            isNewer -> R.string.status_update_available
            installed -> R.string.status_up_to_date
            else -> R.string.status_detecting
        }

        _uiState.value = _uiState.value.copy(
            installedVersion = version,
            isInstalled = installed,
            isUpdateAvailable = isNewer || needsInstall,
            isDownloading = false,
            statusResId = statusRes,
            statusArg = ""
        )
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                statusResId = R.string.status_checking,
                statusArg = ""
            )
            try {
                val (tag, url) = fetchLatestRelease()
                val cleanTag = tag.replace("v", "").trim()
                val isInstalled = _uiState.value.isInstalled
                val isAlreadyInstalled = _uiState.value.installedVersion.contains(cleanTag)
                val isNewer = cleanTag.isNotEmpty() && !isAlreadyInstalled && isInstalled
                val needsInstall = !isInstalled

                val statusRes = when {
                    needsInstall -> R.string.status_not_installed
                    isNewer -> R.string.status_update_available
                    else -> R.string.status_up_to_date
                }

                _uiState.value = _uiState.value.copy(
                    latestVersion = tag,
                    downloadUrl = url,
                    isUpdateAvailable = isNewer || needsInstall,
                    isLoading = false,
                    statusResId = statusRes,
                    statusArg = ""
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statusResId = R.string.status_check_failed,
                    statusArg = e.localizedMessage ?: ""
                )
            }
        }
    }

    private suspend fun fetchLatestRelease(): Pair<String, String> = withContext(Dispatchers.IO) {
        val apiUrl = "https://api.github.com/repos/Alexvbp/f1tv-4k-patch/releases/latest"
        val connection = (URL(apiUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("User-Agent", "F1TV-Philips-Updater")
            connectTimeout = 10000
            readTimeout = 10000
        }

        val jsonStr = connection.inputStream.bufferedReader().use { it.readText() }
        val root = JSONObject(jsonStr)
        val tagName = root.optString("tag_name", "")

        val assets = root.optJSONArray("assets")
        var downloadUrl = ""
        if (assets != null) {
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                val name = asset.optString("name", "")
                if (name.endsWith(".apkm")) {
                    downloadUrl = asset.optString("browser_download_url", "")
                    break
                }
            }
        }
        Pair(tagName, downloadUrl)
    }

    fun downloadAndInstall(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                _uiState.value = _uiState.value.copy(
                    statusResId = R.string.status_permission_required,
                    statusArg = ""
                )
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    try {
                        val fallback = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(fallback)
                    } catch (_: Exception) {}
                }
                return
            }
        }

        val url = _uiState.value.downloadUrl
        if (url.isEmpty()) return

        val apkmFile = File(context.cacheDir, "f1tv_update.apkm")

        viewModelScope.launch {
            val needsDownload = !apkmFile.exists() || apkmFile.length() < 20_000_000L

            var success = true
            if (needsDownload) {
                _uiState.value = _uiState.value.copy(
                    isDownloading = true,
                    downloadProgress = 0,
                    statusResId = R.string.status_starting_download,
                    statusArg = ""
                )

                success = withContext(Dispatchers.IO) {
                    try {
                        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                            requestMethod = "GET"
                            instanceFollowRedirects = true
                            connectTimeout = 15000
                            readTimeout = 30000
                        }

                        val totalLength = conn.contentLength
                        var downloaded = 0L

                        conn.inputStream.use { input ->
                            FileOutputStream(apkmFile).use { output ->
                                val buffer = ByteArray(8192)
                                var read: Int
                                while (input.read(buffer).also { read = it } != -1) {
                                    output.write(buffer, 0, read)
                                    downloaded += read
                                    if (totalLength > 0) {
                                        val progress = ((downloaded * 100) / totalLength).toInt()
                                        _uiState.value = _uiState.value.copy(
                                            downloadProgress = progress,
                                            statusResId = R.string.status_downloading,
                                            statusArg = progress.toString()
                                        )
                                    }
                                }
                            }
                        }
                        true
                    } catch (e: Exception) {
                        e.printStackTrace()
                        false
                    }
                }
            }

            if (success) {
                _uiState.value = _uiState.value.copy(
                    isDownloading = false,
                    statusResId = R.string.status_preparing,
                    statusArg = ""
                )
                val installed = withContext(Dispatchers.IO) {
                    PackageInstallerHelper.installApkm(context, apkmFile)
                }
                if (installed) {
                    _uiState.value = _uiState.value.copy(
                        statusResId = R.string.status_confirm_prompt,
                        statusArg = ""
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        statusResId = R.string.status_install_failed,
                        statusArg = ""
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isDownloading = false,
                    statusResId = R.string.status_download_failed,
                    statusArg = ""
                )
            }
        }
    }

    fun launchF1TV(context: Context) {
        val intent = context.packageManager.getLeanbackLaunchIntentForPackage("com.formulaone.production")
            ?: context.packageManager.getLaunchIntentForPackage("com.formulaone.production")

        if (intent != null) {
            context.startActivity(intent)
        } else {
            _uiState.value = _uiState.value.copy(
                statusResId = R.string.btn_not_installed,
                statusArg = ""
            )
        }
    }
}

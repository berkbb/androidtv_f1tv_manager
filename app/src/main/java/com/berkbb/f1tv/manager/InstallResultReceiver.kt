package com.berkbb.f1tv.manager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.widget.Toast

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface InstallEvent {
    data object Success : InstallEvent
    data class Failure(val message: String) : InstallEvent
}

object InstallEvents {
    private val _events = MutableSharedFlow<InstallEvent>(extraBufferCapacity = 5)
    val events = _events.asSharedFlow()

    fun emit(event: InstallEvent) {
        _events.tryEmit(event)
    }
}

class InstallResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)

        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirmIntent = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                if (confirmIntent != null) {
                    confirmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(confirmIntent)
                }
            }
            PackageInstaller.STATUS_SUCCESS -> {
                InstallEvents.emit(InstallEvent.Success)
            }
            else -> {
                InstallEvents.emit(InstallEvent.Failure(message ?: "Code $status"))
            }
        }
    }
}

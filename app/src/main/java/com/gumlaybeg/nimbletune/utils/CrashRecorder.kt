package com.gumlaybeg.nimbletune.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.gumlaybeg.nimbletune.BuildConfig
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CrashRecorder {
    private const val CRASH_LOG_FILE = "last_crash_log.txt"
    private var installed = false

    fun install(context: Context) {
        if (installed) return
        installed = true

        val appContext = context.applicationContext
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))

                val report = buildString {
                    appendLine("=== NimbleTune Crash Report ===")
                    appendLine("Time: $timestamp")
                    appendLine("App: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) | ${BuildConfig.FLAVOR} | ${BuildConfig.BUILD_TYPE}")
                    appendLine("Device: ${Build.MANUFACTURER} ${Build.BRAND} ${Build.MODEL} (${Build.DEVICE})")
                    appendLine("Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
                    appendLine("Thread: ${thread.name} (id=${thread.id})")
                    appendLine()
                    append(sw.toString())
                }
                File(appContext.filesDir, CRASH_LOG_FILE).writeText(report)
            } catch (_: Throwable) {
                // Never mask the original crash
            } finally {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    fun getCrashLog(context: Context): String? {
        return try {
            val file = File(context.applicationContext.filesDir, CRASH_LOG_FILE)
            if (file.exists()) file.readText().takeIf { it.isNotBlank() } else null
        } catch (_: Throwable) {
            null
        }
    }

    fun clearCrashLog(context: Context) {
        try {
            val file = File(context.applicationContext.filesDir, CRASH_LOG_FILE)
            if (file.exists()) {
                file.delete()
            }
        } catch (_: Throwable) {
        }
    }
}

@Composable
fun CrashLogDialog(
    crashLog: String,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = {
            Text(
                text = "Previous Crash Log",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                SelectionContainer {
                    Text(
                        text = crashLog,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Crash Log", crashLog))
                        Toast.makeText(context, "Crash log copied", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Copy")
                }

                Spacer(Modifier.width(4.dp))

                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "NimbleTune Crash Log")
                            putExtra(Intent.EXTRA_TEXT, crashLog)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Crash Log"))
                    }
                ) {
                    Text("Share")
                }

                Spacer(Modifier.width(4.dp))

                TextButton(onClick = onDismiss) {
                    Text("Clear")
                }
            }
        }
    )
}

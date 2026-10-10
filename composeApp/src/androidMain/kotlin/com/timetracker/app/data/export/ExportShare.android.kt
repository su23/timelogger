package com.timetracker.app.data.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

actual class ExportShare(private val context: Context) {

    actual fun share(fileName: String, mimeType: String, content: String) {
        // Written under cacheDir/exports, which file_paths.xml exposes to the
        // provider; the system clears it, so exports do not accumulate.
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, fileName)
        file.writeText(content)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val send = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(send, "Export").apply {
                // Started from the application context, not an Activity.
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }
}

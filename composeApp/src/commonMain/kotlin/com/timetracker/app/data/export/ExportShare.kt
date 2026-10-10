package com.timetracker.app.data.export

/**
 * Hands generated export content to the platform's own share sheet, so the
 * report can leave the app as a file rather than only being previewed in it.
 */
expect class ExportShare {
    fun share(fileName: String, mimeType: String, content: String)
}

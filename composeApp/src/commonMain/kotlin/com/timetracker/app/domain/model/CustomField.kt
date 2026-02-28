package com.timetracker.app.domain.model

enum class CustomFieldType(val value: String, val label: String) {
    Text("text", "Text"),
    Number("number", "Number"),
    Dropdown("dropdown", "Dropdown");

    companion object {
        fun fromValue(value: String) = entries.find { it.value == value } ?: Text
    }
}

data class CustomField(
    val id: Long = 0,
    val activityId: Long,
    val name: String,
    val fieldType: CustomFieldType = CustomFieldType.Text,
    val options: String = "", // comma-separated for dropdown
    val sortOrder: Int = 0,
)

data class CustomFieldValue(
    val id: Long = 0,
    val entryId: Long,
    val fieldId: Long,
    val value: String = "",
)

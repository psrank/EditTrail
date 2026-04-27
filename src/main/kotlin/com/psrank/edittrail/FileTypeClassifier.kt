package com.psrank.edittrail

object FileTypeClassifier {

    private val EXTENSION_MAP: Map<String, String> = mapOf(
        "cs" to "C#",
        "xaml" to "XAML",
        "razor" to "Razor",
        "cshtml" to "Razor",
        "json" to "JSON",
        "xml" to "XML",
        "sql" to "SQL",
        "yml" to "YAML",
        "yaml" to "YAML",
        "md" to "Markdown",
        "markdown" to "Markdown",
        "kt" to "Kotlin",
        "kts" to "Kotlin",
        "java" to "Java",
        "js" to "JavaScript",
        "jsx" to "JavaScript",
        "ts" to "TypeScript",
        "tsx" to "TypeScript",
        "html" to "HTML",
        "htm" to "HTML",
        "css" to "CSS",
        "scss" to "CSS",
        "sass" to "CSS",
        "less" to "CSS"
    )

    fun classify(fileName: String): String {
        val dot = fileName.lastIndexOf('.')
        if (dot < 0 || dot == fileName.length - 1) return "Other"
        val ext = fileName.substring(dot + 1).lowercase()
        return EXTENSION_MAP[ext] ?: "Other"
    }
}

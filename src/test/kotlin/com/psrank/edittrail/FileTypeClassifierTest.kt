package com.psrank.edittrail

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FileTypeClassifierTest {

    // ── Known extension mappings ──────────────────────────────────────────────────

    @Test
    fun `cs extension maps to C#`() {
        assertEquals("C#", FileTypeClassifier.classify("UserService.cs"))
    }

    @Test
    fun `kt extension maps to Kotlin`() {
        assertEquals("Kotlin", FileTypeClassifier.classify("EditTrailPanel.kt"))
    }

    @Test
    fun `kts extension maps to Kotlin`() {
        assertEquals("Kotlin", FileTypeClassifier.classify("build.gradle.kts"))
    }

    @Test
    fun `json extension maps to JSON`() {
        assertEquals("JSON", FileTypeClassifier.classify("appsettings.json"))
    }

    @Test
    fun `xml extension maps to XML`() {
        assertEquals("XML", FileTypeClassifier.classify("AndroidManifest.xml"))
    }

    @Test
    fun `xaml extension maps to XAML`() {
        assertEquals("XAML", FileTypeClassifier.classify("MainPage.xaml"))
    }

    @Test
    fun `razor extension maps to Razor`() {
        assertEquals("Razor", FileTypeClassifier.classify("Index.razor"))
    }

    @Test
    fun `cshtml extension maps to Razor`() {
        assertEquals("Razor", FileTypeClassifier.classify("_Layout.cshtml"))
    }

    @Test
    fun `sql extension maps to SQL`() {
        assertEquals("SQL", FileTypeClassifier.classify("migration.sql"))
    }

    @Test
    fun `yml extension maps to YAML`() {
        assertEquals("YAML", FileTypeClassifier.classify("docker-compose.yml"))
    }

    @Test
    fun `yaml extension maps to YAML`() {
        assertEquals("YAML", FileTypeClassifier.classify("config.yaml"))
    }

    @Test
    fun `md extension maps to Markdown`() {
        assertEquals("Markdown", FileTypeClassifier.classify("README.md"))
    }

    @Test
    fun `markdown extension maps to Markdown`() {
        assertEquals("Markdown", FileTypeClassifier.classify("CHANGELOG.markdown"))
    }

    @Test
    fun `java extension maps to Java`() {
        assertEquals("Java", FileTypeClassifier.classify("Main.java"))
    }

    @Test
    fun `js extension maps to JavaScript`() {
        assertEquals("JavaScript", FileTypeClassifier.classify("index.js"))
    }

    @Test
    fun `jsx extension maps to JavaScript`() {
        assertEquals("JavaScript", FileTypeClassifier.classify("App.jsx"))
    }

    @Test
    fun `ts extension maps to TypeScript`() {
        assertEquals("TypeScript", FileTypeClassifier.classify("app.ts"))
    }

    @Test
    fun `tsx extension maps to TypeScript`() {
        assertEquals("TypeScript", FileTypeClassifier.classify("App.tsx"))
    }

    @Test
    fun `html extension maps to HTML`() {
        assertEquals("HTML", FileTypeClassifier.classify("index.html"))
    }

    @Test
    fun `htm extension maps to HTML`() {
        assertEquals("HTML", FileTypeClassifier.classify("index.htm"))
    }

    @Test
    fun `css extension maps to CSS`() {
        assertEquals("CSS", FileTypeClassifier.classify("styles.css"))
    }

    @Test
    fun `scss extension maps to CSS`() {
        assertEquals("CSS", FileTypeClassifier.classify("theme.scss"))
    }

    @Test
    fun `sass extension maps to CSS`() {
        assertEquals("CSS", FileTypeClassifier.classify("variables.sass"))
    }

    @Test
    fun `less extension maps to CSS`() {
        assertEquals("CSS", FileTypeClassifier.classify("layout.less"))
    }

    // ── Fallback to Other ─────────────────────────────────────────────────────────

    @Test
    fun `unrecognised extension maps to Other`() {
        assertEquals("Other", FileTypeClassifier.classify("data.parquet"))
    }

    @Test
    fun `file with no extension maps to Other`() {
        assertEquals("Other", FileTypeClassifier.classify("Makefile"))
    }

    @Test
    fun `empty file name maps to Other`() {
        assertEquals("Other", FileTypeClassifier.classify(""))
    }

    // ── Case-insensitivity ────────────────────────────────────────────────────────

    @Test
    fun `uppercase extension is treated same as lowercase`() {
        assertEquals("C#", FileTypeClassifier.classify("UserService.CS"))
    }

    @Test
    fun `mixed-case extension is treated same as lowercase`() {
        assertEquals("Kotlin", FileTypeClassifier.classify("App.Kt"))
    }
}

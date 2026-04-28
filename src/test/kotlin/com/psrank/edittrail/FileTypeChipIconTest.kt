package com.psrank.edittrail

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Test

/**
 * Sanity check that every file-type label produced by [FileTypeClassifier]
 * resolves to a non-null icon and that distinct labels resolve to distinct
 * icon instances (no accidental shared fallback).
 */
class FileTypeChipIconTest {

    private val classifierLabels = listOf(
        "C#", "XAML", "Razor", "JSON", "XML", "SQL", "YAML", "Markdown",
        "Kotlin", "Java", "JavaScript", "TypeScript", "HTML", "CSS", "Other",
    )

    @Test
    fun `every classifier label resolves to a non-null icon`() {
        classifierLabels.forEach { label ->
            assertNotNull(FileTypeChipIcon.iconFor(label), "iconFor($label) returned null")
        }
    }

    @Test
    fun `every icon reports a positive 16x16 size`() {
        classifierLabels.forEach { label ->
            val icon = FileTypeChipIcon.iconFor(label)
            // AllIcons may be 16x16 or platform-defined; require >= 12 to catch null/empty icons.
            val w = icon.iconWidth
            val h = icon.iconHeight
            assert(w >= 12 && h >= 12) { "iconFor($label) has unreasonable size ${w}x$h" }
        }
    }

    @Test
    fun `text-badge fallback labels render distinct badges`() {
        // Types we know fall back to TextBadgeIcon — each must produce a
        // different rendered glyph so users can tell them apart.
        val badges = listOf("C#", "Kotlin", "TypeScript", "Razor", "XAML", "SQL", "Markdown")
            .map { it to FileTypeChipIcon.iconFor(it) }
        for (i in badges.indices) {
            for (j in i + 1 until badges.size) {
                val (li, ii) = badges[i]
                val (lj, ij) = badges[j]
                assertNotSame(ii, ij, "$li and $lj resolved to the same icon instance")
            }
        }
    }

    @Test
    fun `unknown label falls back to a non-null icon`() {
        // Defensive: the chip bar must never crash on a label outside the
        // classifier's vocabulary.
        assertNotNull(FileTypeChipIcon.iconFor("Unobtainium"))
    }

    @Test
    fun `Other falls back to a generic AllIcons file type`() {
        // The "Other" label classification uses a generic file glyph; assert
        // the icon for "Other" matches AllIcons.FileTypes.Any_type.
        val icon = FileTypeChipIcon.iconFor("Other")
        val expected = com.intellij.icons.AllIcons.FileTypes.Any_type
        assertEquals(expected, icon, "Other should resolve to AllIcons.FileTypes.Any_type")
    }
}

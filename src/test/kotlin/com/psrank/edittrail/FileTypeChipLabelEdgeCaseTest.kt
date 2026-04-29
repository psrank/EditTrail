package com.psrank.edittrail

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

/**
 * Edge-case coverage for the iteration-7 chip label format.
 */
class FileTypeChipLabelEdgeCaseTest {

    @Test
    fun `format never produces a string containing parentheses`() {
        val labels = listOf("C#", "Kotlin", "Java", "JSON", "Razor", "TypeScript", "Other")
        val counts = listOf(0, 1, 7, 42, 999, 12345)
        for (label in labels) {
            for (count in counts) {
                val rendered = FileTypeChipLabel.format(FileTypeChip(label, count, false))
                assertFalse(
                    rendered.contains("(") || rendered.contains(")"),
                    "format($label, $count) must not contain parentheses, got `$rendered`"
                )
            }
        }
    }

    @Test
    fun `format respects the selected flag without altering the rendered text`() {
        val unselected = FileTypeChip(label = "Kotlin", count = 4, selected = false)
        val selected = FileTypeChip(label = "Kotlin", count = 4, selected = true)
        // The chip's selected flag controls visual state in the panel, not the
        // label string itself.
        assertEquals(FileTypeChipLabel.format(unselected), FileTypeChipLabel.format(selected))
    }

    @Test
    fun `format handles labels with special characters`() {
        assertEquals("C++ 3", FileTypeChipLabel.format(FileTypeChip("C++", 3, false)))
        assertEquals("F# 5", FileTypeChipLabel.format(FileTypeChip("F#", 5, false)))
        assertEquals(".kts 9", FileTypeChipLabel.format(FileTypeChip(".kts", 9, false)))
    }

    @Test
    fun `formatAll never produces a string containing parentheses`() {
        for (n in listOf(0, 1, 7, 42, 999, 12345)) {
            val rendered = FileTypeChipLabel.formatAll(n)
            assertFalse(
                rendered.contains("(") || rendered.contains(")"),
                "formatAll($n) must not contain parentheses, got `$rendered`"
            )
        }
    }

    @Test
    fun `formatAll always begins with the literal All label`() {
        for (n in listOf(0, 1, 42, 12345)) {
            val rendered = FileTypeChipLabel.formatAll(n)
            assertEquals("All $n", rendered)
        }
    }
}

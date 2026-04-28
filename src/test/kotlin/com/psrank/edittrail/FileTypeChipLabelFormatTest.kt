package com.psrank.edittrail

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * TDD-first tests for the chip label format change in iteration 7.
 *
 * The chip rendering in the panel previously produced "C# (8)" — these tests
 * pin the new format ("C# 8") on a small pure helper that the panel will call
 * from rebuildChipBar(...). The helper does not exist yet, so this file
 * fails to compile until [FileTypeChipLabel.format] is introduced in stage 5.
 */
class FileTypeChipLabelFormatTest {

    @Test
    fun `format omits parentheses around count`() {
        val chip = FileTypeChip(label = "C#", count = 8, selected = false)
        assertEquals("C# 8", FileTypeChipLabel.format(chip))
    }

    @Test
    fun `format preserves single space between label and count`() {
        val chip = FileTypeChip(label = "JSON", count = 7, selected = false)
        assertEquals("JSON 7", FileTypeChipLabel.format(chip))
    }

    @Test
    fun `format handles zero count`() {
        val chip = FileTypeChip(label = "Other", count = 0, selected = false)
        assertEquals("Other 0", FileTypeChipLabel.format(chip))
    }

    @Test
    fun `formatAll uses the same shape for the All chip`() {
        // The "All" chip is a special case rendered separately from FileTypeChip.
        // It still must follow the new "label count" format, e.g. "All 42".
        assertEquals("All 42", FileTypeChipLabel.formatAll(totalCount = 42))
    }

    @Test
    fun `formatAll handles zero total`() {
        assertEquals("All 0", FileTypeChipLabel.formatAll(totalCount = 0))
    }
}

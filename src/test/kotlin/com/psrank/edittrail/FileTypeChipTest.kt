package com.psrank.edittrail

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FileTypeChipTest {

    @Test
    fun `chip stores label count and selected state`() {
        val chip = FileTypeChip(label = "Kotlin", count = 5, selected = true)
        assertEquals("Kotlin", chip.label)
        assertEquals(5, chip.count)
        assertTrue(chip.selected)
    }

    @Test
    fun `chip with selected false is not selected`() {
        val chip = FileTypeChip(label = "JSON", count = 2, selected = false)
        assertFalse(chip.selected)
    }

    @Test
    fun `two chips with same fields are equal`() {
        val a = FileTypeChip(label = "C#", count = 8, selected = false)
        val b = FileTypeChip(label = "C#", count = 8, selected = false)
        assertEquals(a, b)
    }

    @Test
    fun `chips with different labels are not equal`() {
        val a = FileTypeChip(label = "C#", count = 8, selected = false)
        val b = FileTypeChip(label = "Kotlin", count = 8, selected = false)
        assertNotEquals(a, b)
    }

    @Test
    fun `chips with different counts are not equal`() {
        val a = FileTypeChip(label = "JSON", count = 3, selected = false)
        val b = FileTypeChip(label = "JSON", count = 7, selected = false)
        assertNotEquals(a, b)
    }

    @Test
    fun `copy produces independent instance with overridden field`() {
        val original = FileTypeChip(label = "Kotlin", count = 4, selected = false)
        val selected = original.copy(selected = true)
        assertFalse(original.selected)
        assertTrue(selected.selected)
        assertEquals(original.label, selected.label)
        assertEquals(original.count, selected.count)
    }
}

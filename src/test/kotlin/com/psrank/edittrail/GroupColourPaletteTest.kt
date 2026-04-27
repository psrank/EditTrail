package com.psrank.edittrail

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.awt.Color

/**
 * Tests for GroupColourPalette — colour stability and basic contract.
 */
class GroupColourPaletteTest {

    @Test
    fun `colourFor same groupId always returns the same Color`() {
        val first = GroupColourPalette.colourFor(42)
        val second = GroupColourPalette.colourFor(42)
        assertEquals(first, second)
    }

    @Test
    fun `colourFor returns a non-null Color`() {
        val colour = GroupColourPalette.colourFor(1)
        assertNotNull(colour)
    }

    @Test
    fun `colourFor is stable across many repeated calls for the same groupId`() {
        val id = 7
        val reference = GroupColourPalette.colourFor(id)
        repeat(20) {
            assertEquals(reference, GroupColourPalette.colourFor(id),
                "colourFor($id) returned a different Color on call #${it + 2}")
        }
    }

    @Test
    fun `colourFor handles groupIds beyond palette size without throwing`() {
        // Palette has 8 entries; groupId > 8 should still return a colour via modular cycling
        assertDoesNotThrow {
            val c = GroupColourPalette.colourFor(100)
            assertNotNull(c)
        }
    }

    @Test
    fun `colourFor groupId 0 does not throw`() {
        assertDoesNotThrow {
            val c = GroupColourPalette.colourFor(0)
            assertNotNull(c)
        }
    }

    @Test
    fun `colourFor negative groupId does not throw`() {
        assertDoesNotThrow {
            val c = GroupColourPalette.colourFor(-1)
            assertNotNull(c)
        }
    }
}

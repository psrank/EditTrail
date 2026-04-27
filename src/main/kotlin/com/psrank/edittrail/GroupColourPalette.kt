package com.psrank.edittrail

import com.intellij.ui.JBColor
import java.awt.Color

/**
 * Stable, session-scoped colour palette for file groups.
 *
 * Maps group IDs (integers) to colours from a fixed cycle of 8 [JBColor] pairs.
 * Once a colour is assigned to a group ID it is never changed during the IDE session.
 */
object GroupColourPalette {

    private val palette: List<JBColor> = listOf(
        JBColor(Color(0xE57373), Color(0xB71C1C)), // red
        JBColor(Color(0x64B5F6), Color(0x0D47A1)), // blue
        JBColor(Color(0x81C784), Color(0x1B5E20)), // green
        JBColor(Color(0xFFB74D), Color(0xE65100)), // orange
        JBColor(Color(0xBA68C8), Color(0x4A148C)), // purple
        JBColor(Color(0x4DB6AC), Color(0x004D40)), // teal
        JBColor(Color(0xF06292), Color(0x880E4F)), // pink
        JBColor(Color(0xFFF176), Color(0xF57F17)), // yellow
    )

    private val assigned: MutableMap<Int, Color> = mutableMapOf()
    private var nextIndex: Int = 0

    /** Returns the colour assigned to [groupId], allocating from the palette on first access. */
    @Synchronized
    fun colourFor(groupId: Int): Color {
        return assigned.getOrPut(groupId) {
            val colour = palette[nextIndex % palette.size]
            nextIndex++
            colour
        }
    }
}

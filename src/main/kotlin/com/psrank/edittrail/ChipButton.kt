package com.psrank.edittrail

import com.intellij.ui.JBColor
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.BorderFactory
import javax.swing.Icon
import javax.swing.JToggleButton

/**
 * Compact chip button for the EditTrail file-type filter bar.
 *
 * - Renders a tight icon-and-count chip with no L&F border chrome eating
 *   horizontal space.
 * - Paints a theme-aware rounded-rect background only when selected, so the
 *   active filter is visually unambiguous regardless of the host LAF.
 */
class ChipButton(
    text: String,
    icon: Icon? = null,
    selected: Boolean = false,
) : JToggleButton(text, icon, selected) {

    init {
        isFocusPainted = false
        isContentAreaFilled = false
        isBorderPainted = false
        isOpaque = false
        margin = java.awt.Insets(0, 0, 0, 0)
        border = BorderFactory.createEmptyBorder(1, PAD_X, 1, PAD_X)
        iconTextGap = 2
        font = font.deriveFont(font.size2D - 2f)
    }

    override fun paintComponent(g: Graphics) {
        if (isSelected || (model.isArmed && model.isPressed)) {
            val g2 = g.create() as Graphics2D
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                // Inset by 1px on every side so the rounded stroke has room and
                // never gets clipped by the component's bounding box.
                val x = 1
                val y = 1
                val w = width - 2
                val h = height - 2
                g2.color = SELECTED_FILL
                g2.fillRoundRect(x, y, w, h, ARC, ARC)
                g2.color = SELECTED_STROKE
                g2.drawRoundRect(x, y, w - 1, h - 1, ARC, ARC)
            } finally {
                g2.dispose()
            }
        }
        super.paintComponent(g)
    }

    override fun getPreferredSize(): Dimension {
        // Compute the natural size (icon + text + our small padding) and
        // refuse to grow beyond it, so chips don't gain extra width or
        // height from L&F minimum-size rules.
        val fm = getFontMetrics(font)
        val textW = fm.stringWidth(text ?: "")
        val textH = fm.ascent + fm.descent
        val iconW = icon?.iconWidth ?: 0
        val iconH = icon?.iconHeight ?: 0
        val gap = if (icon != null && !text.isNullOrEmpty()) iconTextGap else 0
        val width = iconW + gap + textW + PAD_X * 2 + 2
        val height = maxOf(iconH, textH) + PAD_Y * 2
        return Dimension(width, height)
    }

    companion object {
        private const val PAD_X = 3
        // 2 instead of 1 so the inset rounded-rect background has room to draw
        // a 1px stroke at the top and bottom without being clipped.
        private const val PAD_Y = 2
        private const val ARC = 8
        private val SELECTED_FILL = JBColor(Color(0xC8D4E8), Color(0x44546B))
        private val SELECTED_STROKE = JBColor(Color(0x6E8FCF), Color(0xAFC2E6))
    }
}

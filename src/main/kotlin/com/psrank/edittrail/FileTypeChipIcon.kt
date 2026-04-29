package com.psrank.edittrail

import com.intellij.icons.AllIcons
import com.intellij.ui.JBColor
import java.awt.Color
import java.awt.Component
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.Icon

/**
 * Resolves a 16×16 chip icon for each file-type label produced by
 * [FileTypeClassifier].
 *
 * Where the IntelliJ Platform ships a stable `AllIcons.FileTypes.*` glyph,
 * we use it directly. Where it doesn't (C#, Kotlin, Razor, XAML, SQL,
 * TypeScript, Markdown), we render a small theme-aware text badge so every
 * label gets a distinguishable icon without depending on plugin classes that
 * may not be on the classpath.
 */
object FileTypeChipIcon {

    fun iconFor(label: String): Icon = when (label) {
        // Core IntelliJ Platform icons — safe across plugin/IDE editions.
        "JavaScript" -> AllIcons.FileTypes.JavaScript
        "JSON"       -> AllIcons.FileTypes.Json
        "XML"        -> AllIcons.FileTypes.Xml
        "YAML"       -> AllIcons.FileTypes.Yaml
        "Java"       -> AllIcons.FileTypes.Java
        "HTML"       -> AllIcons.FileTypes.Html
        "CSS"        -> AllIcons.FileTypes.Css
        "Other"      -> AllIcons.FileTypes.Any_type

        // Types whose icons live in language plugins we don't depend on —
        // render a small theme-aware text badge instead.
        "C#"         -> TextBadgeIcon("C#")
        "Kotlin"     -> TextBadgeIcon("Kt")
        "TypeScript" -> TextBadgeIcon("Ts")
        "Razor"      -> TextBadgeIcon("Rz")
        "XAML"       -> TextBadgeIcon("Xa")
        "SQL"        -> TextBadgeIcon("Sq")
        "Markdown"   -> TextBadgeIcon("Md")

        else -> TextBadgeIcon(label.take(2))
    }
}

/**
 * Tiny rounded-rect badge that draws 1–2 letters centred inside a 16×16 box.
 *
 * Used as a stand-in icon for file types that don't have an `AllIcons`
 * glyph in the platform we depend on.
 */
internal class TextBadgeIcon(
    private val text: String,
    private val size: Int = 16,
) : Icon {

    override fun getIconWidth(): Int = size
    override fun getIconHeight(): Int = size

    override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
        val g2 = g.create() as Graphics2D
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

            // Background — soft chip-style fill that adapts to the IDE theme.
            g2.color = JBColor(Color(0xC8D4E8), Color(0x44546B))
            g2.fillRoundRect(x, y, size, size, 4, 4)

            // Text — same blue family as the brand icon for visual coherence.
            g2.color = JBColor(Color(0x2C4A85), Color(0xC7D7F2))
            val baseFont = c?.font ?: g2.font
            val pointSize = (size * 0.55f).coerceAtLeast(8f)
            g2.font = baseFont.deriveFont(Font.BOLD, pointSize)
            val fm = g2.fontMetrics
            val tw = fm.stringWidth(text)
            val tx = x + (size - tw) / 2
            val ty = y + (size + fm.ascent - fm.descent) / 2 - 1
            g2.drawString(text, tx, ty)
        } finally {
            g2.dispose()
        }
    }
}

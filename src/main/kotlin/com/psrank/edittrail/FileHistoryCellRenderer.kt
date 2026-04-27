package com.psrank.edittrail

import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.ui.JBColor
import java.awt.Component
import java.awt.Font
import java.awt.Graphics
import java.awt.Insets
import javax.swing.DefaultListCellRenderer
import javax.swing.JList

/**
 * Cell renderer for the EditTrail history list.
 *
 * Handles two [EditTrailResult] subtypes:
 * - [EditTrailResult.HistoryResult]: bold file name + grey relative path (unchanged behaviour)
 * - [EditTrailResult.ProjectFileResult]: grey italic file name + "Project result" label
 *
 * When a [EditTrailResult.HistoryResult] entry has a non-null [FileHistoryEntry.groupId], a 4 px
 * coloured vertical bar is painted on the left edge of the row via [paintComponent].
 */
class FileHistoryCellRenderer : DefaultListCellRenderer() {

    companion object {
        private const val GROUP_BAR_WIDTH = 4
    }

    /** The group colour to paint this cycle, or null if no bar should be drawn. */
    private var currentGroupColour: java.awt.Color? = null

    override fun getListCellRendererComponent(
        list: JList<*>,
        value: Any?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)

        currentGroupColour = null

        when (value) {
            is EditTrailResult.HistoryResult -> {
                renderHistoryResult(value)
                val gid = value.entry.groupId
                if (gid != null) {
                    currentGroupColour = GroupColourPalette.colourFor(gid)
                }
            }
            is EditTrailResult.ProjectFileResult -> renderProjectResult(value)
            is FileHistoryEntry -> renderLegacyEntry(value)
        }

        // Reserve 4 px on the left so text never overlaps the coloured bar.
        val base = border
        border = javax.swing.BorderFactory.createCompoundBorder(
            base,
            javax.swing.BorderFactory.createEmptyBorder(0, GROUP_BAR_WIDTH, 0, 0)
        )

        return this
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val colour = currentGroupColour ?: return
        g.color = colour
        g.fillRect(0, 0, GROUP_BAR_WIDTH, height)
    }

    private fun renderHistoryResult(result: EditTrailResult.HistoryResult) {
        val entry = result.entry
        text = buildString {
            append("<html><b>")
            append(escapeHtml(entry.fileName))
            append("</b>")
            if (entry.relativePath.isNotBlank() && entry.relativePath != entry.fileName) {
                append("&nbsp;&nbsp;<font color='gray'>")
                append(escapeHtml(entry.relativePath))
                append("</font>")
            }
            append("</html>")
        }
        val vf = VirtualFileManager.getInstance().findFileByUrl(entry.fileUrl)
        icon = vf?.fileType?.icon
    }

    private fun renderProjectResult(result: EditTrailResult.ProjectFileResult) {
        val gray = JBColor.GRAY
        text = buildString {
            append("<html><i><font color='#")
            append(Integer.toHexString(gray.rgb and 0xFFFFFF))
            append("'>")
            append(escapeHtml(result.fileName))
            append("</font></i>")
            if (result.relativePath.isNotBlank() && result.relativePath != result.fileName) {
                append("&nbsp;&nbsp;<font color='gray'>")
                append(escapeHtml(result.relativePath))
                append("</font>")
            }
            append("&nbsp;&nbsp;<font color='gray'><i>Project result</i></font>")
            append("</html>")
        }
        icon = result.virtualFile?.fileType?.icon
    }

    /** Legacy path — renders a raw [FileHistoryEntry] if encountered. */
    private fun renderLegacyEntry(entry: FileHistoryEntry) {
        renderHistoryResult(EditTrailResult.HistoryResult(entry))
    }

    private fun escapeHtml(text: String): String =
        text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
}


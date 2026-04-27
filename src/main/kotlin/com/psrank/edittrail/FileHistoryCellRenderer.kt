package com.psrank.edittrail

import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.ui.JBColor
import java.awt.Component
import java.awt.Font
import javax.swing.DefaultListCellRenderer
import javax.swing.JList

/**
 * Cell renderer for the EditTrail history list.
 *
 * Handles two [EditTrailResult] subtypes:
 * - [EditTrailResult.HistoryResult]: bold file name + grey relative path (unchanged behaviour)
 * - [EditTrailResult.ProjectFileResult]: grey italic file name + "Project result" label
 */
class FileHistoryCellRenderer : DefaultListCellRenderer() {

    override fun getListCellRendererComponent(
        list: JList<*>,
        value: Any?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)

        when (value) {
            is EditTrailResult.HistoryResult -> renderHistoryResult(value)
            is EditTrailResult.ProjectFileResult -> renderProjectResult(value)
            // Legacy fallback — should not occur in normal operation.
            is FileHistoryEntry -> renderLegacyEntry(value)
        }

        return this
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


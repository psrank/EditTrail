package com.psrank.edittrail

import com.intellij.openapi.vfs.VirtualFileManager
import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JList

/**
 * Cell renderer for the EditTrail history list.
 *
 * Displays a file-type icon (when the virtual file is resolvable), the file name
 * in bold, and the relative path in a lighter color.
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

        if (value is FileHistoryEntry) {
            text = buildString {
                append("<html><b>")
                append(escapeHtml(value.fileName))
                append("</b>")
                if (value.relativePath.isNotBlank() && value.relativePath != value.fileName) {
                    append("&nbsp;&nbsp;<font color='gray'>")
                    append(escapeHtml(value.relativePath))
                    append("</font>")
                }
                append("</html>")
            }

            // Resolve the icon from the virtual file's file type (best-effort; null is fine).
            val vf = VirtualFileManager.getInstance().findFileByUrl(value.fileUrl)
            icon = vf?.fileType?.icon
        }

        return this
    }

    private fun escapeHtml(text: String): String =
        text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
}

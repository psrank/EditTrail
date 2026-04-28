package com.psrank.edittrail

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

/**
 * Source-level regression test for iteration 7.
 *
 * Constructing [EditTrailPanel] requires a real IntelliJ [com.intellij.openapi.project.Project]
 * and [com.intellij.openapi.wm.ToolWindow], which aren't available in this
 * project's pure-JUnit test harness. So we instead read the panel source and
 * assert that the controls iteration 7 was supposed to remove are actually
 * gone — preventing a future refactor from accidentally re-introducing them.
 */
class EditTrailPanelStructureTest {

    private val panelSource: String by lazy {
        val path = Path.of("src/main/kotlin/com/psrank/edittrail/EditTrailPanel.kt")
        check(Files.exists(path)) { "EditTrailPanel.kt not found at expected path: $path" }
        Files.readString(path)
    }

    @Test
    fun `panel does not declare a JCheckBox for any removed search-option toggle`() {
        // The five filter controls (matchPath, matchContent, regex, caseSensitive,
        // globalSearch) used to be JCheckBox fields named *Box. They now live on
        // EditTrailToolbarState. None of the old field names should remain.
        val forbidden = listOf(
            "matchPathBox",
            "matchContentBox",
            "regexBox",
            "caseSensitiveBox",
            "globalSearchBox",
        )
        forbidden.forEach { name ->
            assertFalse(
                panelSource.contains(name),
                "EditTrailPanel.kt still references removed JCheckBox field `$name`. " +
                "Iteration 7 moved this state onto the icon toolbar."
            )
        }
    }

    @Test
    fun `panel no longer constructs JCheckBox at all`() {
        assertFalse(
            panelSource.contains("JCheckBox("),
            "EditTrailPanel.kt still constructs JCheckBox controls — iteration 7 " +
            "replaced them with ToggleAction instances on the icon toolbar."
        )
    }

    @Test
    fun `panel does not declare text-button labels for the removed actions`() {
        // The recalculate / clear actions used to be JButton("Recalculate groups")
        // and JButton("Clear history"). Their text-button form is gone.
        val forbiddenButtonLabels = listOf(
            "JButton(\"Recalculate groups\")",
            "JButton(\"Clear history\")",
        )
        forbiddenButtonLabels.forEach { snippet ->
            assertFalse(
                panelSource.contains(snippet),
                "EditTrailPanel.kt still constructs the text button `$snippet` — " +
                "iteration 7 replaced it with an icon AnAction on the toolbar."
            )
        }
    }

    @Test
    fun `panel constructs the icon toolbar via ActionManager`() {
        // Tolerant match: the call may be split across lines for readability.
        val pattern = Regex("""ActionManager\.getInstance\(\)\s*\.\s*createActionToolbar""")
        assertTrue(
            pattern.containsMatchIn(panelSource),
            "EditTrailPanel.kt should build its icon toolbar via " +
            "ActionManager.getInstance().createActionToolbar(...) per design D1/D3."
        )
    }

    @Test
    fun `panel uses FileTypeChipLabel for chip rendering`() {
        // Iteration 7 task 4.1: chip rendering routed through FileTypeChipLabel
        // so the parens-free format is centralised.
        assertTrue(
            panelSource.contains("FileTypeChipLabel.format("),
            "EditTrailPanel.kt should render type chips via FileTypeChipLabel.format(...)."
        )
        assertTrue(
            panelSource.contains("FileTypeChipLabel.formatAll("),
            "EditTrailPanel.kt should render the All chip via FileTypeChipLabel.formatAll(...)."
        )
    }

    @Test
    fun `panel does not render chips with parenthesised count`() {
        // Defensive: catch a future regression that re-adds parens to chip labels.
        val parensPattern = Regex("""\$\{chip\.label\}\s*\(\s*\$\{chip\.count\}\s*\)""")
        assertFalse(
            parensPattern.containsMatchIn(panelSource),
            "Chip label rendering must not wrap the count in parentheses (iteration 7 §4.1)."
        )
    }
}

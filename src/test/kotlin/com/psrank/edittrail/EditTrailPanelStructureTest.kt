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
    fun `icon toolbar action group expands on background thread`() {
        assertTrue(
            panelSource.contains("object : DefaultActionGroup()") &&
                panelSource.contains("ActionUpdateThread.BGT"),
            "EditTrailPanel.kt should declare the icon toolbar group update thread as BGT " +
                "so ActionUpdater does not grab the EDT while expanding it."
        )
    }

    @Test
    fun `panel applies a small outer padding around the tool-window content`() {
        assertTrue(
            panelSource.contains("border = JBUI.Borders.empty(6)"),
            "EditTrailPanel.kt should apply a small outer empty border so the tool window content is not flush to its edges."
        )
    }

    @Test
    fun `panel uses FileTypeChipLabel for chip tooltip and All-chip text`() {
        // FileTypeChipLabel is now used for the chip tooltip text (full label + count)
        // and for the All-chip's visible text. The chip face shows icon + count.
        assertTrue(
            panelSource.contains("FileTypeChipLabel.format("),
            "EditTrailPanel.kt should still call FileTypeChipLabel.format(...) (now as the chip tooltip)."
        )
        assertTrue(
            panelSource.contains("FileTypeChipLabel.formatAll("),
            "EditTrailPanel.kt should render the All chip via FileTypeChipLabel.formatAll(...)."
        )
    }

    @Test
    fun `type chips are rendered with icons via FileTypeChipIcon`() {
        // The user-facing chip face must be icon-driven, not text-driven.
        assertTrue(
            panelSource.contains("FileTypeChipIcon.iconFor("),
            "EditTrailPanel.kt should resolve each chip's icon via FileTypeChipIcon.iconFor(...)."
        )
    }

    @Test
    fun `type chip text is the count only, not the label`() {
        // The chip's visible text should be just the count (the icon carries
        // the type identity). Look for ChipButton constructed with
        // chip.count.toString() as its text argument.
        val pattern = Regex("""ChipButton\s*\(\s*(?:[a-zA-Z]+\s*=\s*)?chip\.count\.toString\(\)""")
        assertTrue(
            pattern.containsMatchIn(panelSource),
            "Type chips should be constructed with chip.count.toString() as their visible text " +
            "and the icon supplied separately."
        )
    }

    @Test
    fun `chip bar uses the custom ChipButton class`() {
        // ChipButton is the slim, selection-painting toggle used for both the
        // All chip and each type chip.
        assertTrue(
            panelSource.contains("ChipButton("),
            "EditTrailPanel.kt should use ChipButton for chip rendering."
        )
    }

    @Test
    fun `panel does not render chips with parenthesised count`() {
        // Defensive: catch a future regression that re-adds parens to chip labels.
        val parensPattern = Regex("""\$\{chip\.label}\s*\(\s*\$\{chip\.count}\s*\)""")
        assertFalse(
            parensPattern.containsMatchIn(panelSource),
            "Chip label rendering must not wrap the count in parentheses (iteration 7 §4.1)."
        )
    }
}

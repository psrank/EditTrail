package com.psrank.edittrail

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * Sanity check for the AllIcons constants iteration 7 references.
 *
 * We pin the seven icon constants chosen in design.md §D2 against the
 * IntelliJ Platform classpath at test time so a typo or a removed icon is
 * caught before the plugin is launched. Each lookup uses reflection so we
 * don't pull AllIcons into production code paths from this test.
 */
class IconConstantsExistTest {

    private fun resolveIcon(holderFqn: String, fieldName: String): Any? {
        val cls = try { Class.forName(holderFqn) }
                  catch (e: ClassNotFoundException) {
                      throw AssertionError("AllIcons holder $holderFqn not found", e)
                  }
        val field = try { cls.getField(fieldName) }
                    catch (e: NoSuchFieldException) {
                        throw AssertionError("Field $fieldName not found on $holderFqn", e)
                    }
        return field.get(null)
    }

    @Test
    fun `Match path icon resolves`() {
        assertNotNull(resolveIcon("com.intellij.icons.AllIcons\$Nodes", "Folder"))
    }

    @Test
    fun `Match content icon resolves`() {
        assertNotNull(resolveIcon("com.intellij.icons.AllIcons\$Actions", "Find"))
    }

    @Test
    fun `Match pattern icon resolves`() {
        assertNotNull(resolveIcon("com.intellij.icons.AllIcons\$Actions", "Regex"))
    }

    @Test
    fun `Case sensitive icon resolves`() {
        assertNotNull(resolveIcon("com.intellij.icons.AllIcons\$Actions", "MatchCase"))
    }

    @Test
    fun `Global search icon resolves`() {
        assertNotNull(resolveIcon("com.intellij.icons.AllIcons\$Nodes", "Project"))
    }

    @Test
    fun `Recalculate icon resolves`() {
        assertNotNull(resolveIcon("com.intellij.icons.AllIcons\$Actions", "Refresh"))
    }

    @Test
    fun `Clear history icon resolves`() {
        assertNotNull(resolveIcon("com.intellij.icons.AllIcons\$General", "Remove"))
    }
}

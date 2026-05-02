package com.psrank.edittrail

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.project.Project
import com.psrank.edittrail.actions.EditTrailToolbarState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.lang.reflect.Proxy

/**
 * TDD-first tests for iteration 7's action classes.
 *
 * Each test loads a class by FQN and asserts that it is a subclass of the
 * appropriate IntelliJ Platform action base class. This avoids constructing
 * the action (which would need a Project) while still pinning the contract
 * that the implementation MUST satisfy.
 *
 * Stage 5 task 1.1–1.7 introduces these classes; until then every test in
 * this file fails with ClassNotFoundException.
 */
class IconActionClassesExistTest {

    private fun assertClassExtends(fqn: String, expectedSuperFqn: String) {
        val cls = try {
            Class.forName(fqn)
        } catch (e: ClassNotFoundException) {
            throw AssertionError("Expected class $fqn to be present on the test classpath", e)
        }
        assertNotNull(cls, "Class.forName($fqn) returned null")

        // Walk the superclass chain (IntelliJ ToggleAction / AnAction may sit
        // several levels above the concrete action class).
        var current: Class<*>? = cls
        while (current != null) {
            if (current.name == expectedSuperFqn) return
            current = current.superclass
        }
        throw AssertionError("Class $fqn does not extend $expectedSuperFqn (chain: ${chain(cls)})")
    }

    private fun chain(cls: Class<*>): String {
        val names = mutableListOf<String>()
        var c: Class<*>? = cls
        while (c != null) {
            names += c.name
            c = c.superclass
        }
        return names.joinToString(" -> ")
    }

    private fun testToolbarState(): EditTrailToolbarState {
        val project = Proxy.newProxyInstance(
            Project::class.java.classLoader,
            arrayOf(Project::class.java),
        ) { proxy, method, args ->
            when (method.name) {
                "equals" -> proxy === args?.firstOrNull()
                "hashCode" -> System.identityHashCode(proxy)
                "toString" -> "TestProject"
                else -> throw UnsupportedOperationException("Unexpected Project call in action update-thread test: ${method.name}")
            }
        } as Project

        return EditTrailToolbarState(
            project = project,
            onSearchChange = {},
            onClearHistory = {},
            onRecalculateGroups = {},
        )
    }

    private fun assertActionUsesBackgroundUpdates(fqn: String) {
        val cls = Class.forName(fqn).asSubclass(AnAction::class.java)
        val action = cls.getConstructor(EditTrailToolbarState::class.java)
            .newInstance(testToolbarState())
        assertEquals(ActionUpdateThread.BGT, action.actionUpdateThread, "$fqn should update on BGT")
    }

    @Test
    fun `MatchPathToggleAction exists and extends ToggleAction`() {
        assertClassExtends(
            "com.psrank.edittrail.actions.MatchPathToggleAction",
            "com.intellij.openapi.actionSystem.ToggleAction"
        )
    }

    @Test
    fun `MatchContentToggleAction exists and extends ToggleAction`() {
        assertClassExtends(
            "com.psrank.edittrail.actions.MatchContentToggleAction",
            "com.intellij.openapi.actionSystem.ToggleAction"
        )
    }

    @Test
    fun `MatchPatternToggleAction exists and extends ToggleAction`() {
        assertClassExtends(
            "com.psrank.edittrail.actions.MatchPatternToggleAction",
            "com.intellij.openapi.actionSystem.ToggleAction"
        )
    }

    @Test
    fun `CaseSensitiveToggleAction exists and extends ToggleAction`() {
        assertClassExtends(
            "com.psrank.edittrail.actions.CaseSensitiveToggleAction",
            "com.intellij.openapi.actionSystem.ToggleAction"
        )
    }

    @Test
    fun `GlobalSearchToggleAction exists and extends ToggleAction`() {
        assertClassExtends(
            "com.psrank.edittrail.actions.GlobalSearchToggleAction",
            "com.intellij.openapi.actionSystem.ToggleAction"
        )
    }

    @Test
    fun `RecalculateGroupsAction exists and extends AnAction`() {
        assertClassExtends(
            "com.psrank.edittrail.actions.RecalculateGroupsAction",
            "com.intellij.openapi.actionSystem.AnAction"
        )
    }

    @Test
    fun `ClearHistoryAction exists and extends AnAction`() {
        assertClassExtends(
            "com.psrank.edittrail.actions.ClearHistoryAction",
            "com.intellij.openapi.actionSystem.AnAction"
        )
    }

    @Test
    fun `every action class declares a non-empty static description`() {
        // Each action class MUST expose a non-empty description string used as
        // its tooltip. The implementation pattern: a `companion object` with
        // `const val DESCRIPTION = "..."`. Kotlin compiles a `const val` inside
        // a companion to a `public static final String DESCRIPTION` on the
        // outer class, so we look the field up there.
        val fqns = listOf(
            "com.psrank.edittrail.actions.MatchPathToggleAction",
            "com.psrank.edittrail.actions.MatchContentToggleAction",
            "com.psrank.edittrail.actions.MatchPatternToggleAction",
            "com.psrank.edittrail.actions.CaseSensitiveToggleAction",
            "com.psrank.edittrail.actions.GlobalSearchToggleAction",
            "com.psrank.edittrail.actions.RecalculateGroupsAction",
            "com.psrank.edittrail.actions.ClearHistoryAction",
        )
        val descriptions = fqns.map { fqn ->
            val cls = Class.forName(fqn)
            val field = try {
                cls.getDeclaredField("DESCRIPTION")
            } catch (e: NoSuchFieldException) {
                throw AssertionError("$fqn does not declare a static DESCRIPTION constant", e)
            }
            field.isAccessible = true
            val value = field.get(null) as? String
                ?: throw AssertionError("$fqn.DESCRIPTION is not a String")
            assertTrue(value.isNotBlank(), "$fqn.DESCRIPTION is blank")
            value
        }
        assertEquals(descriptions.size, descriptions.toSet().size, "Action descriptions are not unique: $descriptions")
    }

    @Test
    fun `every toolbar action declares background update thread`() {
        val fqns = listOf(
            "com.psrank.edittrail.actions.MatchPathToggleAction",
            "com.psrank.edittrail.actions.MatchContentToggleAction",
            "com.psrank.edittrail.actions.MatchPatternToggleAction",
            "com.psrank.edittrail.actions.CaseSensitiveToggleAction",
            "com.psrank.edittrail.actions.GlobalSearchToggleAction",
            "com.psrank.edittrail.actions.RecalculateGroupsAction",
            "com.psrank.edittrail.actions.ClearHistoryAction",
        )
        fqns.forEach(::assertActionUsesBackgroundUpdates)
    }
}

package com.psrank.edittrail

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * TDD-first tests for the redesigned brand icon (iteration 7).
 *
 * Until stage 5 task 5.2 / 5.3 ship the new SVG resources, every test in this
 * file fails because the resource lookups return null.
 *
 * The resources MUST live on the classpath at:
 *   - META-INF/pluginIcon.svg
 *   - META-INF/pluginIcon_dark.svg
 *
 * (JetBrains Marketplace and the IDE plugin chooser both look for these
 * exact paths.)
 */
class PluginIconResourcesTest {

    private fun resource(path: String) =
        PluginIconResourcesTest::class.java.classLoader.getResource(path)

    @Test
    fun `light theme plugin icon exists`() {
        val url = resource("META-INF/pluginIcon.svg")
        assertNotNull(url, "META-INF/pluginIcon.svg must exist on the classpath")
    }

    @Test
    fun `dark theme plugin icon exists`() {
        val url = resource("META-INF/pluginIcon_dark.svg")
        assertNotNull(url, "META-INF/pluginIcon_dark.svg must exist on the classpath")
    }

    @Test
    fun `light theme plugin icon is a non-empty SVG`() {
        val url = resource("META-INF/pluginIcon.svg")
            ?: throw AssertionError("META-INF/pluginIcon.svg missing")
        val text = url.readText()
        assertTrue(text.isNotBlank(), "pluginIcon.svg is blank")
        assertTrue(text.contains("<svg"), "pluginIcon.svg does not appear to contain an SVG root element")
    }

    @Test
    fun `dark theme plugin icon is a non-empty SVG`() {
        val url = resource("META-INF/pluginIcon_dark.svg")
            ?: throw AssertionError("META-INF/pluginIcon_dark.svg missing")
        val text = url.readText()
        assertTrue(text.isNotBlank(), "pluginIcon_dark.svg is blank")
        assertTrue(text.contains("<svg"), "pluginIcon_dark.svg does not appear to contain an SVG root element")
    }

    @Test
    fun `redesigned plugin icon must not use the four-dots-in-circle motif`() {
        // Defensive sentinel: the previous icon used four <circle> elements
        // arranged inside an outer <circle>. The new icon MUST move away from
        // that motif. Any new icon with five or more <circle> elements is
        // suspicious for this contract; flag it here.
        val url = resource("META-INF/pluginIcon.svg") ?: return
        val circles = Regex("<circle\\b").findAll(url.readText()).count()
        assertTrue(
            circles < 5,
            "pluginIcon.svg contains $circles <circle> elements — this resembles the retired four-dots-in-circle motif. Pick a different concept (trail / breadcrumb / edit flow)."
        )
    }
}

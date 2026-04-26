package com.psrank.edittrail

/**
 * Represents a single file in the EditTrail history.
 *
 * All fields are `var` to satisfy IntelliJ's XmlSerializer (requires mutable bean
 * with a no-arg constructor, which is satisfied by the default parameter values).
 */
data class FileHistoryEntry(
    var fileUrl: String = "",
    var fileName: String = "",
    var relativePath: String = "",
    var lastViewedAt: Long? = null,
    var lastEditedAt: Long? = null,
    var viewCount: Int = 0,
    var editCount: Int = 0,
    var exists: Boolean = true
)

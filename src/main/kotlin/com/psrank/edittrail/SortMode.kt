package com.psrank.edittrail

/** Sort mode for the EditTrail history list. */
enum class SortMode {
    /** Sort by most recently edited (files with no edit timestamp appear last). */
    LAST_EDITED,

    /** Sort by most recently viewed. */
    LAST_VIEWED
}

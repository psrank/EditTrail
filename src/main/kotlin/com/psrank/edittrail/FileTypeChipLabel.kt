package com.psrank.edittrail

/**
 * Chip-label formatting helper.
 *
 * Iteration 7 dropped the parentheses around chip counts:
 *   `C# (8)` → `C# 8`
 *
 * Centralising the formatter here keeps the shape testable without spinning up
 * a panel and gives every chip renderer a single source of truth.
 */
object FileTypeChipLabel {

    /** Formats a normal type chip, e.g. `C# 8`. */
    fun format(chip: FileTypeChip): String = "${chip.label} ${chip.count}"

    /** Formats the special `All` chip, e.g. `All 42`. */
    fun formatAll(totalCount: Int): String = "All $totalCount"
}

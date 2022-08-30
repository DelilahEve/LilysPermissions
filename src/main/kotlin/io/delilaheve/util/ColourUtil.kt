package io.delilaheve.util

import org.bukkit.ChatColor

/**
 * Colour utilities to ease [ChatColor] management
 */
object ColourUtil {

    /**
     * Colourise this [String] replacing chat colour codes
     */
    fun String.colourise() = ChatColor.translateAlternateColorCodes(
        '&',
        this
    )

}

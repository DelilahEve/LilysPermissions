package io.delilaheve.util

import org.bukkit.ChatColor

object ColourUtil {

    fun String.colourise() = ChatColor.translateAlternateColorCodes(
        '&',
        this
    )

}

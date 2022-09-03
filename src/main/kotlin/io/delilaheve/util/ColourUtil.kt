package io.delilaheve.util

import org.bukkit.ChatColor
import java.awt.Color

/**
 * Colour utilities to ease [ChatColor] management
 */
object ColourUtil {

    // Character we expect when a colour code is defined
    private const val CHAT_COLOUR_CHAR = '&'

    // Hex code patterns
    private val fullHexRegex = "#[a-fA-F0-9]{6}".toRegex()
    private val shortHexRegex = "#[a-fA-F0-9]{3}".toRegex()

    /**
     * Colourise this [String] replacing chat colour codes
     */
    fun String.colourise() = try {
        colouriseBungeeApi()
    } catch (e: Exception) {
        colouriseBukkitApi()
    }

    /**
     * Primary colourisation using Bungee's api
     *
     * From research, this function will likely throw an error on Bukkit
     * servers, which is why we have a fallback for Bukkit's api
     */
    private fun String.colouriseBungeeApi(): String {
        var result = this
        // translate '&' colour codes
        result = net.md_5.bungee.api.ChatColor.translateAlternateColorCodes(
            CHAT_COLOUR_CHAR,
            result
        )
        // translate 6 digit hex colour codes
        fullHexRegex.matchEntire(result)
            ?.let { result = result.replaceColours(it) }
        // translate 3 digit hex colour codes
        shortHexRegex.matchEntire(result)
            ?.let { result = result.replaceColours(it) }
        return result
    }

    /**
     * Replace hex colours found in this [String] through the given [match] result
     */
    private fun String.replaceColours(match: MatchResult): String {
        var result = this
        match.groups.forEach {
            it?.let { result = result.replaceColour(it) }
        }
        return result
    }

    /**
     * Replace hex colour in this [String] found in the given match [group]
     */
    private fun String.replaceColour(group: MatchGroup): String {
        val colour = Color.decode(group.value)
        val colourString = net.md_5.bungee.api.ChatColor.of(colour).toString()
        return replace(group.value, colourString, false)
    }

    /**
     * Fallback colourisation using Bukkit's api
     */
    private fun String.colouriseBukkitApi(): String = ChatColor.translateAlternateColorCodes(
        CHAT_COLOUR_CHAR,
        this
    )

}

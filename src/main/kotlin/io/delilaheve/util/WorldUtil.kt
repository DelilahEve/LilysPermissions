package io.delilaheve.util

import io.delilaheve.LilysPermissions
import org.bukkit.World

/**
 * Set of functions intended to ease [World] management
 */
object WorldUtil {

    /**
     * Get a list of all [World]s registered on the current server
     */
    fun getAllWorlds() = LilysPermissions.instance
        ?.server
        ?.worlds
        ?: emptyList()

}

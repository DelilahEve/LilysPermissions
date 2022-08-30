package io.delilaheve.util

import io.delilaheve.LilysPermissions.Companion.PERMISSIONS_FILE
import io.delilaheve.data.Group
import io.delilaheve.exception.DefaultGroupMissing
import io.delilaheve.util.YamlUtil.PATH_GROUPS
import io.delilaheve.util.YamlUtil.readGroup
import org.bukkit.World

/**
 * Utility functions intended to ease [Group] management
 */
object GroupUtil {

    // String to search for to indicate a default group is globally applied
    private const val DEFAULT_GLOBAL_KEY = "true"

    /**
     * Get a list of all defined [Group]s in the [PERMISSIONS_FILE]
     *
     * This list will, by default, be ordered ascending
     */
    private fun allGroups(): List<Group> = YamlUtil.getFile(PERMISSIONS_FILE)
        ?.getConfigurationSection(PATH_GROUPS)
        ?.getKeys(false)
        ?.mapNotNull { it.asGroup() }
        ?: emptyList()

    /**
     * Get the default [Group] most relevant to [world]
     *
     * throws [DefaultGroupMissing] if none are defined
     */
    fun defaultGroup(
        world: World
    ): Group {
        val allGroups = allGroups()
        return allGroups.firstOrNull { it.default.equals(world.name, true) }
            ?: allGroups.firstOrNull { it.default.equals(DEFAULT_GLOBAL_KEY, true) }
            ?: throw DefaultGroupMissing()
    }

    /**
     * Attempt to pull [Group] information from this [String] group name
     */
    fun String.asGroup(): Group? = YamlUtil.getFile(PERMISSIONS_FILE)
        ?.readGroup(this)

    /**
     * Get the [Group]s relevant in [world]
     */
    fun List<Group>.relevantIn(world: World) = filter {
        it.worlds.isEmpty() || it.worlds.any { w -> w == world.name }
    }

    /**
     * Get all inherited permissions for this [Group]
     */
    private fun Group.inheritedPermissions(
        world: World
    ): List<String> {
        val inheritGroups = inherit.mapNotNull { it.asGroup() }
        return inheritGroups.allPermissions(world)
    }

    /**
     * Get all permissions for these [Group]s including their inherited permissions
     */
    fun List<Group>.allPermissions(
        world: World
    ): List<String> = flatMap { it.permissionsFor(world) }
        .toMutableList()
        .apply {
           val inherited = this@allPermissions.flatMap { it.inheritedPermissions(world) }
            addAll(inherited)
        }
        .distinct()

    /**
     * Get the highest ranked [Group] in this [List]
     */
    fun List<Group>.highestRanked(): Group? = allGroups().lastOrNull { it in this }
        ?: firstOrNull()

}

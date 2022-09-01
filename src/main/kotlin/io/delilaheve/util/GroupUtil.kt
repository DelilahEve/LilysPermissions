package io.delilaheve.util

import io.delilaheve.LilysPermissions.Companion.PERMISSIONS_FILE
import io.delilaheve.data.Group
import io.delilaheve.exception.DefaultGroupMissing
import io.delilaheve.util.PermissionsUtil.WILDCARD_PERMISSION
import io.delilaheve.util.YamlUtil.PATH_GROUPS
import io.delilaheve.util.YamlUtil.readGroup
import org.bukkit.World

/**
 * Utility functions intended to ease [Group] management
 */
object GroupUtil {

    // String to search for to indicate a default group is globally applied
    const val DEFAULT_GLOBAL_KEY = "true"

    /**
     * Get a list of all defined [Group]s in the [PERMISSIONS_FILE]
     *
     * This list will, by default, be ordered ascending
     */
    fun allGroups(): List<Group> = YamlUtil.getFile(PERMISSIONS_FILE)
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
     * Get a list of all default [Group]s
     */
    fun allDefaults(): List<Group> {
        val allWorlds = WorldUtil.getAllWorlds()
            .map { it.name }
        return allGroups().filter {
            it.default in allWorlds || it.default == DEFAULT_GLOBAL_KEY
        }
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
     * Get all inherited denied permissions for this [Group]
     */
    private fun Group.inheritedDeniedPermissions(
        world: World
    ): List<String> {
        val inheritedGroups = inherit.mapNotNull { it.asGroup() }
        return inheritedGroups.allDeniedPermissions(world)
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
            addImplicitChildren()
        }
        .distinct()

    /**
     * Get all denied permissions for these [Group]s including their inherited denied permissions
     */
    fun List<Group>.allDeniedPermissions(
        world: World
    ): List<String> = flatMap { it.deniedPermissionsFor(world) }
        .toMutableList()
        .apply {
            val inherited = this@allDeniedPermissions.flatMap { it.inheritedDeniedPermissions(world) }
            addAll(inherited)
            addImplicitChildren()
        }

    /**
     * Loop all items to detect any wildcards and add their child permissions
     * because not all plugins support a wildcard permission, but we want to offer
     * it for as many as possible.
     *
     * This doesn't guarantee we will successfully grab all permissions that should
     * be children, but it does cover a fair number.
     */
    private fun MutableList<String>.addImplicitChildren() {
        val findChildrenFor = mutableListOf<String>()
        forEach {
            // We want to be careful not to check for descendants of a true wildcard, as we won't find any
            if (it != WILDCARD_PERMISSION && it.endsWith(WILDCARD_PERMISSION)) {
                findChildrenFor.add(it)
            }
        }
        findChildrenFor.forEach { addAll(PermissionsUtil.descendingPermissions(it)) }
    }

    /**
     * Get the highest ranked [Group] in this [List]
     */
    fun List<Group>.highestRanked(): Group? = allGroups().lastOrNull { it in this }
        ?: firstOrNull()

}

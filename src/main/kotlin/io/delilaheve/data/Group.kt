package io.delilaheve.data

import org.bukkit.World

/**
 * Model representing a permissions group.
 *
 * @param name the group's name.
 * @param default Can be "true" or a world name, determines if this group is a default group.
 * @param prefix Prefix string for members of this group.
 * @param suffix Suffix string for members of this group.
 * @param inherit List of groups this one should inherit [permissions] from.
 * @param worlds List of world names this group is applied to. Applies to all if empty.
 * @param permissions List of permission nodes granted to members of this group.
 * @param denyPermissions List of permission nodes denied to members of this group.
 * @param worldOverrides Map of [GroupWorldOverride]s that may change a member's prefix,
 *                       suffix, or permissions when they are in a world defined here.
 */
data class Group(
    val name: String,
    val default: String,
    val prefix: String,
    val suffix: String,
    val inherit: List<String>,
    val worlds: List<String>,
    val permissions: List<String>,
    val denyPermissions: List<String>,
    val worldOverrides: Map<String, GroupWorldOverride>
) {

    /**
     * Determine the prefix to use in the given [world]
     */
    fun prefixFor(
        world: World
    ) = worldOverrides[world.name]?.prefix
        ?.takeIf { it.isNotEmpty() }
        ?: prefix

    /**
     * Determine the suffix to use in the given [world]
     */
    fun suffixFor(
        world: World
    ) = worldOverrides[world.name]?.suffix
        ?.takeIf { it.isNotEmpty() }
        ?: suffix

    /**
     * Determine the permissions to use in the given [world]
     */
    fun permissionsFor(
        world: World
    ) = worldOverrides[world.name]?.permissions
        ?.toMutableList()
        ?.apply { addAll(permissions) }
        ?.distinct()
        ?: permissions

    /**
     * Determine the denied permissions to use in the given [world]
     */
    fun deniedPermissionsFor(
        world: World
    ) = worldOverrides[world.name]?.permissions
        ?.toMutableList()
        ?.apply { addAll(denyPermissions) }
        ?.distinct()
        ?: denyPermissions

    /**
     * Determine if this group is equivalent to [other]
     */
    override fun equals(other: Any?): Boolean {
        return if (other !is Group) {
            false
        } else {
            name.equals(other.name, true)
        }
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

}

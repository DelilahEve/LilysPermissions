package io.delilaheve.util

import io.delilaheve.ConfigOptions
import io.delilaheve.ConfigOptions.PrefixSuffixModes.COMBINE
import io.delilaheve.ConfigOptions.PrefixSuffixModes.OVERRIDE
import io.delilaheve.LilysPermissions.Companion.USERS_FILE
import io.delilaheve.data.Group
import io.delilaheve.data.User
import io.delilaheve.manager.PlayerManager
import io.delilaheve.util.ColourUtil.colourise
import io.delilaheve.util.GroupUtil.allPermissions
import io.delilaheve.util.GroupUtil.asGroup
import io.delilaheve.util.GroupUtil.highestRanked
import io.delilaheve.util.GroupUtil.relevantIn
import io.delilaheve.util.YamlUtil.readUser
import io.delilaheve.util.YamlUtil.setUserGroupNames
import io.delilaheve.util.YamlUtil.trySave
import io.delilaheve.util.YamlUtil.userGroupNames
import io.delilaheve.util.YamlUtil.writeUser
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionAttachment

/**
 * Set of functions and variables intended to ease [Player] management
 */
object PlayerUtil {

    /**
     * Get this [Player]'s permissions overrides, we include the current [world]
     * in-case this is their first join, or they have no prior record
     */
    fun Player.getUserPermissions(
        world: World
    ) = YamlUtil.getFile(USERS_FILE)
            ?.readUser(uniqueId, world)

    /**
     * Get this [Player]s highest ranking group relevant to the given [world]
     */
    fun Player.highestGroup(
        world: World
    ) = getUserPermissions(world)
        ?.relevantGroups(world)
        ?.highestRanked()

    /**
     * Find this [Player]'s most relevant group for [world]
     */
    private fun User.relevantGroups(
        world: World
    ): List<Group> = groups().relevantIn(world)

    /**
     * Find all groups for this [Player]
     */
    fun User.groups(): List<Group> = groups.mapNotNull { it.asGroup() }

    /**
     * Set the active permissions for this [Player] per the current configuration
     */
    fun Player.setActivePermissions(
        attachment: PermissionAttachment
    ) {
        val user = getUserPermissions(world) ?: return
        val groups = user.relevantGroups(world)
        val permissions = mutableListOf<String>()
            .apply {
                addAll(user.permissions)
                addAll(groups.allPermissions(world))
            }
            .distinct()
            .toMutableList()
        // wildcard permission provides all registered permissions
        if (permissions.contains("*")) {
            permissions.addAll(PermissionsUtil.allPermissionStrings)
        }
        permissions.forEach { attachment.setPermission(it, true) }
        // ensure players has an up-to-date list of commands
        updateCommands()
    }

    /**
     * Update this [Player]'s display name as defined in configurations
     */
    fun Player.updateDisplayName() {
        val user = getUserPermissions(world) ?: return
        val group = highestGroup(world) ?: return
        var prefix = when (ConfigOptions.prefixMode) {
            COMBINE -> "${user.prefix}${group.prefixFor(world)}"
            OVERRIDE -> group.prefixFor(world)
                .takeIf { user.prefix.isEmpty() }
                ?: user.prefix
        }
        var suffix = when (ConfigOptions.suffixMode) {
            COMBINE -> "${user.suffix}${group.suffixFor(world)}"
            OVERRIDE -> group.suffixFor(world)
                .takeIf { user.suffix.isEmpty() }
                ?: user.suffix
        }
        if (ConfigOptions.spaceAfterPrefix) {
            prefix = "$prefix "
        }
        if (ConfigOptions.spaceBeforeSuffix) {
            suffix = " $suffix"
        }
        setDisplayName("$prefix$name$suffix".colourise())
    }

    /**
     * Add all [groupNames] to this [Player]'s groups
     */
    fun Player.addGroups(
        groupNames: List<String>
    ): Boolean = YamlUtil.getFile(USERS_FILE)?.let {
        val newGroupNames = it.userGroupNames(uniqueId)
            .toMutableList()
            .apply { addAll(groupNames) }
            .distinct()
        it.setUserGroupNames(uniqueId, newGroupNames)
        val result = it.trySave(USERS_FILE)
        PlayerManager.updatePlayer(this)
        result
    } ?: false

    /**
     * Attempt to set a [Player]'s [groups], overwriting any previous groups
     */
    fun Player.setGroups(
        groupNames: List<String>
    ): Boolean = YamlUtil.getFile(USERS_FILE)?.let {
        it.setUserGroupNames(uniqueId, groupNames)
        val result = it.trySave(USERS_FILE)
        PlayerManager.updatePlayer(this)
        result
    } ?: false

    /**
     * Save this [User] to the [USERS_FILE]
     */
    fun User.save() = YamlUtil.getFile(USERS_FILE)?.let {
        it.writeUser(this)
        it.trySave(USERS_FILE)
    } ?: false

}

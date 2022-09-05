package io.delilaheve.util

import io.delilaheve.ConfigOptions
import io.delilaheve.ConfigOptions.PrefixSuffixModes.COMBINE
import io.delilaheve.ConfigOptions.PrefixSuffixModes.OVERRIDE
import io.delilaheve.LilysPermissions.Companion.USERS_FILE
import io.delilaheve.data.Group
import io.delilaheve.data.User
import io.delilaheve.manager.PlayerManager
import io.delilaheve.util.ColourUtil.colourise
import io.delilaheve.util.GroupUtil.allDeniedPermissions
import io.delilaheve.util.GroupUtil.allPermissions
import io.delilaheve.util.GroupUtil.asGroup
import io.delilaheve.util.GroupUtil.highestRanked
import io.delilaheve.util.GroupUtil.relevantIn
import io.delilaheve.util.PermissionsUtil.WILDCARD_PERMISSION
import io.delilaheve.util.YamlUtil.readUser
import io.delilaheve.util.YamlUtil.setUserGroupNames
import io.delilaheve.util.YamlUtil.trySave
import io.delilaheve.util.YamlUtil.userGroupNames
import io.delilaheve.util.YamlUtil.writeUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionAttachment

/**
 * Set of functions and variables intended to ease [Player] management
 */
object PlayerUtil {

    /**
     * When a server gets busy, the permissions calculations can take quite a bit of
     * cpu power, especially if there are hundreds or thousands of defined permission
     * nodes. As such, we're best off to move the calculations to another thread, then
     * set them after the calculation is complete.
     */
    @OptIn(DelicateCoroutinesApi::class)
    val permissionsScope by lazy {
        CoroutineScope(newSingleThreadContext("permissionsScope"))
    }

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
        permissionsScope.launch {
            val user = getUserPermissions(world) ?: return@launch
            val groups = user.relevantGroups(world)
            val denyPermissions = mutableListOf<String>()
                .apply {
                    addAll(user.denyPermissions)
                    addAll(groups.allDeniedPermissions(world))
                }
            mutableListOf<String>()
                .apply {
                    addAll(user.permissions)
                    addAll(groups.allPermissions(world))
                    if (contains(WILDCARD_PERMISSION)) {
                        removeIf { it == WILDCARD_PERMISSION }
                        addAll(PermissionsUtil.allPermissionStrings)
                    }
                    removeAll { it in denyPermissions }
                }
                .forEach { attachment.setPermission(it) }
            updateCommands()
        }
    }

    /**
     * Remove active permissions from this [PermissionAttachment]
     */
    fun PermissionAttachment.removeActivePermissions() {
        permissionsScope.launch {
            permissions.forEach { (permission, _) ->
                unsetPermission(permission)
            }
        }
    }

    /**
     * Shorthand to set a permission because for some stupid reason the "value"
     * boolean on setPermission gets fucking ignored when checking with hasPermission?? Imagine.
     */
    private fun PermissionAttachment.setPermission(
        permission: String
    ) = setPermission(permission, true)

    /**
     * Update this [Player]'s display name as defined in configurations
     */
    fun Player.updateDisplayNames() {
        permissionsScope.launch {
            val user = getUserPermissions(world) ?: return@launch
            val group = highestGroup(world) ?: return@launch
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
            val displayNameAs = "$prefix$name$suffix".colourise()
            if (ConfigOptions.chatFormatting) {
                setDisplayName(displayNameAs)
            }
            if (ConfigOptions.tabFormatting) {
                val restrictedAndInGroup = ConfigOptions.tabRestrictions
                        && group.name in ConfigOptions.tabListGroups
                if (!ConfigOptions.tabRestrictions || restrictedAndInGroup) {
                    setPlayerListName(displayNameAs)
                }
            }
        }
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

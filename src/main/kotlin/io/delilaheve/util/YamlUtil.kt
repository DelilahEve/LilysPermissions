package io.delilaheve.util

import io.delilaheve.LilysPermissions
import io.delilaheve.LilysPermissions.Companion.USERS_FILE
import io.delilaheve.data.Group
import io.delilaheve.data.GroupWorldOverride
import io.delilaheve.data.Ladder
import io.delilaheve.data.User
import io.delilaheve.exception.DefaultGroupMissing
import org.bukkit.World
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.*

/**
 * YAML utilities intended to ease reading of YAML configurations
 */
object YamlUtil {

    // Common configuration section paths
    const val PATH_GROUPS = "groups"
    private const val PATH_USERS = "users"
    const val PATH_LADDERS = "ladders"
    private const val PATH_DEFAULT = "default"
    private const val PATH_PREFIX = "prefix"
    private const val PATH_SUFFIX = "suffix"
    private const val PATH_INHERIT = "inherit"
    private const val PATH_WORLDS = "worlds"
    private const val PATH_PERMISSIONS = "permissions"
    // Default value for missing strings
    private const val DEFAULT_STRING = ""

    /**
     * Try to get a [YamlConfiguration] by name
     */
    fun getFile(name: String): YamlConfiguration? {
        val file = LilysPermissions.instance
            ?.let { File(it.dataFolder, name) }
            ?: return null
        return try {
            YamlConfiguration.loadConfiguration(file)
        } catch (e: Exception) {
            LogUtil.warn("$name contains invalid YAML, failed to load.")
            null
        }
    }

    /**
     * Try to save this [YamlConfiguration]
     */
    fun YamlConfiguration.trySave(
        name: String
    ): Boolean = try {
        LilysPermissions.instance?.let {
            val file = File(it.dataFolder, name)
            save(file)
            true
        }
        false
    } catch (e: Exception) {
        false
    }

    /**
     * Read a [Group] from this [YamlConfiguration]
     */
    fun YamlConfiguration.readGroup(
        groupName: String
    ): Group? {
        return getConfigurationSection("$PATH_GROUPS.$groupName")?.let {
            Group(
                name = groupName,
                default = it.getString(PATH_DEFAULT) ?: DEFAULT_STRING,
                prefix = it.getString(PATH_PREFIX) ?: DEFAULT_STRING,
                suffix = it.getString(PATH_SUFFIX) ?: DEFAULT_STRING,
                inherit = it.getStringList(PATH_INHERIT),
                worlds = it.getStringList(PATH_WORLDS),
                permissions = it.getStringList(PATH_PERMISSIONS),
                worldOverrides = findWorldOverrides()
            )
        }
    }

    /**
     * Find any [GroupWorldOverride]s in this [ConfigurationSection] and return them
     * as a [Map] of [String] world names, and [GroupWorldOverride]s
     */
    private fun ConfigurationSection.findWorldOverrides(): Map<String, GroupWorldOverride> {
        val result = mutableMapOf<String, GroupWorldOverride>()
        val worlds = WorldUtil.getAllWorlds()
        worlds.forEach { world ->
            getConfigurationSection(world.name)?.let {
                result[world.name] = it.readWorldOverride()
            }
        }
        return result
    }

    /**
     * Read a [GroupWorldOverride] from this [ConfigurationSection]
     */
    private fun ConfigurationSection.readWorldOverride() = GroupWorldOverride(
        prefix = getString(PATH_PREFIX) ?: DEFAULT_STRING,
        suffix = getString(PATH_SUFFIX) ?: DEFAULT_STRING,
        permissions = getStringList(PATH_PERMISSIONS)
    )

    /**
     * Read the [User] with matching [uuid] from this [YamlConfiguration]. If the
     * user isn't found, return default [User] permissions object
     */
    fun YamlConfiguration.readUser(
        uuid: UUID,
        world: World
    ): User = getConfigurationSection("$PATH_USERS.$uuid")?.let {
        User(
            uuid = uuid,
            prefix = getString(PATH_PREFIX) ?: DEFAULT_STRING,
            suffix = getString(PATH_SUFFIX) ?: DEFAULT_STRING,
            groups = getStringList(PATH_GROUPS),
            permissions = getStringList(PATH_PERMISSIONS)
        )
    } ?: User(
        uuid = uuid,
        prefix = DEFAULT_STRING,
        suffix = DEFAULT_STRING,
        groups = listOf(GroupUtil.defaultGroup(world).name),
        permissions = emptyList()
    )

    /**
     * Save a user to this [YamlConfiguration]
     */
    fun YamlConfiguration.writeUser(
        user: User
    ) {
        val basePath = "$PATH_USERS.${user.uuid}"
        set("$basePath.$PATH_PREFIX", user.prefix)
        set("$basePath.$PATH_SUFFIX", user.suffix)
        set("$basePath.$PATH_GROUPS", user.groups)
        set("$basePath.$PATH_PERMISSIONS", user.permissions)
    }

    /**
     * Get a list of group names from this [YamlConfiguration] for the user with the given [uuid]
     */
    fun YamlConfiguration.userGroupNames(
        uuid: UUID
    ): List<String> = getStringList(
        "$PATH_USERS.$uuid.$PATH_GROUPS"
    )

    /**
     * Set the list of [groupNames] for this [YamlConfiguration] for the user with the given [uuid]
     */
    fun YamlConfiguration.setUserGroupNames(
        uuid: UUID,
        groupNames: List<String>
    ) {
        set("$PATH_USERS.$uuid.$PATH_GROUPS", groupNames)
    }

    /**
     * Try to read a ladder by [name] from this [YamlConfiguration]
     */
    fun YamlConfiguration.readLadder(
        name: String
    ): Ladder = Ladder(
        name = name,
        groups = getStringList("$PATH_LADDERS.$name")
    )

}

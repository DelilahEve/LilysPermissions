package io.delilaheve

import io.delilaheve.handler.PlayerJoinHandler
import io.delilaheve.handler.PlayerLeaveHandler
import io.delilaheve.handler.WorldChangeHandler
import io.delilaheve.manager.CommandManager
import io.delilaheve.manager.PlayerManager
import io.delilaheve.util.GroupUtil
import io.delilaheve.util.GroupUtil.DEFAULT_GLOBAL_KEY
import io.delilaheve.util.LogUtil
import io.delilaheve.util.YamlUtil
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

/**
 * Plugin main class
 */
@Suppress("unused") // Class is loaded by plugin manager
class LilysPermissions : JavaPlugin() {

    companion object {
        // Additional configuration files
        const val PERMISSIONS_FILE = "permissions.yml"
        const val USERS_FILE = "users.yml"

        // Warning for when a configuration might be in error, but the plugin can still function in some capacity
        private const val WARNING_NOT_INTENDED: String
            = "If this is not intended you should check your $PERMISSIONS_FILE formatting."

        // Plugin instance
        var instance: LilysPermissions? = null
            private set
    }

    /**
     * Handle plugin enable event
     */
    override fun onEnable() {
        instance = this
        saveDefaults()
        registerHandlers()
        startupCheck()
        // If there are already players online when we enable, we should be attaching to them
        if (server.onlinePlayers.isNotEmpty()) {
            PlayerManager.attachAllOnline()
        }
    }

    /**
     * Handle plugin disable event
     */
    override fun onDisable() {
        PlayerManager.detachAll()
        instance = null
    }

    /**
     * Handle commands registered to the plugin
     */
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (!command.testPermission(sender)) { return false }
        return CommandManager.routeCommand(sender, command, args)
    }

    /**
     * Save default files to plugin config folder
     */
    private fun saveDefaults() {
        saveDefaultConfig()
        saveResource(PERMISSIONS_FILE, false)
        saveResource(USERS_FILE, false)
    }

    /**
     * Register event handlers
     */
    private fun registerHandlers() {
        listOf(
            PlayerJoinHandler(),
            PlayerLeaveHandler(),
            WorldChangeHandler()
        ).forEach { server.pluginManager.registerEvents(it, this) }
    }

    /**
     * Check configurations for # of groups defined, # of users known,
     * and validate at least on default group exists
     */
    private fun startupCheck() {
        // Check the # of groups defined and log a warning if none are found
        val groups = GroupUtil.allGroups()
        if (groups.isEmpty()) {
            LogUtil.warn("No permissions groups are defined. $WARNING_NOT_INTENDED")
        } else {
            LogUtil.info("${groups.size} groups defined")
        }
        // Check the # of default groups and whether a global default is defined
        val defaultGroups = GroupUtil.allDefaults()
        val haveGlobalDefault = defaultGroups.any {
            it.default.equals(DEFAULT_GLOBAL_KEY, true)
        }
        when {
            defaultGroups.isEmpty() -> LogUtil.warn("No default group has been defined. $WARNING_NOT_INTENDED")
            !haveGlobalDefault -> LogUtil.warn("No global default group has been defined. $WARNING_NOT_INTENDED")
            else -> {
                val defaultsString = defaultGroups.joinToString(", ") {
                    val forWorld = if (it.default.equals(DEFAULT_GLOBAL_KEY, true)) {
                        "global"
                    } else {
                        it.default
                    }
                    "${it.name}: $forWorld"
                }
                LogUtil.info("${defaultGroups.size} default groups defined ($defaultsString)")
            }
        }
        // loading the users file will log a warning if there are errors, we don't need to log anything here
        YamlUtil.getFile(USERS_FILE)
    }

}

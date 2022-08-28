package io.delilaheve

import io.delilaheve.handler.PlayerJoinHandler
import io.delilaheve.handler.PlayerLeaveHandler
import io.delilaheve.manager.CommandManager
import io.delilaheve.manager.PlayerManager
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
            PlayerLeaveHandler()
        ).forEach { server.pluginManager.registerEvents(it, this) }
    }



}

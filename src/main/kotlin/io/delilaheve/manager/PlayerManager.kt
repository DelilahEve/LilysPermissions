package io.delilaheve.manager

import io.delilaheve.ConfigOptions
import io.delilaheve.LilysPermissions
import io.delilaheve.util.PlayerUtil.setActivePermissions
import io.delilaheve.util.PlayerUtil.updateDisplayName
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionAttachment
import org.bukkit.plugin.Plugin

/**
 * Singleton to manage player attachment interactions
 */
object PlayerManager {

    // Plugin instance accessor
    private val pluginInstance: Plugin?
        get() = LilysPermissions.instance

    // All attachments currently held by the plugin
    private val attachments = mutableMapOf<Player, PermissionAttachment>()

    /**
     * Attach to [player]
     */
    fun attachPlayer(player: Player) {
        pluginInstance?.let { player.addAttachment(it) }
            ?.let { attachments[player] = it }
        attachments[player]?.let { player.setActivePermissions(it) }
        if (ConfigOptions.chatFormatting) {
            player.updateDisplayName()
        }
    }

    /**
     * Detach from [player]
     */
    fun detachPlayer(player: Player) {
        attachments[player]?.let {
            it.permissions.forEach { (permission, granted) ->
                if (granted) {
                    it.unsetPermission(permission)
                }
            }
            it.remove()
        }
    }

    /**
     * Attach to all already online players
     */
    fun attachAllOnline() {
        pluginInstance?.server
            ?.onlinePlayers
            ?.forEach { attachPlayer(it) }
    }

    /**
     * Detach from all players we have an attachment for
     */
    fun detachAll() {
        attachments.forEach { (player, _) ->
            detachPlayer(player)
        }
    }

    /**
     * Update permissions for all attachments
     */
    fun updateAttachments() {
        attachments.forEach { (player, attachment) ->
            attachment.permissions.forEach { (permission, granted) ->
                if (granted) {
                    attachment.unsetPermission(permission)
                }
            }
            player.setActivePermissions(attachment)
            if (ConfigOptions.chatFormatting) {
                player.updateDisplayName()
            }
        }
    }

    /**
     * Update permissions for the given [player]
     */
    fun updatePlayer(player: Player) {
        attachments[player]?.let {
            it.permissions.forEach { (permission, granted) ->
                if (granted) {
                    it.unsetPermission(permission)
                }
            }
            player.setActivePermissions(it)
            if (ConfigOptions.chatFormatting) {
                player.updateDisplayName()
            }
        }
    }

}

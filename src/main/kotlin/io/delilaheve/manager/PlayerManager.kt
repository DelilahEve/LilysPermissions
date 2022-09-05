package io.delilaheve.manager

import io.delilaheve.LilysPermissions
import io.delilaheve.util.PlayerUtil.removeActivePermissions
import io.delilaheve.util.PlayerUtil.setActivePermissions
import io.delilaheve.util.PlayerUtil.updateDisplayNames
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
        player.updateDisplayNames()
    }

    /**
     * Detach from [player]
     */
    fun detachPlayer(player: Player) {
        attachments[player]?.let {
             it.removeActivePermissions()
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
            attachment.removeActivePermissions()
            player.setActivePermissions(attachment)
            player.updateDisplayNames()
        }
    }

    /**
     * Update permissions for the given [player]
     */
    fun updatePlayer(player: Player) {
        attachments[player]?.let {
            it.removeActivePermissions()
            player.setActivePermissions(it)
            player.updateDisplayNames()
        }
    }

}

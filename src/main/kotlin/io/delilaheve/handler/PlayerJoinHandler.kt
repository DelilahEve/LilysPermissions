package io.delilaheve.handler

import io.delilaheve.ConfigOptions
import io.delilaheve.exception.DefaultGroupMissing
import io.delilaheve.manager.PlayerManager
import io.delilaheve.util.LogUtil
import io.delilaheve.util.PlayerUtil.getUserPermissions
import io.delilaheve.util.PlayerUtil.save
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.HIGHEST
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

/**
 * Handle player join events
 */
class PlayerJoinHandler : Listener {

    /**
     * Player join logic
     */
    @EventHandler(priority = HIGHEST)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        try {
            PlayerManager.attachPlayer(event.player)
        } catch (e: Exception) {
            if (e is DefaultGroupMissing) {
                e.message?.let { LogUtil.warn(it) }
            }
        }
        if (ConfigOptions.saveAll) {
            event.player
                .getUserPermissions(event.player.world)
                ?.save()
        }
    }

}

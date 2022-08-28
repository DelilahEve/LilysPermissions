package io.delilaheve.handler

import io.delilaheve.manager.PlayerManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * Handle player leave events
 */
class PlayerLeaveHandler : Listener {

    /**
     * Player quit logic
     */
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        PlayerManager.detachPlayer(event.player)
    }

    /**
     * Player kicked logic
     */
    @EventHandler
    fun onPlayerKicked(event: PlayerKickEvent) {
        PlayerManager.detachPlayer(event.player)
    }

}

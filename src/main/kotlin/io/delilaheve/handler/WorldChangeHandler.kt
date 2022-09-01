package io.delilaheve.handler

import io.delilaheve.exception.DefaultGroupMissing
import io.delilaheve.manager.PlayerManager
import io.delilaheve.util.LogUtil
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent

/**
 * Handle player change world events
 */
class WorldChangeHandler : Listener {

    /**
     * Player world change logic
     */
    @EventHandler
    fun onPlayerChangeWorld(event: PlayerChangedWorldEvent) {
        try {
            PlayerManager.attachPlayer(event.player)
        } catch (e: Exception) {
            if (e is DefaultGroupMissing) {
                e.message?.let { LogUtil.warn(it) }
            }
        }
    }

}

package io.delilaheve.manager

import io.delilaheve.LilysPermissions
import io.delilaheve.data.Group
import io.delilaheve.manager.CommandManager.Commands.ADD_GROUP
import io.delilaheve.manager.CommandManager.Commands.DEMOTE
import io.delilaheve.manager.CommandManager.Commands.LP_RELOAD
import io.delilaheve.manager.CommandManager.Commands.PROMOTE
import io.delilaheve.manager.CommandManager.Commands.SET_GROUP
import io.delilaheve.util.GroupUtil.asGroup
import io.delilaheve.util.LadderUtil
import io.delilaheve.util.LadderUtil.asLadder
import io.delilaheve.util.LadderUtil.findDemotionGroup
import io.delilaheve.util.LadderUtil.findPromotionGroup
import io.delilaheve.util.PlayerUtil.addGroups
import io.delilaheve.util.PlayerUtil.highestGroup
import io.delilaheve.util.PlayerUtil.setGroups
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Command router
 */
object CommandManager {

    // Error messages
    private const val ERROR_NO_GROUP = "Player does not have a group"
    private const val ERROR_NO_LADDER = "An appropriate ladder could not be found"
    private const val ERROR_TOP_OF_LADDER = "Player is at the top of the ladder already"
    private const val ERROR_BOTTOM_OF_LADDER = "Player is at the bottom of the ladder already"
    private const val ERROR_NO_GROUPS_FOUND = "No mentioned groups found"
    private const val ERROR_RELOAD_FAILED = "An error occurred reloading permissions"
    // Success messages
    private const val SUCCESS_GROUPS_UPDATED = "Player groups updated"
    private const val SUCCESS_PLAYER_PROMOTED = "Player promoted"
    private const val SUCCESS_PLAYER_DEMOTED = "Player demoted"
    private const val SUCCESS_RELOADED = "Permissions reloaded"
    
    /**
     * Enum of valid commands
     */
    private enum class Commands {
        PROMOTE,
        DEMOTE,
        SET_GROUP,
        ADD_GROUP,
        LP_RELOAD;

        companion object {
            /**
             * Try to determine the [Commands] item from its name ([key])
             */
            fun fromKey(key: String): Commands? = try {
                valueOf(key.uppercase().replace("-", "_"))
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Route the given [command] to the appropriate handler function
     */
    fun routeCommand(
        sender: CommandSender,
        command: Command,
        args: Array<out String>
    ): Boolean {
        return when (Commands.fromKey(command.name)) {
            PROMOTE -> moveAlongLadderCommand(sender, args)
            DEMOTE -> moveAlongLadderCommand(sender, args, false)
            SET_GROUP -> groupCommand(sender, args)
            ADD_GROUP -> groupCommand(sender, args, false)
            LP_RELOAD -> reloadCommand(sender)
            else -> false
        }
    }

    /**
     * Promote a player
     */
    private fun moveAlongLadderCommand(
        sender: CommandSender,
        args: Array<out String>,
        isPromote: Boolean = true
    ): Boolean {
        val player = args.getMentionedUser()
            ?: return false
        val playerGroup = player.highestGroup(player.world)
        if (playerGroup == null) {
            sender.sendError(ERROR_NO_GROUP)
            return true
        }
        val ladder = args.getOrNull(1)
            ?.asLadder()
            ?: LadderUtil.firstLadderWithGroup(playerGroup)
        if (ladder == null) {
            sender.sendError(ERROR_NO_LADDER)
            return true
        }
        val newGroup = if (isPromote) {
            player.findPromotionGroup(ladder)
        } else {
            player.findDemotionGroup(ladder)
        }
        if (newGroup == null) {
            val error = if (isPromote) {
                ERROR_TOP_OF_LADDER
            } else {
                ERROR_BOTTOM_OF_LADDER
            }
            sender.sendError(error)
            return true
        }
        val result = player.setGroups(listOf(newGroup.name))
        val success = if (isPromote) {
            SUCCESS_PLAYER_PROMOTED
        } else {
            SUCCESS_PLAYER_DEMOTED
        }
        sender.sendSuccess(success)
        return result
    }

    /**
     * Set a player's group(s)
     */
    private fun groupCommand(
        sender: CommandSender,
        args: Array<out String>,
        isSet: Boolean = true
    ): Boolean {
        val player = args.getMentionedUser()
            ?: return false
        if (args.size < 2) {
            return false
        }
        val groups = args.getMentionedGroups()
            .map { it.name }
        if (groups.isEmpty()) {
            sender.sendError(ERROR_NO_GROUPS_FOUND)
            return true
        }
        val result = if (isSet) {
            player.setGroups(groups)
        } else {
            player.addGroups(groups)
        }
        sender.sendSuccess(SUCCESS_GROUPS_UPDATED)
        return result
    }

    /**
     * Reload plugin configurations
     */
    private fun reloadCommand(
        sender: CommandSender
    ): Boolean = try {
        PlayerManager.updateAttachments()
        sender.sendSuccess(SUCCESS_RELOADED)
        true
    } catch (e: Exception) {
        sender.sendError(ERROR_RELOAD_FAILED)
        true
    }

    /**
     * Get the [Player] mentioned in this [Array]
     */
    private fun Array<out String>.getMentionedUser(): Player? {
        val server = LilysPermissions.instance
            ?.server
            ?: return null
        return getOrNull(0)
            ?.let { server.getPlayerExact(it) }
    }

    /**
     * Get all mentioned [Group]s in this [Array]
     */
    private fun Array<out String>.getMentionedGroups(): List<Group> = try {
        copyOfRange(1, size).mapNotNull { it.asGroup() }
    } catch (e: Exception) {
        emptyList()
    }

    /**
     * Send a chat [message] to this [CommandSender] in red to indicate
     * the action was not successful
     */
    private fun CommandSender.sendError(message: String) {
        sendMessage("${ChatColor.RED}$message")
    }

    /**
     * Send a chat [message] to the [CommandSender] in green to indicate
     * the action was successful
     */
    private fun CommandSender.sendSuccess(message: String) {
        sendMessage("${ChatColor.GREEN}$message")
    }

}

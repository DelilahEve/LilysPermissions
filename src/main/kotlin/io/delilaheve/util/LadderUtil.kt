package io.delilaheve.util

import io.delilaheve.LilysPermissions.Companion.PERMISSIONS_FILE
import io.delilaheve.data.Group
import io.delilaheve.data.Ladder
import io.delilaheve.util.GroupUtil.asGroup
import io.delilaheve.util.GroupUtil.highestRanked
import io.delilaheve.util.PlayerUtil.getUserPermissions
import io.delilaheve.util.PlayerUtil.groups
import io.delilaheve.util.YamlUtil.PATH_LADDERS
import io.delilaheve.util.YamlUtil.readLadder
import org.bukkit.entity.Player

/**
 * Utility functions intended to ease [Ladder] management
 */
object LadderUtil {

    /**
     * Get a [List] of all defined [Ladder]s
     */
    private fun allLadders(): List<Ladder> = YamlUtil.getFile(PERMISSIONS_FILE)
        ?.getConfigurationSection(PATH_LADDERS)
        ?.getKeys(false)
        ?.mapNotNull { it.asLadder() }
        ?: emptyList()

    /**
     * Try to convert this [String] to a [Ladder]
     */
    fun String.asLadder(): Ladder? = YamlUtil.getFile(PERMISSIONS_FILE)
        ?.readLadder(this)

    /**
     * Try to find the first [Ladder] defined containing the given [group]
     */
    fun firstLadderWithGroup(
        group: Group
    ): Ladder? = allLadders().firstOrNull {
        group.name in it.groups
    }

    /**
     * Try to find the next [Group] to promote this [Player] to on the given [ladder]
     */
    fun Player.findPromotionGroup(
        ladder: Ladder
    ): Group? {
        val currentGroup = getUserPermissions(world)?.groups()
            ?.highestRanked()
            ?: return null
        val currentPosition = ladder.groups
            .indexOfLast { it.equals(currentGroup.name, true) }
        return ladder.groups
            .getOrNull(currentPosition + 1)
            ?.asGroup()
    }

    /**
     * Try to find the next [Group] to demote this [Player] to on the given [ladder]
     */
    fun Player.findDemotionGroup(
        ladder: Ladder
    ): Group? {
        val currentGroup = getUserPermissions(world)?.groups()
            ?.highestRanked()
            ?: return null
        val currentPosition = ladder.groups
            .indexOfLast { it.equals(currentGroup.name, true) }
        return ladder.groups
            .getOrNull(currentPosition - 1)
            ?.asGroup()
    }

}

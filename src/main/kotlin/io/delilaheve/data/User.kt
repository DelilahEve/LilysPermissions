package io.delilaheve.data

import java.util.*

/**
 * Model representing a permissions user
 *
 * @param prefix User's prefix string
 * @param suffix User's suffix string
 * @param groups User's permission group names
 * @param permissions User's permission overrides
 * @param denyPermissions User's denied permission overrides
 */
data class User(
    val uuid: UUID,
    val prefix: String,
    val suffix: String,
    val groups: List<String>,
    val permissions: List<String>,
    val denyPermissions: List<String>
)

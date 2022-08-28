package io.delilaheve.data

/**
 * Model representing a Group override for a specific world. Which world this is
 * for is kept in the map this object will get stored in.
 *
 * @param prefix Prefix string override.
 * @param suffix Suffix string override.
 * @param permissions List of permission strings to be given when the associated
 *                    user(s) are in the world this is set for.
 */
data class GroupWorldOverride(
    val prefix: String,
    val suffix: String,
    val permissions: List<String>
)

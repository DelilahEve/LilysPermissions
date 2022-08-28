package io.delilaheve.data

/**
 * Model representing a permissions user
 *
 * @param prefix User's prefix string
 * @param suffix User's suffix string
 * @param groups User's permission group names
 * @param permissions User's permission overrides
 */
data class User(
    val prefix: String,
    val suffix: String,
    val groups: List<String>,
    val permissions: List<String>
)

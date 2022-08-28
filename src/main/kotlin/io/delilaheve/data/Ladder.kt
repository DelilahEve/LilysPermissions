package io.delilaheve.data

/**
 * Model representing a group ladder
 *
 * @param name the name of this ladder
 * @param groups the groups in this ladder in ascending order
 */
data class Ladder(
    val name: String,
    val groups: List<String>
)

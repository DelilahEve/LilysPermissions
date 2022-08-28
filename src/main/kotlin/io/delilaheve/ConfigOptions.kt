package io.delilaheve

import io.delilaheve.ConfigOptions.PrefixSuffixModes.Companion
import io.delilaheve.ConfigOptions.PrefixSuffixModes.OVERRIDE
import org.bukkit.configuration.Configuration

/**
 * Configuration options accessors
 */
object ConfigOptions {

    /**
     * Prefix/Suffix modes
     */
    enum class PrefixSuffixModes {
        OVERRIDE, // Should override group settings
        COMBINE;  // Should combine with group settings

        companion object {
            fun fromKey(key: String) = try {
                valueOf(key.uppercase())
            } catch (e: Exception) {
                OVERRIDE
            }
        }
    }

    // Path for whether all players should be saved
    private const val PATH_SAVE_ALL = "save_all_players"
    // Chat formatting toggle path
    private const val PATH_CHAT_FORMATTING = "chat_formatting"
    // Prefix mode path
    private const val PATH_PREFIX_MODE = "user_prefix_mode"
    // Suffix mode path
    private const val PATH_SUFFIX_MODE = "user_suffix_mode"
    // Prefix spacing path
    private const val PATH_PREFIX_SPACE = "space_after_prefix"
    // Suffix spacing path
    private const val PATH_SUFFIX_SPACE = "space_before_suffix"

    // Default save all
    private const val DEFAULT_SAVE_ALL = true
    // Default for chat formatting
    private const val DEFAULT_CHAT_FORMATTING = true
    // Default prefix/suffix mode
    private val DEFAULT_MODE = OVERRIDE
    // Default spacing
    private const val DEFAULT_SPACING = true

    // Configuration instance
    private val config: Configuration?
        get() = LilysPermissions.instance?.config

    // Whether we should be saving all players on their first join or not
    val saveAll: Boolean
        get() = config?.getBoolean(PATH_SAVE_ALL, DEFAULT_SAVE_ALL)
            ?: DEFAULT_SAVE_ALL

    // Whether we should perform chat formatting
    val chatFormatting: Boolean
        get() = config?.getBoolean(PATH_CHAT_FORMATTING, DEFAULT_CHAT_FORMATTING)
            ?: DEFAULT_CHAT_FORMATTING

    // How should user prefixes operate
    val prefixMode: PrefixSuffixModes
        get() = config?.getString(PATH_PREFIX_MODE)
            ?.let { PrefixSuffixModes.fromKey(it) }
            ?: DEFAULT_MODE

    // How should user suffixes operate
    val suffixMode: PrefixSuffixModes
        get() = config?.getString(PATH_SUFFIX_MODE)
            ?.let { Companion.fromKey(it) }
            ?: DEFAULT_MODE

    // Should we put a space between the prefix and the user's name?
    val spaceAfterPrefix: Boolean
        get() = config?.getBoolean(PATH_PREFIX_SPACE, DEFAULT_SPACING)
            ?: DEFAULT_SPACING

    // Should we put a space between the user's name and their suffix?
    val spaceBeforeSuffix: Boolean
        get() = config?.getBoolean(PATH_SUFFIX_SPACE, DEFAULT_SPACING)
            ?: DEFAULT_SPACING

}

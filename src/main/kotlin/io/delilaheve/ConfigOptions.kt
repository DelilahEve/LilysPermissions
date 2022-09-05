package io.delilaheve

import io.delilaheve.ConfigOptions.PrefixSuffixModes.Companion
import io.delilaheve.ConfigOptions.PrefixSuffixModes.OVERRIDE
import io.delilaheve.LilysPermissions.Companion.CONFIG_FILE
import io.delilaheve.util.LogUtil
import io.delilaheve.util.YamlUtil
import io.delilaheve.util.YamlUtil.trySave
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
    // Path for whether tab list formatting should be enabled
    private const val PATH_TAB_LIST_FORMATTING = "tab_list_formatting"
    // Path for whether tab list formatting should be group-restricted
    private const val PATH_TAB_LIST_RESTRICT = "tab_list_restrict_groups"
    // Tab list display groups path
    private const val PATH_TAB_LIST_GROUPS = "tab_list_groups"
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
    // Default for tab list formatting
    private const val DEFAULT_TAB_LIST_FORMATTING = true
    // Default for tab list group restriction
    private const val DEFAULT_TAB_LIST_RESTRICT = false
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

    // Whether we should perform tab list formatting
    val tabFormatting: Boolean
        get() = config?.getBoolean(PATH_TAB_LIST_FORMATTING, DEFAULT_TAB_LIST_FORMATTING)
            ?: DEFAULT_TAB_LIST_FORMATTING

    // Whether we should restrict tab list formatting by group
    val tabRestrictions: Boolean
        get() = config?.getBoolean(PATH_TAB_LIST_RESTRICT, DEFAULT_TAB_LIST_RESTRICT)
            ?: DEFAULT_TAB_LIST_RESTRICT

    // List of groups whose members may have their name altered on the tab list
    val tabListGroups: List<String>
        get() = config?.getStringList(PATH_TAB_LIST_GROUPS)
            ?: emptyList()

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

    /**
     * Update config.yml with new options, ensuring we restore current values when
     * we replace the file.
     */
    init {
        if (missingConfigNodes()) { upgradeConfig() }
    }

    /**
     * Check if any newly added paths are missing from the current config file
     *
     * Last changed for v1.0.4
     */
    private fun missingConfigNodes(): Boolean {
        val configOnDisk = YamlUtil.getFile(CONFIG_FILE)
        return listOf(
            PATH_TAB_LIST_FORMATTING,
            PATH_TAB_LIST_RESTRICT,
            PATH_TAB_LIST_GROUPS
        ).any { configOnDisk?.get(it) == null }
    }

    /**
     * Upgrade the config file to contain the latest options,
     * preserving current settings in the process
     */
    private fun upgradeConfig(){
        var configOnDisk = YamlUtil.getFile(CONFIG_FILE) ?: return
        val currentValues = mutableMapOf<String, Any>()
        // For now config.yml only has shallow keys, so we can get away with this
        configOnDisk.getKeys(false).map { key ->
            configOnDisk.get(key)?.let { value ->
                currentValues.put(key, value)
            }
        }
        // Replace the config file
        LilysPermissions.instance
            ?.saveResource(CONFIG_FILE, true)
        // Re-grab config on disk since we've just changed it
        configOnDisk = YamlUtil.getFile(CONFIG_FILE) ?: return
        // Restore saved values
        currentValues.forEach { (key, value) ->
            configOnDisk.set(key, value)
        }
        configOnDisk.trySave(CONFIG_FILE)
    }

}

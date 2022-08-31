package io.delilaheve.util

import io.delilaheve.LilysPermissions

/**
 * Utilities to ease permissions management
 */
object PermissionsUtil {

    // Wildcard permission character, can be placed at the end of a permission or as a standalone
    const val WILDCARD_PERMISSION = "*"

    /**
     * List of all permission strings registered to the server
     */
    val allPermissionStrings: List<String>
        get() = LilysPermissions.instance
            ?.server
            ?.pluginManager
            ?.permissions
            ?.toList()
            ?.map { it.name }
            ?: emptyList()

    /**
     * List of all permission string descending from the given [parent]
     *
     * Assumes [parent] does not end with a ".", meaning the parent should
     * be the actual node we want to find children of. Child permissions don't
     * need to be explicitly defined as such by the plugin in question as we're
     * doing value matching rather than pulling registered children. We do this
     * because it's not a safe assumption that all plugins will properly register
     * child permissions.
     *
     * Unregistered permissions will be impossible to find, unfortunately.
     *
     * As an example if [parent] is "lilys_permissions" we should find
     * "lilys_permissions.promote" and other children.
     */
    fun descendingPermissions(
        parent: String
    ): List<String> = allPermissionStrings.filter {
        it.startsWith("$parent.")
    }.distinct()

}

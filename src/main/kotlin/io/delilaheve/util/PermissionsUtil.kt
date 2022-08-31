package io.delilaheve.util

import io.delilaheve.LilysPermissions

/**
 * Utilities to ease permissions management
 */
object PermissionsUtil {

    val allPermissionStrings: List<String>
        get() = LilysPermissions.instance
            ?.server
            ?.pluginManager
            ?.permissions
            ?.toList()
            ?.map { it.name }
            ?: emptyList()

}

package io.delilaheve.util

import io.delilaheve.LilysPermissions

/**
 * Utilities to ease logging
 */
object LogUtil {

    /**
     * Log a warning [message]
     */
    fun warn(message: String) {
        LilysPermissions.instance
            ?.logger
            ?.warning(message)
    }

}

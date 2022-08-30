package io.delilaheve.util

import io.delilaheve.LilysPermissions
import java.util.logging.Logger

/**
 * Utilities to ease logging
 */
object LogUtil {

    /**
     * [Logger] instance
     */
    private val logger: Logger?
        get() = LilysPermissions.instance
            ?.logger

    /**
     * Log an info [message]
     */
    fun info(
        message: String
    ) = logger?.info(message)

    /**
     * Log a warning [message]
     */
    fun warn(
        message: String
    ) = logger?.warning(message)

}

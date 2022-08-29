package io.delilaheve.util

import io.delilaheve.LilysPermissions

object LogUtil {

    private const val debugBuild = true

    fun info(message: String) {
        if (!debugBuild) { return }
        LilysPermissions.instance
            ?.logger
            ?.info(message)
    }

    fun warn(message: String) {
        LilysPermissions.instance
            ?.logger
            ?.warning(message)
    }

}

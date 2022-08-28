package io.delilaheve.exception

import io.delilaheve.LilysPermissions.Companion.PERMISSIONS_FILE

/**
 * Thrown when a default group is not defined in permissions.yml
 */
class DefaultGroupMissing : IllegalStateException(
    "Default group missing from $PERMISSIONS_FILE"
)

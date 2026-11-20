package com.mmk.kmpnotifier.permission

public interface PermissionUtil {
    public fun hasNotificationPermission(): Boolean
    public fun askNotificationPermission()
}

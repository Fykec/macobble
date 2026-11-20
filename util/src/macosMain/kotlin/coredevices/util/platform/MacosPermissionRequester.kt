package coredevices.util.platform

import PlatformUiContext
import coredevices.util.AppResumed
import coredevices.util.Permission
import coredevices.util.PermissionRequester
import coredevices.util.PermissionResult
import coredevices.util.RequiredPermissions
import platform.AppKit.NSWorkspace
import platform.Foundation.NSURL

// macOS v1: permissions are granted through System Settings; the app reports what the
// existing permission-nag UI needs and never prompts inline.
class MacosPermissionRequester(
    requiredPermissions: RequiredPermissions,
    appResumed: AppResumed,
) : PermissionRequester(requiredPermissions, appResumed) {
    override suspend fun requestPlatformPermission(
        permission: Permission,
        uiContext: PlatformUiContext,
    ): PermissionResult = PermissionResult.Rejected

    override suspend fun hasPermission(permission: Permission): Boolean = false

    override fun openPermissionsScreen(uiContext: PlatformUiContext) {
        NSURL.URLWithString("x-apple.systempreferences:com.apple.preference.security?Privacy_Bluetooth")
            ?.let { NSWorkspace.sharedWorkspace.openURL(it) }
    }
}

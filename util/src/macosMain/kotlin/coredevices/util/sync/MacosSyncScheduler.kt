package coredevices.util.sync

import coredevices.util.Platform

// macOS has no background refresh scheduling; sync runs while the app is in the foreground.
class MacosSyncScheduler(
    private val syncManager: Lazy<SyncManager>,
    private val platform: Platform,
) : SyncScheduler {
    override fun schedule() = Unit
}

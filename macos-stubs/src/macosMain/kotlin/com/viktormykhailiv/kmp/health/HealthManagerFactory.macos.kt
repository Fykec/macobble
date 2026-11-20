package com.viktormykhailiv.kmp.health

import com.viktormykhailiv.kmp.health.region.RegionalPreferences
import kotlin.time.Duration
import kotlin.time.Instant

// macOS has no health platform: every manager is a no-op returning failures/empty results.
internal object NoopHealthManager : HealthManager {
    override fun isAvailable(): Result<Boolean> = Result.success(false)
    override suspend fun isAuthorized(
        readTypes: List<HealthDataType>,
        writeTypes: List<HealthDataType>,
    ): Result<Boolean> = Result.success(false)
    override suspend fun requestAuthorization(
        readTypes: List<HealthDataType>,
        writeTypes: List<HealthDataType>,
        requestReadHealthDataInBackground: Boolean,
        requestReadHealthDataHistory: Boolean,
    ): Result<Boolean> = Result.success(false)
    override suspend fun isRevokeAuthorizationSupported(): Result<Boolean> = Result.success(false)
    override suspend fun revokeAuthorization(): Result<Unit> = Result.success(Unit)
    override fun openSystemHealthSettings(): Result<Unit> = Result.success(Unit)
    override suspend fun hasReadHealthDataInBackgroundPermission(): Result<Boolean> = Result.success(false)
    override suspend fun requestReadHealthDataInBackgroundPermission(): Result<Boolean> = Result.success(false)
    override suspend fun hasReadHealthDataHistoryPermission(): Result<Boolean> = Result.success(false)
    override suspend fun requestReadHealthDataHistoryPermission(): Result<Boolean> = Result.success(false)
    override suspend fun readData(
        startTime: Instant,
        endTime: Instant,
        type: HealthDataType,
    ): Result<List<HealthRecord>> = Result.success(emptyList())
    override suspend fun writeData(
        records: List<HealthRecord>,
    ): Result<Unit> = Result.success(Unit)
    override suspend fun aggregate(
        startTime: Instant,
        endTime: Instant,
        type: HealthDataType,
    ): Result<HealthAggregatedRecord> =
        Result.failure(UnsupportedOperationException("No health platform on macOS"))
    override suspend fun aggregateGroupByDuration(
        startTime: Instant,
        endTime: Instant,
        sliceWidth: Duration,
        type: HealthDataType,
    ): Result<List<HealthAggregatedRecord>> = Result.success(emptyList())
    override suspend fun getRegionalPreferences(): Result<RegionalPreferences> =
        Result.failure(UnsupportedOperationException("No health platform on macOS"))
}

public actual class HealthManagerFactory actual constructor() {
    public actual fun createManager(
        options: HealthManagerFactoryOptions,
    ): HealthManager = NoopHealthManager
}

public actual class HealthManagerFactoryOptions {
    public actual companion object {
        public actual fun default(): HealthManagerFactoryOptions = HealthManagerFactoryOptions()
    }
}

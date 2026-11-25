package coredevices.coreapp.di

import CoreAppVersion
import PlatformContext
import PlatformShareLauncher
import coredevices.coreapp.auth.RealAppleAuthUtil
import coredevices.coreapp.auth.RealGithubAuthUtil
import coredevices.coreapp.auth.RealGoogleAuthUtil
import coredevices.analytics.AnalyticsBackend
import coredevices.coreapp.util.AppUpdate
import coredevices.coreapp.util.MacosAppUpdate
import coredevices.libindex.LibIndex
import coredevices.pebble.config.ConfigPageClosedListener
import coredevices.util.NoOpWatchNotifier
import coredevices.util.NoopCompanionDevice
import coredevices.util.PermissionRequester
import coredevices.util.Platform
import coredevices.util.RequiredPermissions
import coredevices.util.WatchNotifier
import coredevices.util.auth.AppleAuthUtil
import coredevices.util.auth.GitHubAuthUtil
import coredevices.util.auth.GoogleAuthUtil
import coredevices.util.auth.NoOpSilentSignIn
import coredevices.util.auth.SilentSignIn
import coredevices.util.integrations.OAuthLauncher
import coredevices.util.models.ModelDownloadManager
import coredevices.util.sync.SyncManager
import coredevices.util.sync.SyncScheduler
import coredevices.util.sync.MacosSyncScheduler
import coredevices.util.platform.MacosPlatform
import coredevices.util.platform.MacosPermissionRequester
import coredevices.util.integrations.MacosOAuthLauncher
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import io.rebble.libpebblecommon.connection.AppContext
import coredevices.util.transcription.CactusModelPathProvider
import coredevices.util.transcription.NoopCactusModelPathProvider
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.time.Duration
import kotlin.time.DurationUnit

val macosDefaultModule = module {
    singleOf(::RealGoogleAuthUtil) bind GoogleAuthUtil::class
    single<SilentSignIn> { NoOpSilentSignIn }
    singleOf(::RealAppleAuthUtil) bind AppleAuthUtil::class
    singleOf(::RealGithubAuthUtil) bind GitHubAuthUtil::class
    singleOf(::PlatformShareLauncher)
    factory { params ->
        Darwin.create {
            configureRequest {
                setTimeoutInterval(params.get<Duration>().toDouble(DurationUnit.SECONDS))
            }
        }
    } bind HttpClientEngine::class
    singleOf(::AppContext)
    singleOf(::MacosPlatform) bind Platform::class
    singleOf(::MacosOAuthLauncher) bind OAuthLauncher::class
    singleOf(::PlatformContext)
    singleOf(::MacosPermissionRequester) bind PermissionRequester::class
    single<WatchNotifier> { NoOpWatchNotifier() }
    single { NoopCompanionDevice() } bind coredevices.util.CompanionDevice::class
    single<AppUpdate> { MacosAppUpdate() }
    single { MacosSyncScheduler(lazy { get<SyncManager>() }, get()) } bind SyncScheduler::class
    single {
        val version = platform.Foundation.NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: "unknown"
        val build = (platform.Foundation.NSBundle.mainBundle.infoDictionary?.get("CFBundleVersion") as? String)?.toInt() ?: 0
        val buildStringAddition = if (build > 0) ".$build" else ""
        CoreAppVersion("$version$buildStringAddition")
    }
    single {
        RequiredPermissions(
            kotlinx.coroutines.flow.flow {
                emit(emptySet<coredevices.util.Permission>())
            }
        )
    }
    singleOf(::ModelDownloadManager)
    // macOS has no ring, so there is nothing to offer when a plugin's config page closes.
    single { ConfigPageClosedListener { _ -> } }
    // macOS has no ring/local models; callers fall back to cloud transcription.
    single<CactusModelPathProvider> { NoopCactusModelPathProvider() }
    single<AnalyticsBackend> { NoopAnalyticsBackend }
    single<LibIndex> { NoopLibIndex() }
}

private object NoopAnalyticsBackend : AnalyticsBackend {
    override fun logEvent(name: String, parameters: Map<String, Any>?) = Unit

    override fun addGlobalProperty(name: String, value: String?) = Unit

    override fun setEnabled(enabled: Boolean) = Unit
}

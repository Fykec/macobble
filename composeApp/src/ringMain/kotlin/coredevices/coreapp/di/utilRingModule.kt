package coredevices.coreapp.di

import coredevices.coreapp.plugins.AppstorePluginStore
import coredevices.coreapp.ui.screens.BugReportProcessor
import coredevices.pebble.config.ConfigPageClosedListener
import coredevices.ring.agent.builtin_servlets.bugreport.BugReporter
import coredevices.ring.agent.integrations.providers.DefaultDestinationPrompter
import coredevices.ring.agent.pebble_plugin.PluginStore
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/** Ring-only bindings (Android/iOS); not included on macOS. */
val utilRingModule = module {
    single { BugReporter(get<BugReportProcessor>()::newVoiceBugReport) }
    singleOf(::AppstorePluginStore) bind PluginStore::class
    single { ConfigPageClosedListener(get<DefaultDestinationPrompter>()::configPageClosed) }
}

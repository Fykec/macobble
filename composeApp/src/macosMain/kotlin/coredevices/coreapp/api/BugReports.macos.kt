package coredevices.coreapp.api

import PlatformContext

actual fun createNotification(
    platformContext: PlatformContext,
    title: String,
    message: String,
    conversationId: String,
) = Unit

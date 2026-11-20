package com.mmk.kmpnotifier.notification

import com.mmk.kmpnotifier.logger.Logger
import com.mmk.kmpnotifier.permission.PermissionUtil

public class PayloadData(
    public val payload: Map<String, Any> = emptyMap(),
) {
    public operator fun get(key: String): Any? = payload[key]
}

public interface Notifier {
    public fun notify(id: Int, title: String, body: String)
    public fun notify(id: Int, title: String, body: String, payloadData: PayloadData)
    public fun notify(block: NotifierBuilder.() -> Unit)
    public fun remove(id: Int)
    public fun removeAll()
}

public class NotifierBuilder {
    public var id: Int = 0
    public var title: String = ""
    public var body: String = ""
    public var payloadData: PayloadData? = null
}

public interface PushNotifier {
    public fun getToken(): String?
    public fun deleteToken()
    public fun subscribeToTopic(topic: String)
    public fun unsubscribeFromTopic(topic: String)
}

public object NotifierManager {

    public interface Listener {
        public fun onNewToken(token: String) {}
        public fun onPayloadData(data: PayloadData) {}
        public fun onPushNotification(title: String?, body: String?) {}
        public fun onPushNotificationWithPayloadData(title: String?, body: String?, data: PayloadData) {}
        public fun onNotificationClicked(data: PayloadData) {}
    }

    private val listeners = mutableListOf<Listener>()
    private var singleListener: Listener? = null
    private var logger: Logger = Logger {}

    private val localNotifier = object : Notifier {
        override fun notify(id: Int, title: String, body: String) = Unit
        override fun notify(id: Int, title: String, body: String, payloadData: PayloadData) = Unit
        override fun notify(block: NotifierBuilder.() -> Unit) = Unit
        override fun remove(id: Int) = Unit
        override fun removeAll() = Unit
    }

    private val pushNotifier = object : PushNotifier {
        override fun getToken(): String? = null
        override fun deleteToken() = Unit
        override fun subscribeToTopic(topic: String) = Unit
        override fun unsubscribeFromTopic(topic: String) = Unit
    }

    public fun initialize(configuration: com.mmk.kmpnotifier.notification.configuration.NotificationPlatformConfiguration) = Unit
    public fun getLocalNotifier(): Notifier = localNotifier
    public fun getPushNotifier(): PushNotifier = pushNotifier
    public fun getPermissionUtil(): PermissionUtil = object : PermissionUtil {
        override fun hasNotificationPermission(): Boolean = false
        override fun askNotificationPermission() = Unit
    }
    public fun addListener(listener: Listener) {
        listeners.add(listener)
    }
    public fun setListener(listener: Listener?) {
        singleListener = listener
    }
    public fun setLogger(logger: Logger) {
        this.logger = logger
    }
    public fun setLogger(loggerProvider: com.mmk.kmpnotifier.logger.LoggerProvider) {
        this.logger = loggerProvider.provideLogger()
    }
    public fun onNewToken(token: String) {
        logger.log("onNewToken")
        singleListener?.onNewToken(token)
        listeners.forEach { it.onNewToken(token) }
    }
    public fun onPushPayloadData(data: PayloadData) {
        singleListener?.onPayloadData(data)
        listeners.forEach { it.onPayloadData(data) }
    }
    public fun onPushNotification(title: String?, body: String?) {
        singleListener?.onPushNotification(title, body)
        listeners.forEach { it.onPushNotification(title, body) }
    }
    public fun onPushNotificationWithPayloadData(title: String?, body: String?, data: PayloadData) {
        singleListener?.onPushNotificationWithPayloadData(title, body, data)
        listeners.forEach { it.onPushNotificationWithPayloadData(title, body, data) }
    }
    public fun onNotificationClicked(data: PayloadData) {
        singleListener?.onNotificationClicked(data)
        listeners.forEach { it.onNotificationClicked(data) }
    }
}

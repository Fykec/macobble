package com.mmk.kmpnotifier.logger

public fun interface Logger {
    public fun log(message: String)
}

public fun interface LoggerProvider {
    public fun provideLogger(): Logger
}

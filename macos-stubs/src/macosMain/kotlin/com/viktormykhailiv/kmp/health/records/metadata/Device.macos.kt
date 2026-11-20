package com.viktormykhailiv.kmp.health.records.metadata

import com.viktormykhailiv.kmp.health.records.metadata.Device
import com.viktormykhailiv.kmp.health.records.metadata.DeviceType

actual fun Device.Companion.getLocalDevice(): Device =
    Device(type = DeviceType.Phone)

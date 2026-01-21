package com.fkmit.fido

import org.apache.cordova.PluginResult

enum class StatusCodes(val code: Int, val resultStatus: PluginResult.Status) {
    Success(1, PluginResult.Status.OK),
    Failure(2, PluginResult.Status.ERROR),
    Progress(3, PluginResult.Status.OK)
}
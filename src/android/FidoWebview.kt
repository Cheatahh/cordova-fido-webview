package com.fkmit.fido

import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaPlugin
import org.apache.cordova.PluginResult
import org.json.JSONArray
import org.json.JSONObject
import android.util.Log

import com.yubico.yubikit.android.YubiKitManager
import com.yubico.yubikit.android.transport.nfc.NfcConfiguration
import com.yubico.yubikit.android.transport.nfc.NfcDispatcher
import com.yubico.yubikit.android.transport.nfc.NfcReaderDispatcher
import com.yubico.yubikit.android.transport.nfc.NfcYubiKeyManager
import com.yubico.yubikit.android.transport.usb.UsbConfiguration
import com.yubico.yubikit.android.transport.usb.UsbYubiKeyManager
import com.yubico.yubikit.core.YubiKeyDevice

enum class StatusCodes(val code: Int, val resultStatus: PluginResult.Status) {
    Success(1, PluginResult.Status.OK),
    Failure(2, PluginResult.Status.ERROR),
    Progress(3, PluginResult.Status.OK)
}

class FidoWebview : CordovaPlugin() {
    override fun execute(action: String, args: JSONArray, callback: CallbackContext) = when(action) {
        "getAssertion" -> {
            callback.with(args, ::executeGetAssertion)
            true
        }
        else -> false
    }
    private fun CallbackContext.sendStatusResult(code: StatusCodes, payload: Any) {
        val result = PluginResult(code.resultStatus, JSONObject().apply {
            put("statusCode", code.code)
            put("payload", payload)
        })
        result.setKeepCallback(code === StatusCodes.Progress)
        sendPluginResult(result)
    }
    private inline fun CallbackContext.with(args: JSONArray, handler: (JSONArray) -> Any): Boolean {
        runCatching {
            handler(args)
        }.onSuccess { result ->
            sendStatusResult(StatusCodes.Success, result)
        }.onFailure { err ->
            sendStatusResult(StatusCodes.Failure, err.message.toString())
        }
        return true
    }
    private fun executeGetAssertion(args: JSONArray): String {
        require(args.length() == 2) {
            "Invalid parameters, expected <CLIENT_DATA> <USER_PIN>."
        }
        val clientData = requireNotNull(args.optString(0).takeIf(String::isNotBlank)) {
            "Parameter <CLIENT_DATA> must be a string."
        }
        val userPin = requireNotNull(args.optString(1).takeIf(String::isNotBlank)) {
            "Parameter <USER_PIN> must be a string."
        }

        return "Nice"
    }
}
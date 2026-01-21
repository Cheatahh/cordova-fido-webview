package com.fkmit.fido

import org.json.JSONArray

object FidoHandlers {
    fun getAssertion(args: JSONArray, nfc: NFCDiscoveryDispatcher): String {
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
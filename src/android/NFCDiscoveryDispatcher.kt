package com.fkmit.fido

import android.nfc.TagLostException
import android.util.Log
import com.yubico.yubikit.core.YubiKeyDevice
import com.yubico.yubikit.core.smartcard.SmartCardConnection

interface NFCDiscoveryDispatcher {

    var currentNFCDevice: YubiKeyDevice?

    fun startDeviceDiscovery(callback: (YubiKeyDevice) -> Unit)

    fun stopDeviceDiscovery()

    fun useDeviceConnection(dispatch: ResultDispatcher, callback: (SmartCardConnection) -> Unit) {
        runCatching {
            requireNotNull(currentNFCDevice).let {
                dispatch.sendMessage(MessageCodes.SignalDeviceDiscovered, null)
                it.openConnection(SmartCardConnection::class.java)
            }
        }.onSuccess { connection ->
            try {
                connection.use(callback)
            } catch (_: TagLostException) {
                currentNFCDevice = null
                dispatch.sendMessage(MessageCodes.SignalDeviceLost, null)
            }
        }.onFailure {
            try {
                stopDeviceDiscovery()
                startDeviceDiscovery(InvokeOnce { device ->
                    dispatch.sendMessage(MessageCodes.SignalDeviceDiscovered, null)
                    device.openConnection(SmartCardConnection::class.java).use(callback)
                })
            } catch (_: TagLostException) {
                currentNFCDevice = null
                dispatch.sendMessage(MessageCodes.SignalDeviceLost, null)
            }
        }
    }

}
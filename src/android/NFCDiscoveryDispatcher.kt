package com.fkmit.fido

import android.nfc.TagLostException
import android.util.Log
import com.yubico.yubikit.core.YubiKeyDevice
import com.yubico.yubikit.core.application.InvalidPinException
import com.yubico.yubikit.core.fido.CtapException
import com.yubico.yubikit.core.smartcard.SmartCardConnection

interface NFCDiscoveryDispatcher {

    var currentNFCDevice: YubiKeyDevice?

    fun startDeviceDiscovery(callback: (YubiKeyDevice) -> Unit)

    fun stopDeviceDiscovery()

    private fun runWithCatching(dispatch: ResultDispatcher, block: () -> Unit) {
        Log.wtf("DEBUG", "runWithCatching")
        try {
            block()
        } catch (e: TagLostException) {
            Log.wtf("ERROR", e)
            currentNFCDevice = null
            dispatch.sendMessage(MessageCodes.FailureDeviceLost, null)
        } catch (e: InvalidPinException) {
            Log.wtf("ERROR", e)
            dispatch.sendMessage(MessageCodes.FailureInvalidPin, null)
        } catch (e: CtapException) {
            Log.wtf("ERROR", e)
            dispatch.sendMessage(when(e.ctapError) {
                CtapException.ERR_NO_CREDENTIALS -> MessageCodes.FailureNoCredentials
                CtapException.ERR_PIN_INVALID -> MessageCodes.FailureInvalidPin
                else -> MessageCodes.FailureUnsupportedDevice
            }, null)
        } catch (e: Exception) {
            Log.wtf("ERROR", e)
            dispatch.sendMessage(MessageCodes.Failure, e.message)
        }
        Log.wtf("DEBUG", "runWithCatching2")
    }

    fun useDeviceConnection(dispatch: ResultDispatcher, callback: (SmartCardConnection) -> Unit) {
        runCatching {
            requireNotNull(currentNFCDevice).let {
                dispatch.sendMessage(MessageCodes.SignalDeviceDiscovered, null)
                it.openConnection(SmartCardConnection::class.java)
            }
        }.onSuccess { connection ->
            runWithCatching(dispatch) {
                connection.use(callback)
            }
        }.onFailure {
            startDeviceDiscovery(InvokeOnce { device ->
                dispatch.sendMessage(MessageCodes.SignalDeviceDiscovered, null)
                runWithCatching(dispatch) {
                    device.openConnection(SmartCardConnection::class.java).use(callback)
                }
            })
        }
    }

}
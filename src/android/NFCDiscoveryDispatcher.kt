package com.fkmit.fido

import com.yubico.yubikit.core.YubiKeyDevice

interface NFCDiscoveryDispatcher {
    fun startDeviceDiscovery(callback: (YubiKeyDevice) -> Unit)
    fun stopDeviceDiscovery()
}
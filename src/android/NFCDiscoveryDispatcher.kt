package com.fkmit.fido

interface NFCDiscoveryDispatcher {
    fun startDeviceDiscovery(callback: (YubiKeyDevice) -> Unit)
    fun stopDeviceDiscovery()
}
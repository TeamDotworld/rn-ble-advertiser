package com.reactnativebleadvertiser

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.JavaScriptModule
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager


class BleAdvertiserPackage : ReactPackage {
    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
        return listOf(BleAdvertiserModule(reactContext))
    }

  override fun createJSModules(): MutableList<Class<out JavaScriptModule>> {
    TODO("Not yet implemented")
  }

  override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
        return emptyList()
    }
}

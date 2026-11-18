package com.cleaner.tvbox

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class AutoCleanService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    companion object {
        var instance: AutoCleanService? = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}

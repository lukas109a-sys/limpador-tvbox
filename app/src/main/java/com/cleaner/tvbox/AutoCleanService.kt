package com.cleaner.tvbox

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class AutoCleanService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}

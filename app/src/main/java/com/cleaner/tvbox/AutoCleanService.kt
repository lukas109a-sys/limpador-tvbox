package com.cleaner.tvbox

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class AutoCleanService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Evento padrão exigido pelo sistema
    }

    override fun onInterrupt() {
        // Interrupção exigida pelo sistema
    }
}

package com.example.turboautismdoselog

import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.WindowManager
import com.example.turboautismdoselog.security.LockActivity

class DoseLogApplication : Application() {

    private var activityReferences = 0
    private var isActivityChangingConfigurations = false
    var isAppLocked = true
        private set

    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_OFF) {
                isAppLocked = true
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        registerReceiver(screenOffReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                activity.window.setFlags(
                    WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE
                )
            }

            override fun onActivityStarted(activity: Activity) {
                if (++activityReferences == 1 && !isActivityChangingConfigurations) {
                    redirectToLockIfNeeded(activity)
                }
            }

            override fun onActivityResumed(activity: Activity) {
                redirectToLockIfNeeded(activity)
            }

            override fun onActivityStopped(activity: Activity) {
                isActivityChangingConfigurations = activity.isChangingConfigurations
                if (--activityReferences == 0 && !isActivityChangingConfigurations) {
                    isAppLocked = true
                }
            }

            override fun onActivityPaused(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    private fun redirectToLockIfNeeded(activity: Activity) {
        if (isAppLocked && activity !is LockActivity) {
            val intent = Intent(activity, LockActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.startActivity(intent)
        }
    }

    fun unlockApp() {
        isAppLocked = false
    }
}
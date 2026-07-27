package com.example.turboautismdoselog

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import com.example.turboautismdoselog.security.LockActivity

class DoseLogApplication : Application() {

    private var activityReferences = 0
    private var isActivityChangingConfigurations = false
    var isAppLocked = true
        private set

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {

            override fun onActivityStarted(activity: Activity) {
                if (++activityReferences == 1 && !isActivityChangingConfigurations) {
                    // App came to the foreground from the background
                    if (isAppLocked && activity !is LockActivity) {
                        val intent = Intent(activity, LockActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        activity.startActivity(intent)
                    }
                }
            }

            override fun onActivityStopped(activity: Activity) {
                isActivityChangingConfigurations = activity.isChangingConfigurations
                if (--activityReferences == 0 && !isActivityChangingConfigurations) {
                    // App went to the background — lock it
                    isAppLocked = true
                }
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    fun unlockApp() {
        isAppLocked = false
    }
}
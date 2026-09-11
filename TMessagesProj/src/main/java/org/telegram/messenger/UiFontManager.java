/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 */

package org.telegram.messenger;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.View;

import org.telegram.ui.ActionBar.Theme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public final class UiFontManager implements Application.ActivityLifecycleCallbacks {

    private static final UiFontManager INSTANCE = new UiFontManager();

    private final Set<Activity> activities = Collections.newSetFromMap(new WeakHashMap<>());
    private boolean registered;

    private UiFontManager() {
    }

    public static void init(Application application) {
        if (!INSTANCE.registered) {
            INSTANCE.registered = true;
            application.registerActivityLifecycleCallbacks(INSTANCE);
        }
    }

    public static void applyToActivity(Activity activity) {
        AndroidUtilities.applyUiFontTheme(activity);
    }

    public static void refreshAllActivities(Activity currentActivity) {
        Theme.applyUiFont();

        ArrayList<Activity> activitySnapshot;
        synchronized (INSTANCE.activities) {
            activitySnapshot = new ArrayList<>(INSTANCE.activities);
        }
        for (Activity activity : activitySnapshot) {
            if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
                continue;
            }
            applyToActivity(activity);
            if (activity == currentActivity) {
                View decorView = activity.getWindow().getDecorView();
                decorView.requestLayout();
                decorView.invalidate();
            } else {
                activity.recreate();
            }
        }
    }

    @Override
    public void onActivityPreCreated(Activity activity, Bundle savedInstanceState) {
        applyToActivity(activity);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        synchronized (activities) {
            activities.add(activity);
        }
        applyToActivity(activity);
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        synchronized (activities) {
            activities.remove(activity);
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }
}

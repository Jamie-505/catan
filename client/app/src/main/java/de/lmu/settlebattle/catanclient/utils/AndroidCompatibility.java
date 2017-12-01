package de.lmu.settlebattle.catanclient;

/*
 * Copyright (C) 2015 Wglxy.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.lmu.settlebattle.catanclient.Constants;

import android.app.Activity;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

/**
 * This class provides backward compatibility for methods that were not supported in older version of Android.
 *
 * It includes the following as static methods:
 * (1) View.setSystemUiVisibility, which was introduced in API 11.
 * (2) supportsImmersiveMode
 * (3) turnOnImmersiveMode
 * (4) getDrawable (int, theme), which was introduced in API 21.
 */

public class AndroidCompatibility {

/*
 * History
 * 06-Oct-15 (wgl) Added this class.
 */


/**
 * Get a drawable, given a resource id and a theme.
 * 
 * @param res Resources
 * @param drawableId int
 * @param theme Theme
 * @return Drawable
 */

@SuppressWarnings("deprecation")
public static Drawable getDrawable (Resources res, int drawableId, Theme theme) {
    if (android.os.Build.VERSION.SDK_INT >= 21) {
       return res.getDrawable (drawableId, theme);
    } else {
      return res.getDrawable (drawableId);
    }
}

/**
 * Versions of Android after API 11 support setSystemUiVisibility.
 * If this variable isn't null, we can make the call.
 */

public static Method mSetSystemUiVisibility;

static {
 	try {
      mSetSystemUiVisibility = View.class.getMethod ("setSystemUiVisibility", 
                                                     new Class [] {Integer.TYPE} );
         /* success, this is a newer device */
     } catch (NoSuchMethodException nsme) {
         /* failure, must be older device */
     }    
};

/**
 * If the current device supports it, call setSystemUiVisibility.
 * Otherwise, do nothing.
 * 
 * @param v View
 * @param flags int
 * @return void
 */

public static void setSystemUiVisibility (View v, int flags) {
    if (mSetSystemUiVisibility != null) {
      try {
 	     mSetSystemUiVisibility.invoke (v, flags);
      } catch (InvocationTargetException ite) {
        Log.d (Constants.LOG_NAME, "AndroidCompatibility.setSystemUiVisibility: Invocation Target Exception");
      } catch (IllegalAccessException ie) {
        Log.d (Constants.LOG_NAME, "AndroidCompatibility.setSystemUiVisibility: Illegal Access Exception");
      }
    }
}

/**
 * Return true if immersive mode is supported on the current Android device.
 * The result depends on constant IMMERSIVE_MODE_ENABLED and the Android VERSION_SDK_INT value.
 *
 * @return boolean
 */

public static boolean supportsImmersiveMode () {
    return Constants.IMMERSIVE_MODE_ENABLED && (android.os.Build.VERSION.SDK_INT >= 19);
}

/**
 * Turn on immersive mode for this activity if the API level is 19 or higher.
 * (Immersive mode was not supported prior to KitKat.)
 *
 * <p> Call this in onCreate and onResume of Activity objects.
 * (FIX THIS: There are still glitches when you switch to fragments.)
 * 
 * @param a Activity
 * @param immersive boolean
 * @return void
 */

public static void turnOnImmersiveMode (Activity a, boolean immersive) {
    if (immersive && (android.os.Build.VERSION.SDK_INT >= 19)) {
       AndroidCompatibility.setSystemUiVisibility 
                   (a.getWindow ().getDecorView (),
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                   );
    }
}

public static void turnOnImmersiveMode (Activity a) {
    turnOnImmersiveMode (a, Constants.IMMERSIVE_MODE_ENABLED);
}

} // end class

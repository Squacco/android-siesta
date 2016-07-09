/*
 * Copyright 2016 Girish Kamath
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rhoadster91.android.siesta.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.rhoadster91.android.siesta.api.ApiExecutor;

import java.util.HashSet;
import java.util.Set;

public class CallbackManager {

    Set<ApiExecutor.ApiCallback> mCallbacks = new HashSet<>();

    public <T> ApiExecutor.ApiCallback<T> newCallback(ApiExecutor.ApiCallback<T> callback) {
        mCallbacks.add(callback);
        return callback;
    }

    public void unregisterAll() {
        mCallbacks.clear();
    }

    public void manage(Activity activity) {
        activity.getApplication().registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

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
                unregisterAll();
                activity.getApplication().unregisterActivityLifecycleCallbacks(this);
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                unregisterAll();
                activity.getApplication().unregisterActivityLifecycleCallbacks(this);
            }
        });
    }

}
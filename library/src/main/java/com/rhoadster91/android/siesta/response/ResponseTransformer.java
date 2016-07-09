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

package com.rhoadster91.android.siesta.response;

import com.rhoadster91.android.siesta.api.ApiExecutor;

public abstract class ResponseTransformer<EXPECTED, ACTUAL> implements ApiExecutor.ApiCallback<ACTUAL> {

    ApiExecutor.ApiCallback<EXPECTED> mActualCallback;

    public ResponseTransformer(ApiExecutor.ApiCallback<EXPECTED> actualApiCallback) {
        mActualCallback = actualApiCallback;
    }

    public abstract EXPECTED transform(ACTUAL actual) throws Throwable;

    @Override
    public void onSuccess(int responseCode, ACTUAL response) {
        if(mActualCallback!=null) {
            try {
                mActualCallback.onSuccess(responseCode, transform(response));
            } catch (Throwable throwable) {
                mActualCallback.onFailure(responseCode, null, throwable);
            }
        }
    }

    @Override
    public void onFailure(int responseCode, ACTUAL response, Throwable t) {
        if(mActualCallback!=null) {
            try {
                mActualCallback.onFailure(responseCode, transform(response), t);
            } catch (Throwable throwable) {
                mActualCallback.onFailure(responseCode, null, new Throwable(throwable.getMessage(), t));
            }
        }
    }
}

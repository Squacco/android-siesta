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

package com.rhoadster91.android.siesta.request;

import com.rhoadster91.android.siesta.api.Api;
import com.rhoadster91.android.siesta.ResponseWrapper;
import com.rhoadster91.android.siesta.api.capability.Gettable;

import java.util.Map;

public interface GetRequest<T> {
    <A extends Api & Gettable<T>> void get(A api, Map<String, String> params, Map<String, String> headers, ResponseWrapper<T> responseWrapper) throws Throwable;
}

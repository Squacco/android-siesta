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

package com.rhoadster91.android.siesta.api;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;

import com.rhoadster91.android.siesta.api.capability.Deletable;
import com.rhoadster91.android.siesta.api.capability.Gettable;
import com.rhoadster91.android.siesta.api.capability.Postable;
import com.rhoadster91.android.siesta.api.capability.Puttable;
import com.rhoadster91.android.siesta.request.RESTApiRequest;
import com.rhoadster91.android.siesta.response.ResponseWrapper;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiExecutor {

    public static final int MESSAGE_API_SUCCESS = 1;
    public static final int MESSAGE_API_FAILED = 2;

    public static final String KEY_API_SIGNATURE = ":apiexecutor:key:api:signature";
    public static final String KEY_API_RESPONSE_CODE = ":apiexecutor:key:api:response:code";
    public static final String KEY_API_EXCEPTION = ":apiexecutor:key:api:exception";


    public interface ApiCallback<T> {

        void onSuccess(int responseCode, T response);
        void onFailure(int responseCode, T response, Throwable t);

    }

    final Handler mUiHandler;

    Map<String, List<SoftReference<ApiCallback>>> mApiBus = new HashMap<>();
    final Object mWaitApiBus = new Object();

    final ExecutorService mExecutorService;

    public ApiExecutor(int threadPoolSize) {
        mExecutorService = Executors.newFixedThreadPool(threadPoolSize);
        mUiHandler = initUiHandler();
    }

    public ApiExecutor() {
        mExecutorService = Executors.newCachedThreadPool();
        mUiHandler = initUiHandler();
    }

    private Handler initUiHandler() {
        return new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Bundle bundle = msg.getData();
                String signature;
                ResponseWrapper response;
                int responseCode;
                switch (msg.what) {

                    case MESSAGE_API_FAILED:
                        signature = bundle.getString(KEY_API_SIGNATURE);
                        response = (ResponseWrapper) msg.obj;
                        responseCode = bundle.getInt(KEY_API_RESPONSE_CODE);
                        Throwable t = (Throwable) bundle.getSerializable(KEY_API_EXCEPTION);
                        notifyListeners(signature, false, response, responseCode, t);
                        break;

                    case MESSAGE_API_SUCCESS:
                        signature = bundle.getString(KEY_API_SIGNATURE);
                        response = (ResponseWrapper) msg.obj;
                        responseCode = bundle.getInt(KEY_API_RESPONSE_CODE);
                        notifyListeners(signature, true, response, responseCode, null);
                        break;
                }
                return false;
            }
        });
    }

    public <T, A extends Api & Gettable<T>> void get(A api, ApiCallback<T> apiCallback, Map<String, String> headers, Map<String, String> params) {
        submitApiTask(api, apiCallback, headers, params, RESTApiRequest.METHOD_GET, new ResponseWrapper<T>());
    }

    public <T, A extends Api & Gettable<T>> void get(A api, ApiCallback<T> apiCallback, Map<String, String> headers, String... params) {
        get(api, apiCallback, headers, toMap(params));
    }

    public <T, A extends Api & Puttable<T>> void put(A api, ApiCallback<T> apiCallback, Map<String, String> headers, Map<String, String> params) {
        submitApiTask(api, apiCallback, headers, params, RESTApiRequest.METHOD_PUT, new ResponseWrapper<T>());
    }

    public <T, A extends Api & Puttable<T>> void put(A api, ApiCallback<T> apiCallback, Map<String, String> headers, String... params) {
        put(api, apiCallback, headers, toMap(params));
    }

    public <T, A extends Api & Deletable<T>> void delete(A api, ApiCallback<T> apiCallback, Map<String, String> headers, Map<String, String> params) {
        submitApiTask(api, apiCallback, headers, params, RESTApiRequest.METHOD_DELETE, new ResponseWrapper<T>());
    }

    public <T, A extends Api & Deletable<T>> void delete(A api, ApiCallback<T> apiCallback, Map<String, String> headers, String... params) {
        delete(api, apiCallback, headers, toMap(params));
    }

    public <T, A extends Api & Postable<T>> void post(A api, ApiCallback<T> apiCallback, Map<String, String> headers, Map<String, String> params) {
        submitApiTask(api, apiCallback, headers, params, RESTApiRequest.METHOD_POST, new ResponseWrapper<T>());
    }

    public <T, A extends Api & Postable<T>> void post(A api, ApiCallback<T> apiCallback, Map<String, String> headers, String... params) {
        post(api, apiCallback, headers, toMap(params));
    }

    private void submitApiTask(Api api, ApiCallback apiCallback, Map<String, String> headers, Map<String, String> params, int method, ResponseWrapper responseWrapper) {
        boolean needRun = listenForCallback(api.getApiSignature(params), apiCallback);
        if(needRun) {
            ApiTask apiTask = new ApiTask(api, responseWrapper, method, new Messenger(mUiHandler));
            apiTask.withParams(params).withHeaders(headers);
            mExecutorService.submit(apiTask);
        }
    }

    private boolean listenForCallback(String signature, ApiCallback apiCallback) {
        synchronized (mWaitApiBus) {
            List<SoftReference<ApiCallback>> registeredCallbacks = mApiBus.get(signature);
            try {
                if (registeredCallbacks == null) {
                    registeredCallbacks = new ArrayList<>();
                    registeredCallbacks.add(new SoftReference<>(apiCallback));
                    mApiBus.put(signature, registeredCallbacks);
                    return true;
                } else {
                    registeredCallbacks.add(new SoftReference<>(apiCallback));
                    return false;
                }
            } finally {
                trimBus();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void notifyListeners(String signature, boolean success, ResponseWrapper response, int responseCode, Throwable t) {
        synchronized (mWaitApiBus) {
            List<SoftReference<ApiCallback>> callbacks = mApiBus.get(signature);
            if(callbacks == null) {
                return;
            }
            SoftReference<ApiCallback> callbackRef;
            for(Iterator<SoftReference<ApiCallback>> iterator = callbacks.iterator(); iterator.hasNext(); ) {
                callbackRef = iterator.next();
                ApiCallback callback = callbackRef.get();
                if(callback!=null) {
                    if (success) {
                        callback.onSuccess(responseCode, response.getResponse());
                    } else {
                        callback.onFailure(responseCode, response.getResponse(), t);
                    }
                } else {
                    iterator.remove();
                }
            }
            mApiBus.remove(signature);
        }

    }

    private void trimBus() {
        synchronized (mWaitApiBus) {
            Iterator<Map.Entry<String, List<SoftReference<ApiCallback>>>> iterator = mApiBus.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, List<SoftReference<ApiCallback>>> entry = iterator.next();
                if (entry.getValue() == null || entry.getValue().size() == 0) {
                    iterator.remove();
                }
            }
        }
    }

    public Map<String, String> toMap(String... params) {
        Map<String, String> map = new TreeMap<>();
        for(int i = 0; i < params.length; i = i + 2) {
            if(params.length == i + 1) {
                map.put(params[i], null);
            } else {
                map.put(params[i], params[i+1]);
            }
        }
        return map;
    }
}
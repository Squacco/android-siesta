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
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.rhoadster91.android.siesta.api.capability.Deletable;
import com.rhoadster91.android.siesta.api.capability.Gettable;
import com.rhoadster91.android.siesta.api.capability.Postable;
import com.rhoadster91.android.siesta.api.capability.Puttable;
import com.rhoadster91.android.siesta.request.RESTApiRequest;
import com.rhoadster91.android.siesta.response.ResponseWrapper;

import java.util.Map;
import java.util.TreeMap;

class ApiTask implements Runnable {

    private Messenger mCallbackMessenger;
    private Api api;
    private int method;
    private ResponseWrapper responseWrapper;

    public ApiTask(Api api, ResponseWrapper responseWrapper, int method, Messenger callbackMessenger) {
        mCallbackMessenger = callbackMessenger;
        this.api = api;
        this.responseWrapper = responseWrapper;
        this.method = method;
        if(api.getDefaultHeaders()!=null) {
            headers.putAll(api.getDefaultHeaders());
        }
    }

    Map<String, String> params = new TreeMap<>();
    Map<String, String> headers = new TreeMap<>();

    public ApiTask withParams(Map<String, String> params)  {
        if(params!=null) {
            this.params.putAll(params);
        }
        return this;
    }

    public ApiTask withHeaders(Map<String, String> headers)  {
        if(headers!=null) {
            this.params.putAll(headers);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        boolean isSuccess = false;
        try {
            switch (method) {
                case RESTApiRequest.METHOD_GET:
                    if(api instanceof Gettable) {
                        ((Gettable) api).newGetRequest().get(api, params, headers, responseWrapper);
                        isSuccess = ((Gettable) api).isGetSuccess(responseWrapper.getCode(), responseWrapper.getResponse());
                    }
                    break;
                case RESTApiRequest.METHOD_POST:
                    if(api instanceof Postable) {
                        ((Postable) api).newPostRequest().post(api, params, headers, responseWrapper);
                        isSuccess = ((Postable) api).isPostSuccess(responseWrapper.getCode(), responseWrapper.getResponse());
                    }
                    break;
                case RESTApiRequest.METHOD_PUT:
                    if(api instanceof Puttable) {
                        ((Puttable) api).newPutRequest().put(api, params, headers, responseWrapper);
                        isSuccess = ((Puttable) api).isPutSuccess(responseWrapper.getCode(), responseWrapper.getResponse());
                    }
                    break;
                case RESTApiRequest.METHOD_DELETE:
                    if(api instanceof Deletable) {
                        ((Deletable) api).newDeleteRequest().delete(api, params, headers, responseWrapper);
                        isSuccess = ((Deletable) api).isDeleteSuccess(responseWrapper.getCode(), responseWrapper.getResponse());
                    }
                    break;
            }
        } catch (Throwable t) {
            responseWrapper.setThrowable(t);
        }
        postResponse(responseWrapper, isSuccess);
    }

    private void postResponse(ResponseWrapper responseWrapper, boolean isSuccess) {
        Message message = Message.obtain();
        Bundle data = new Bundle();
        data.putString(ApiExecutor.KEY_API_SIGNATURE, api.getApiSignature(params));
        data.putInt(ApiExecutor.KEY_API_RESPONSE_CODE, responseWrapper.getCode());
        data.putSerializable(ApiExecutor.KEY_API_EXCEPTION, responseWrapper.getThrowable());
        message.what = isSuccess?ApiExecutor.MESSAGE_API_SUCCESS:ApiExecutor.MESSAGE_API_FAILED;
        message.setData(data);
        message.obj = responseWrapper;
        try {
            mCallbackMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }



}
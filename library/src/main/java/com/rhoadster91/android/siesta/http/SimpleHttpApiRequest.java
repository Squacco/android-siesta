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

package com.rhoadster91.android.siesta.http;

import com.rhoadster91.android.siesta.api.Api;
import com.rhoadster91.android.siesta.request.RESTApiRequest;
import com.rhoadster91.android.siesta.response.ResponseWrapper;
import com.rhoadster91.android.siesta.api.capability.Deletable;
import com.rhoadster91.android.siesta.api.capability.Gettable;
import com.rhoadster91.android.siesta.api.capability.Postable;
import com.rhoadster91.android.siesta.api.capability.Puttable;
import com.rhoadster91.android.siesta.request.DeleteRequest;
import com.rhoadster91.android.siesta.request.GetRequest;
import com.rhoadster91.android.siesta.request.PostRequest;
import com.rhoadster91.android.siesta.request.PutRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class SimpleHttpApiRequest extends RESTApiRequest implements GetRequest<String>, PostRequest<String>, PutRequest<String>, DeleteRequest<String> {

    private void doRequest(Api api, Map<String, String> params, Map<String, String> headers, int method, ResponseWrapper<String> responseWrapper) throws IOException {
        int responseCode;
        URL url;
        HttpURLConnection urlConnection;

        if (method == METHOD_GET || method == METHOD_DELETE) {
            url = new URL(api.processGetParams(params));
        } else {
            url = new URL(api.getApiUrl());
        }


        urlConnection = (HttpURLConnection) url
                .openConnection();


        for(Map.Entry<String, String> header : headers.entrySet()) {
            urlConnection.setRequestProperty(header.getKey(), header.getValue());
        }

        urlConnection.setRequestMethod(getMethodAsString(method));

        if (method == METHOD_POST || method == METHOD_PUT) {
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            OutputStream os = urlConnection.getOutputStream();
            os.write(api.processPutParams(params).getBytes("UTF-8"));
            os.flush();
            os.close();
        }

        responseCode = urlConnection.getResponseCode();
        responseWrapper.setCode(responseCode);
        InputStream in = urlConnection.getInputStream();

        InputStreamReader isw = new InputStreamReader(in);
        StringBuilder stringBuilder = new StringBuilder();
        int data = isw.read();
        while (data != -1) {
            char current = (char) data;
            data = isw.read();
            stringBuilder.append(current);
        }
        responseWrapper.setResponse(stringBuilder.toString());
    }

    private String getMethodAsString(int method) {
        switch (method) {
            case METHOD_DELETE:
                return "DELETE";
            case METHOD_PUT:
                return "PUT";
            case METHOD_POST:
                return "POST";
            default:
            case METHOD_GET:
                return "GET";
        }
    }


    @Override
    public <A extends Api & Gettable<String>> void get(A api, Map<String, String> params, Map<String, String> headers, ResponseWrapper<String> responseWrapper) throws Throwable {
        doRequest(api, params, headers, METHOD_GET, responseWrapper);
    }

    @Override
    public <A extends Api & Deletable<String>> void delete(A api, Map<String, String> params, Map<String, String> headers, ResponseWrapper<String> responseWrapper) throws Throwable {
        doRequest(api, params, headers, METHOD_DELETE, responseWrapper);
    }

    @Override
    public <A extends Api & Postable<String>> void post(A api, Map<String, String> params, Map<String, String> headers, ResponseWrapper<String> responseWrapper) throws Throwable {
        doRequest(api, params, headers, METHOD_POST, responseWrapper);
    }

    @Override
    public <A extends Api & Puttable<String>> void put(A api, Map<String, String> params, Map<String, String> headers, ResponseWrapper<String> responseWrapper) throws Throwable {
        doRequest(api, params, headers, METHOD_PUT, responseWrapper);
    }
}
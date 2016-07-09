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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public abstract class Api {
    public abstract String getApiUrl();

    public String processGetParams(Map<String, String> params) {
        StringBuilder getParamUrl = new StringBuilder();
        getParamUrl.append(getApiUrl());
        getParamUrl.append("?");
        for(Map.Entry<String, String> entry : params.entrySet()) {
            try {
                String paramEncoded = URLEncoder.encode(entry.getValue(), "UTF-8");
                getParamUrl.append(entry.getKey()).append("=").append(paramEncoded).append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return getParamUrl.toString();
    }

    public String processPutParams(Map<String, String> params) {
        JSONObject jsonObject = new JSONObject();
        for(Map.Entry<String, String> entry : params.entrySet()) {
            try {
                jsonObject.put(entry.getKey(), entry.getValue());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonObject.toString();
    }

    public String getApiSignature(Map<String, String> params) {
        return processGetParams(params);
    }

    public Map<String, String> getDefaultHeaders() {
        return null;
    }

}
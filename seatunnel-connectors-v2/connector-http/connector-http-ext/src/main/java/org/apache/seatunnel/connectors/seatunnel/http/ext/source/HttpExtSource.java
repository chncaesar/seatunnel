/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.seatunnel.connectors.seatunnel.http.ext.source;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.seatunnel.api.configuration.ReadonlyConfig;
import org.apache.seatunnel.api.table.type.SeaTunnelRow;
import org.apache.seatunnel.connectors.seatunnel.common.source.AbstractSingleSplitReader;
import org.apache.seatunnel.connectors.seatunnel.common.source.SingleSplitReaderContext;
import org.apache.seatunnel.connectors.seatunnel.http.config.HttpCommonOptions;
import org.apache.seatunnel.connectors.seatunnel.http.config.HttpRequestMethod;
import org.apache.seatunnel.connectors.seatunnel.http.config.HttpSourceOptions;
import org.apache.seatunnel.connectors.seatunnel.http.exception.HttpConnectorErrorCode;
import org.apache.seatunnel.connectors.seatunnel.http.exception.HttpConnectorException;
import org.apache.seatunnel.connectors.seatunnel.http.ext.config.HttpExtSourceOptions;
import org.apache.seatunnel.connectors.seatunnel.http.source.HttpSource;
import org.apache.seatunnel.connectors.seatunnel.http.source.HttpSourceReader;
import org.apache.seatunnel.shade.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.seatunnel.shade.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.seatunnel.connectors.seatunnel.http.exception.HttpConnectorErrorCode.*;
import static org.apache.seatunnel.connectors.seatunnel.http.ext.config.AuthType.BEARER;
import static org.apache.seatunnel.connectors.seatunnel.http.ext.config.HttpExtSourceOptions.*;
import static org.apache.seatunnel.shade.com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

@Slf4j
public class HttpExtSource extends HttpSource {

    public static final String PLUGIN_NAME = "HttpExt";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        // Ignore unknown properties in JSON deserialization
        MAPPER.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    public HttpExtSource(ReadonlyConfig pluginConfig) {
        super(pluginConfig);
        buildAuthentication(pluginConfig);
    }


    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    public AbstractSingleSplitReader<SeaTunnelRow> createReader(
            SingleSplitReaderContext readerContext) throws Exception {

        return new HttpSourceReader(
                this.httpParameter,
                readerContext,
                this.deserializationSchema,
                jsonField,
                contentField,
                pageInfo);
    }

    private void buildAuthentication(ReadonlyConfig pluginConfig) throws HttpConnectorException {

        // Extract authentication parameters from pluginConfig
        if (! pluginConfig.getOptional(HttpExtSourceOptions.API_AUTH).isPresent()) {
            throw new HttpConnectorException(HttpConnectorErrorCode.FIELD_AUTH_IS_REQUIRED,
                    "The 'auth' field is required in the HTTP source configuration.");
        }
        Map<String, Object> authMap = pluginConfig.getOptional(HttpExtSourceOptions.API_AUTH).get();
        if (authMap.isEmpty()) {
            throw new HttpConnectorException(HttpConnectorErrorCode.AUTH_CONTENT_IS_REQUIRED,
                    "The auth content cannot be empty in the HTTP source configuration.");
        }
        log.info("Auth size: {}", authMap.size());
        if( ! authMap.containsKey(AUTH_TYPE.key())) {
            log.warn("The 'auth-type' field is not set in the HTTP source configuration");
            return;
        }
        String authType = (String) authMap.get(AUTH_TYPE.key());
        if (! BEARER.getName().equals(authType)) {
            throw new HttpConnectorException(AUTH_TYPE_NOT_SUPPORTED, authType);
        }
        if( ! authMap.containsKey(USERNAME.key())) {
            throw new HttpConnectorException(FIELD_USERNAME_IS_REQUIRED,
                    "The 'username' field is required for basic authentication in the HTTP source configuration.");
        }

        if (! authMap.containsKey(PASSWORD.key())) {
            throw new HttpConnectorException(HttpConnectorErrorCode.FIELD_PASSWORD_IS_REQUIRED,
                    "The 'password' field is required for basic authentication in the HTTP source configuration.");
        }
        if(! authMap.containsKey(HttpCommonOptions.URL.key()) ) {
            throw new HttpConnectorException(HttpConnectorErrorCode.FIELD_URL_IS_REQUIRED,
                    "The 'url' field is required in the HTTP source configuration.");
        }
        if( ! authMap.containsKey(RESULT_FIELD.key())) {
            throw new HttpConnectorException(HttpConnectorErrorCode.FIELD_RESULT_FIELD_IS_REQUIRED,
                    "The 'result_field' field is required in the HTTP source configuration.");
        }
        String contentType = (String)authMap.getOrDefault(CONTENT_TYPE.key(),"application/json");
        String method = ((String)authMap.getOrDefault(HttpSourceOptions.METHOD.key(), "post")).toUpperCase();
        String username = (String)authMap.get(USERNAME.key());
        String password = (String)authMap.get(PASSWORD.key());
        String url = (String)authMap.get(URL.key());
        String resultField = (String)authMap.get(RESULT_FIELD.key());
        log.info("contentType: {}, method: {}, username: {}, url: {}, resultField: {}", contentType, method, username, url, resultField);
        // Send http request to the url and get result from response body
        OutputStream os = null;
        InputStream is = null;
        HttpURLConnection conn = null;
        String responseBody = null;
        try {
            URL requestUrl = new java.net.URL(url);
            conn = (HttpURLConnection) requestUrl.openConnection();
            conn.setRequestMethod(method);
            conn.setRequestProperty("Content-Type", contentType);
            conn.setDoOutput(true);
            String requestBody = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
            os = conn.getOutputStream();
            os.write(requestBody.getBytes(UTF_8));
            os.flush();
            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                is = conn.getInputStream();
            } else {
                is = conn.getErrorStream();
            }
            Scanner s = new Scanner(is).useDelimiter("\\A");
            responseBody = s.hasNext() ? s.next() : "";
            log.info("HTTP request successful, response body: {}", responseBody);
        } catch (Exception e) {
            log.error("Failed to send HTTP request", e);
            throw new HttpConnectorException(HTTP_REQUEST_FAILED, "Failed to send HTTP request: " + e.getMessage(), e);
        } finally {
            if (os != null) {
                try { os.close(); } catch (Exception ignore) {}
            }
            if (is != null) {
                try { is.close(); } catch (Exception ignore) {}
            }
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            if (StringUtils.isNotEmpty(responseBody) ) {
                // Parse responseBody as JSON
                Map<String, Object> jsonMap = null;
                jsonMap = MAPPER.readValue(responseBody, Map.class);

                String[] path = resultField.split("\\.");
                Object value = jsonMap;
                for (String key : path) {
                    if (key.equals("$")) {
                        // Skip the root element
                        continue;
                    }
                    if (value instanceof Map) {
                        value = ((Map) value).get(key);
                    } else {
                        value = null;
                        break;
                    }
                }
                if (value != null) {
                    // Set the Authorization header for subsequent requests
                    if(this.httpParameter.getHeaders() == null) {
                        this.httpParameter.setHeaders(new HashMap<>());
                    }
                    this.httpParameter.getHeaders().put("Authorization", "Bearer " + value);
                    log.info("Authorization header set successfully with value: {}", value);
                }
            }
        }
        catch (JsonProcessingException e) {
            log.error("Failed to parse response body as JSON", e);
            throw new HttpConnectorException(HTTP_RESPONSE_PROCESS_FAILED, e.getMessage());
        }
    }
}

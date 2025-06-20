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

package org.apache.seatunnel.connectors.seatunnel.http.exception;

import org.apache.seatunnel.common.exception.SeaTunnelErrorCode;

public enum HttpConnectorErrorCode implements SeaTunnelErrorCode {
    FIELD_DATA_IS_INCONSISTENT("HTTP-01", "The field data is inconsistent"),
    REQUEST_FAILED("HTTP-02", "The request is failed"),
    FIELD_AUTH_IS_REQUIRED("HTTP-03", "Auth is required"),
    AUTH_CONTENT_IS_REQUIRED("HTTP-04", "Auth content is required"),
    FIELD_USERNAME_IS_REQUIRED("HTTP-05", "Username is required"),
    FIELD_PASSWORD_IS_REQUIRED("HTTP-06", "Password is required"),
    FIELD_URL_IS_REQUIRED("HTTP-07", "URL is required"),
    HTTP_REQUEST_FAILED("HTTP-08", "HTTP request failed"),
    HTTP_RESPONSE_PROCESS_FAILED("HTTP-09", "HTTP response process failed"),
    FIELD_RESULT_FIELD_IS_REQUIRED("HTTP-10", "Result field is required"),
    AUTH_TYPE_NOT_SUPPORTED("HTTP-11", "Auth type is not supported"),
    X_TENANT_ID_IS_REQUIRED("HTTP-12", "X-tenant-id is required");

    private final String code;
    private final String description;

    HttpConnectorErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }
}

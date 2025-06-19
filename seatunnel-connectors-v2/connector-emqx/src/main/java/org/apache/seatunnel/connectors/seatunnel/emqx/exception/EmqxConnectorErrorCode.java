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


package org.apache.seatunnel.connectors.seatunnel.emqx.exception;

import org.apache.seatunnel.common.exception.SeaTunnelErrorCode;

public enum EmqxConnectorErrorCode implements SeaTunnelErrorCode {
    FAILED_TO_CONNECT("EMQX-01", "Failed to connect to EMQX broker"),
    FAILED_TO_CLOSE("EMQX-02", "Failed to close EMQX client connection"),
    FAILED_TO_PUBLISH("EMQX-03", "Failed to publish message to EMQX topic");
    private final String code;
    private final String description;

    EmqxConnectorErrorCode(String code, String description) {
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

    @Override
    public String getErrorMessage() {
        return SeaTunnelErrorCode.super.getErrorMessage();
    }
}

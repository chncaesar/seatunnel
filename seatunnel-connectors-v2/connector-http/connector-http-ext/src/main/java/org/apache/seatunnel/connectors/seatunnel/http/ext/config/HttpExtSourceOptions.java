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

package org.apache.seatunnel.connectors.seatunnel.http.ext.config;

import org.apache.seatunnel.api.configuration.Option;
import org.apache.seatunnel.api.configuration.Options;
import org.apache.seatunnel.connectors.seatunnel.http.config.HttpCommonOptions;

import java.util.Map;

public class HttpExtSourceOptions extends HttpCommonOptions {
    public static final Option<Map<String, Object>> API_AUTH =
            Options.key("api-auth").mapObjectType()
                    .noDefaultValue()
                    .withDescription(
                            "Http auth configuration");
    public static final Option<String> AUTH_TYPE =
            Options.key("auth-type")
                    .stringType()
                    .defaultValue("basic")
                    .withDescription("Http auth type, support basic and bearer");

    public static final Option<String> CONTENT_TYPE =
            Options.key("Content-Type")
                    .stringType()
                    .defaultValue("application/json")
                    .withDescription("Http content type, default is application/json");

    public static final Option<String> USERNAME =
            Options.key("username")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("Http username for basic auth");

    public static final Option<String> PASSWORD =
            Options.key("password")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("Http password for basic auth");
    public static final Option<String> RESULT_FIELD =
            Options.key("result_field")
                    .stringType()
                    .noDefaultValue()
                    .withDescription(
                            "The field json path in the response body that contains the result data.");
}

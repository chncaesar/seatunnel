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

package org.apache.seatunnel.connectors.seatunnel.emqx.config;

import org.apache.seatunnel.api.configuration.Option;
import org.apache.seatunnel.api.configuration.Options;

public class EmqxOptions {
    public static final Option<String> BROKER =
            Options.key("broker")
                    .stringType()
                    .noDefaultValue()
                    .withDescription(
                            "EMQX broker address, e.g. tcp://");

    public static final Option<String> TOPIC =
            Options.key("topic")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("The topic to which the message will be sent.");

    public static final Option<Integer> QOS =
            Options.key("qos")
                    .intType()
                    .defaultValue(0)
                    .withDescription(
                            "Quality of Service level for the message. 0: At most once, 1: At least once, 2: Exactly once.");

    public static final Option<String> USERNAME =
            Options.key("username")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("Username for authentication with the EMQX broker.");

    public static final Option<String> PASSWORD =
            Options.key("password")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("Password for authentication with the EMQX broker.");

    public static final Option<String> CLIENT_ID =
            Options.key("client_id")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("Client ID for the EMQX connection, used to identify the client.");

    public static final Option<Boolean> CLEAN_SESSION =
            Options.key("clean_session")
                    .booleanType()
                    .defaultValue(true)
                    .withDescription(
                            "If true, the broker will not store any session information for this client. If false, the broker will store session information.");

    public static final Option<Boolean> AUTOMATIC_RECONNECT =
            Options.key("automatic_reconnect")
                    .booleanType()
                    .defaultValue(true)
                    .withDescription(
                            "If true, the client will automatically attempt to reconnect to the broker if the connection is lost.");

    public static final Option<Integer> BATCH_SIZE =
            Options.key("batch_size")
                    .intType()
                    .defaultValue(100)
                    .withDescription(
                            "The number of messages to batch before sending them to the EMQX broker. This can improve performance by reducing the number of network calls.");

}

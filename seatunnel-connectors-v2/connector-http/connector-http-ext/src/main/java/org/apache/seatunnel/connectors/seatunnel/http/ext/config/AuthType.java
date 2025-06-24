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

public enum AuthType {
    BASIC("basic"),
    BEARER("bearer"),
    X_ACCESS_TOKEN("X-access-token"),
    SET_COOKIE("Set-Cookie");

    private final String name;

    AuthType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static AuthType fromName(String name) {
        for (AuthType type : AuthType.values()) {
            if (type.name.equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown auth type: " + name);
    }
}

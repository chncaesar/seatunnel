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

import org.apache.seatunnel.api.serialization.DeserializationSchema;
import org.apache.seatunnel.api.table.type.SeaTunnelRow;
import org.apache.seatunnel.connectors.seatunnel.common.source.SingleSplitReaderContext;
import org.apache.seatunnel.connectors.seatunnel.http.config.HttpParameter;
import org.apache.seatunnel.connectors.seatunnel.http.config.JsonField;
import org.apache.seatunnel.connectors.seatunnel.http.config.PageInfo;
import org.apache.seatunnel.connectors.seatunnel.http.source.HttpSourceReader;

public class HttpExtSourceReader extends HttpSourceReader {
    public HttpExtSourceReader(HttpParameter httpParameter, SingleSplitReaderContext context,
                               DeserializationSchema<SeaTunnelRow> deserializationSchema, JsonField jsonField,
                               String contentJson) {
        super(httpParameter, context, deserializationSchema, jsonField, contentJson);
    }

    public HttpExtSourceReader(HttpParameter httpParameter, SingleSplitReaderContext context,
                               DeserializationSchema<SeaTunnelRow> deserializationSchema, JsonField jsonField,
                               String contentJson, PageInfo pageInfo) {
        super(httpParameter, context, deserializationSchema, jsonField, contentJson, pageInfo);
    }

    /**
     *  "auth": {
     *   "jwt": {
     *     "url": "https://example.com/api/auth",
     *     "method": "POST",
     *     "Content-Type": "application/json",
     *     "username": "admin",
     *     "password": "lianrui123"
     *   }
     * }
     * If Config contains "auth" and "jwt", then the init method will be called to initialize the authentication.
     */
    private void init() {
        //
    }
}

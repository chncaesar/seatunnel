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

package org.apache.seatunnel.connectors.seatunnel.hik.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class SignUtil {

    public static String sign(
            String secret,
            String method,
            String path,
            Map<String, String> headers,
            Map<String, String> query,
            Map<String, String> body,
            List<String> signHeaderPrefixList) {
        try {
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            byte[] keyBytes = secret.getBytes(UTF_8);
            hmacSha256.init(new SecretKeySpec(keyBytes, 0, keyBytes.length, "HmacSHA256"));
            String stringToSign =
                    buildStringToSign(method, path, headers, query, body, signHeaderPrefixList);
            if (log.isDebugEnabled()) {
                log.debug("StringToSign:\n{}", stringToSign);
            }

            return new String(
                    Base64.encodeBase64(hmacSha256.doFinal(stringToSign.getBytes(UTF_8))), UTF_8);
        } catch (Exception e) {
            log.error("Error signing request", e);
            throw new RuntimeException(e);
        }
    }

    private static String buildStringToSign(
            String method,
            String path,
            Map<String, String> headers,
            Map<String, String> query,
            Map<String, String> body,
            List<String> signHeaderPrefixList) {
        StringBuilder sb = new StringBuilder();
        sb.append(method.toUpperCase()).append("\n");
        if (null != headers) {
            if (null != headers.get("Accept")) {
                sb.append(headers.get("Accept"));
                sb.append("\n");
            }

            if (null != headers.get("Content-MD5")) {
                sb.append(headers.get("Content-MD5"));
                sb.append("\n");
            }

            if (null != headers.get("Content-Type")) {
                String contentType = headers.get("Content-Type");
                if (contentType.contains("boundary")) {
                    String[] strings = contentType.split(";");

                    for (String string : strings) {
                        if (!string.contains("boundary")) {
                            sb.append(string);
                            sb.append(";");
                        }
                    }
                    sb.deleteCharAt(sb.toString().length() - 1);
                } else {
                    sb.append(contentType);
                }

                sb.append("\n");
            }

            if (null != headers.get("Date")) {
                sb.append(headers.get("Date"));
                sb.append("\n");
            }
        }

        sb.append(buildHeaders(headers, signHeaderPrefixList));
        sb.append(buildResource(path, query, body));
        return sb.toString();
    }

    private static String buildResource(
            String path, Map<String, String> query, Map<String, String> body) {
        StringBuilder sb = new StringBuilder();
        if (!StringUtils.isBlank(path)) {
            sb.append(path);
        }

        Map<String, String> sortMap = new TreeMap();
        if (null != query) {
            for (Map.Entry<String, String> q : query.entrySet()) {
                if (!StringUtils.isBlank(q.getKey())) {
                    sortMap.put(q.getKey(), q.getValue());
                }
            }
        }

        if (null != body) {
            for (Map.Entry<String, String> b : body.entrySet()) {
                if (!StringUtils.isBlank(b.getKey())) {
                    sortMap.put(b.getKey(), b.getValue());
                }
            }
        }

        StringBuilder sbParam = new StringBuilder();

        for (Map.Entry<String, String> item : sortMap.entrySet()) {
            if (!StringUtils.isBlank(item.getKey())) {
                if (0 < sbParam.length()) {
                    sbParam.append("&");
                }

                sbParam.append((String) item.getKey());
                if (!StringUtils.isBlank((CharSequence) item.getValue())) {
                    sbParam.append("=").append((String) item.getValue());
                }
            }
        }

        if (0 < sbParam.length()) {
            sb.append("?");
            sb.append(sbParam);
        }

        return sb.toString();
    }

    private static String buildHeaders(
            Map<String, String> headers, List<String> signHeaderPrefixList) {
        StringBuilder sb = new StringBuilder();
        if (null != signHeaderPrefixList) {
            signHeaderPrefixList.remove("x-ca-signature");
            signHeaderPrefixList.remove("Accept");
            signHeaderPrefixList.remove("Content-MD5");
            signHeaderPrefixList.remove("Content-Type");
            signHeaderPrefixList.remove("Date");
            Collections.sort(signHeaderPrefixList);
        }

        if (null != headers) {
            Map<String, String> sortMap = new TreeMap();
            sortMap.putAll(headers);
            StringBuilder signHeadersStringBuilder = new StringBuilder();

            for (Map.Entry<String, String> header : sortMap.entrySet()) {
                if (isHeaderToSign(header.getKey(), signHeaderPrefixList)) {
                    sb.append(header.getKey());
                    sb.append(":");
                    if (!StringUtils.isBlank((CharSequence) header.getValue())) {
                        sb.append((String) header.getValue());
                    }

                    sb.append("\n");
                    if (0 < signHeadersStringBuilder.length()) {
                        signHeadersStringBuilder.append(",");
                    }

                    signHeadersStringBuilder.append(header.getKey());
                }
            }

            headers.put("x-ca-signature-headers", signHeadersStringBuilder.toString());
        }

        return sb.toString();
    }

    private static boolean isHeaderToSign(String headerName, List<String> signHeaderPrefixList) {
        if (StringUtils.isBlank(headerName)) {
            return false;
        } else if (headerName.startsWith("x-ca-")) {
            return true;
        } else {
            if (null != signHeaderPrefixList) {
                for (String signHeaderPrefix : signHeaderPrefixList) {
                    if (headerName.equalsIgnoreCase(signHeaderPrefix)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}

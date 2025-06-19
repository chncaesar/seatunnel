package org.apache.seatunnel.connectors.seatunnel.hik.source;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.seatunnel.api.configuration.ReadonlyConfig;
import org.apache.seatunnel.api.serialization.DeserializationSchema;
import org.apache.seatunnel.api.source.Collector;
import org.apache.seatunnel.api.table.type.SeaTunnelRow;
import org.apache.seatunnel.connectors.seatunnel.common.source.AbstractSingleSplitReader;
import org.apache.seatunnel.connectors.seatunnel.common.source.SingleSplitReaderContext;
import org.apache.seatunnel.connectors.seatunnel.hik.config.HikOptions;
import org.apache.seatunnel.connectors.seatunnel.hik.exception.HikConnectorErrorCode;
import org.apache.seatunnel.connectors.seatunnel.hik.util.SignUtil;
import org.apache.seatunnel.connectors.seatunnel.http.config.HttpParameter;
import org.apache.seatunnel.connectors.seatunnel.http.config.JsonField;
import org.apache.seatunnel.connectors.seatunnel.http.config.PageInfo;
import org.apache.seatunnel.connectors.seatunnel.http.exception.HttpConnectorErrorCode;
import org.apache.seatunnel.connectors.seatunnel.http.exception.HttpConnectorException;
import org.apache.seatunnel.connectors.seatunnel.http.source.HttpSourceHook;
import org.apache.seatunnel.connectors.seatunnel.http.source.HttpSourceReader;
import org.apache.seatunnel.shade.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.seatunnel.shade.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;

@Slf4j
@SuppressWarnings("unchecked")
public class HikSourceReader extends AbstractSingleSplitReader<SeaTunnelRow> implements HttpSourceHook {
    private final HttpSourceReader httpSourceReader;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public HikSourceReader(HttpParameter httpParameter,
                           SingleSplitReaderContext context,
                           DeserializationSchema<SeaTunnelRow> deserializationSchema,
                           JsonField jsonField,
                           String contentJson,
                           PageInfo pageInfo,
                           ReadonlyConfig pluginConfig) {
        httpSourceReader = new HttpSourceReader(httpParameter, context, deserializationSchema, jsonField, contentJson, pageInfo, pluginConfig);
        httpSourceReader.addHook(this);
    }


    @Override
    public void open() throws Exception {
        this.httpSourceReader.open();
    }

    @Override
    public void close() throws IOException {
        this.httpSourceReader.close();
    }

    @Override
    public void pollNext(Collector<SeaTunnelRow> output) throws Exception {
        this.httpSourceReader.pollNext(output);
    }

    @Override
    public void beforeRequest(ReadonlyConfig pluginConfig) {
        HttpParameter httpParameter = httpSourceReader.getHttpParameter();
        if(httpParameter.getHeaders().containsKey("x-ca-signature")) {
            return;
        }
        String path = pluginConfig.get(HikOptions.PATH);
        httpParameter.setUrl(pluginConfig.get(HikOptions.HOST) + path);
        httpParameter.setHeaders(
                Optional.ofNullable(httpParameter.getHeaders()).orElse(new HashMap<>()));
        // Setup Content-Type
        if (!httpParameter.getHeaders().containsKey("Content-Type")) {
            httpParameter.getHeaders().put("Content-Type", "application/text;charset=UTF-8");
        }

        Map<String, String> bodyMap = new HashMap<>();
        try {
            if (!StringUtils.isEmpty(httpParameter.getBody())) {
                bodyMap = OBJECT_MAPPER.readValue(httpParameter.getBody(), Map.class);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse request body JSON: {}", httpParameter.getBody(), e);
            throw new HttpConnectorException(
                    HikConnectorErrorCode.INVALID_REQUEST_BODY_JSON,
                    "Invalid request body JSON format.",
                    e);
        }
        // Setup Accept
        if (!httpParameter.getHeaders().containsKey("Accept")) {
            httpParameter.getHeaders().put("Accept", "*/*");
        }
        String appKey = pluginConfig.get(HikOptions.AK);
        String sk = pluginConfig.get(HikOptions.SK);
        // Sign the request
        httpParameter.getHeaders().put("Hik-Request-ID", "artemis&artemis-http-client&1.1.6");
        httpParameter.getHeaders().put("x-ca-timestamp", String.valueOf((new Date()).getTime()));
        httpParameter.getHeaders().put("x-ca-nonce", UUID.randomUUID().toString());
        httpParameter.getHeaders().put("x-ca-key", appKey);

        String contentType = (String)httpParameter.getHeaders().get("Content-Type");
        String sign = null;
        if ("application/x-www-form-urlencoded;charset=UTF-8".equals(contentType)) {
            String modelDatas = (String)bodyMap.get("modelDatas");
            if (StringUtils.isNotBlank(modelDatas)) {
                bodyMap.put("modelDatas", URLDecoder.decode(modelDatas));
            }

            sign = SignUtil.sign(sk,
                httpParameter.getMethod().getMethod(),
                path,
                httpParameter.getHeaders(),
                httpParameter.getParams(),
                bodyMap,
                null);
        } else {
            sign = SignUtil.sign(
                    sk,
                    httpParameter.getMethod().getMethod(),
                    path,
                    httpParameter.getHeaders(),
                    httpParameter.getParams(),
                    null,
                    null);
        }
        httpParameter.getHeaders().put("x-ca-signature", sign);
        log.info("Hik sign finished");
    }

    @Override
    public void afterRequest(ReadonlyConfig pluginConfig, String contentData) {
        if(StringUtils.isEmpty(contentData)) {
            return;
        }
        try {
            Map<String, Object> contentMap = OBJECT_MAPPER.readValue(contentData, Map.class);
            if(! "0".equals( contentMap.getOrDefault("code", "0"))) {
                log.error("Response contentData {}", contentData);
                String msg = (String)contentMap.getOrDefault("msg", "error ");
                throw new HttpConnectorException(HttpConnectorErrorCode.REQUEST_FAILED, msg);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse contentData to Map {}", contentData);
            throw new HttpConnectorException(HttpConnectorErrorCode.REQUEST_FAILED, e);
        }
    }
}

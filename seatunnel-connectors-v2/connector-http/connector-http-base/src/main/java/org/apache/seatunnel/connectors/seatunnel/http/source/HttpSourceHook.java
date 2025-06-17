package org.apache.seatunnel.connectors.seatunnel.http.source;

import org.apache.seatunnel.api.configuration.ReadonlyConfig;

public interface HttpSourceHook {
    void beforeRequest(ReadonlyConfig pluginConfig);
    void afterRequest(ReadonlyConfig pluginConfig, String contentData);
}

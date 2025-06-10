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

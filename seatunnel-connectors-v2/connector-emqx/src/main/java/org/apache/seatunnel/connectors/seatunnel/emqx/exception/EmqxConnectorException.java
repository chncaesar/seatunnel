package org.apache.seatunnel.connectors.seatunnel.emqx.exception;

import org.apache.seatunnel.common.exception.SeaTunnelErrorCode;
import org.apache.seatunnel.common.exception.SeaTunnelRuntimeException;

import java.util.Map;

public class EmqxConnectorException extends SeaTunnelRuntimeException {
    public EmqxConnectorException(SeaTunnelErrorCode seaTunnelErrorCode, String errorMessage) {
        super(seaTunnelErrorCode, errorMessage);
    }

    public EmqxConnectorException(SeaTunnelErrorCode seaTunnelErrorCode, String errorMessage, Throwable cause) {
        super(seaTunnelErrorCode, errorMessage, cause);
    }

    public EmqxConnectorException(SeaTunnelErrorCode seaTunnelErrorCode, Throwable cause) {
        super(seaTunnelErrorCode, cause);
    }

    public EmqxConnectorException(SeaTunnelErrorCode seaTunnelErrorCode, Map<String, String> params) {
        super(seaTunnelErrorCode, params);
    }

    public EmqxConnectorException(SeaTunnelErrorCode seaTunnelErrorCode, Map<String, String> params, Throwable cause) {
        super(seaTunnelErrorCode, params, cause);
    }
}

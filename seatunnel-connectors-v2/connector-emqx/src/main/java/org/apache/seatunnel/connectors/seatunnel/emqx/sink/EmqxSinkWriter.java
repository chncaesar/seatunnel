package org.apache.seatunnel.connectors.seatunnel.emqx.sink;

import lombok.extern.slf4j.Slf4j;
import org.apache.seatunnel.api.configuration.ReadonlyConfig;
import org.apache.seatunnel.api.serialization.SerializationSchema;
import org.apache.seatunnel.api.table.type.SeaTunnelRow;
import org.apache.seatunnel.api.table.type.SeaTunnelRowType;
import org.apache.seatunnel.connectors.seatunnel.common.sink.AbstractSinkWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.seatunnel.connectors.seatunnel.emqx.config.EmqxOptions;
import org.apache.seatunnel.connectors.seatunnel.emqx.exception.EmqxConnectorErrorCode;
import org.apache.seatunnel.connectors.seatunnel.emqx.exception.EmqxConnectorException;
import org.apache.seatunnel.format.json.JsonSerializationSchema;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

@Slf4j
public class EmqxSinkWriter extends AbstractSinkWriter<SeaTunnelRow, Void> {

    protected final MqttClient mqttClient;
    protected final SeaTunnelRowType seaTunnelRowType;
    protected final SerializationSchema serializationSchema;

    private final int batchSize;
    private final List<SeaTunnelRow> batchBuffer;
    private final int qos;
    private final String topic;

    public EmqxSinkWriter(SeaTunnelRowType seaTunnelRowType, ReadonlyConfig config) {
        this.mqttClient = buildMqttClient(config);
        this.seaTunnelRowType = seaTunnelRowType;
        this.batchBuffer = new ArrayList<>();
        this.serializationSchema = new JsonSerializationSchema(seaTunnelRowType);
        this.qos = config.get(EmqxOptions.QOS);
        this.batchSize = config.get(EmqxOptions.BATCH_SIZE);
        this.topic = config.get(EmqxOptions.TOPIC);
    }

    private MqttClient buildMqttClient(ReadonlyConfig config) {

        String broker = config.get(EmqxOptions.BROKER);
        String clientId = config.get(EmqxOptions.CLIENT_ID);
        Boolean cleanSession = config.get(EmqxOptions.CLEAN_SESSION);
        Boolean automaticReconnect = config.get(EmqxOptions.AUTOMATIC_RECONNECT);
        Optional<String> username = config.getOptional(EmqxOptions.USERNAME);
        Optional<String> password = config.getOptional(EmqxOptions.PASSWORD);
        MqttClient client;
        try {
            // An instance of the default persistence mechanism MqttDefaultFilePersistence is used by the client.
            // @see org.eclipse.paho.client.mqttv3.MqttClient#MqttClient()
            client = new MqttClient(broker, clientId);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(automaticReconnect);
            options.setCleanSession(cleanSession);
            if(username.isPresent() && password.isPresent()) {
                options.setUserName(username.get());
                options.setPassword(password.get().toCharArray());
            }
            // 连接到 EMQX 服务器
            client.connect(options);
            log.info("Connected to EMQX broker at {}", broker);
        }
        catch (MqttException e) {
            log.error("Failed to create MqttClient", e);
            throw new EmqxConnectorException(EmqxConnectorErrorCode.FAILED_TO_CONNECT, e.getMessage());
        }
        return client;
    }


    @Override
    public void write(SeaTunnelRow element) throws IOException {
        if(element == null) {
            return;
        }
        batchBuffer.add(element);
        if (batchBuffer.size() >= batchSize) {
            sendBatch(false);
        }
    }

    private void sendBatch(boolean force) throws IOException {
        if (batchBuffer.isEmpty()) {
            return;
        }
        if (!force && batchBuffer.size() < batchSize) {
            log.debug("Batch size {} is not reached, current size: {}, skipping send", batchSize, batchBuffer.size());
            return;
        }
        try {
            for (SeaTunnelRow row : batchBuffer) {
                byte[] serialize = serializationSchema.serialize(row);
                String message = new String(serialize);
                mqttClient.publish(this.topic, message.getBytes(), this.qos, false);
            }
            log.info("sent {} messages to EMQX on topic {}", batchBuffer.size(), this.topic);
            batchBuffer.clear();
        } catch (MqttException e) {
            log.error("Failed to publish messages to EMQX", e);
            throw new EmqxConnectorException(EmqxConnectorErrorCode.FAILED_TO_PUBLISH, e.getMessage());
        }
    }

    @Override
    public void close() throws IOException {
        log.info("Closing EmqxSinkWriter, flushing remaining messages...");
        sendBatch(true);

        if(mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
                mqttClient.close();
            } catch (MqttException e) {
                log.error("Failed to close MqttClient", e);
                throw new EmqxConnectorException(EmqxConnectorErrorCode.FAILED_TO_CLOSE, e.getMessage());
            }
        }
    }
}

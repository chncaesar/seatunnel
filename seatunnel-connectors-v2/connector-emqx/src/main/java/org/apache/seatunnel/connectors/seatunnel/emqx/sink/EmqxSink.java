package org.apache.seatunnel.connectors.seatunnel.emqx.sink;

import org.apache.seatunnel.api.configuration.ReadonlyConfig;
import org.apache.seatunnel.api.sink.SinkWriter;
import org.apache.seatunnel.api.table.catalog.CatalogTable;
import org.apache.seatunnel.api.table.type.SeaTunnelRow;
import org.apache.seatunnel.api.table.type.SeaTunnelRowType;
import org.apache.seatunnel.connectors.seatunnel.common.sink.AbstractSimpleSink;

import java.io.IOException;
import java.util.Optional;


public class EmqxSink extends AbstractSimpleSink<SeaTunnelRow, Void> {

    private final CatalogTable catalogTable;

    private final ReadonlyConfig config;
    private final SeaTunnelRowType seaTunnelRowType;

    public EmqxSink(ReadonlyConfig pluginConfig, CatalogTable catalogTable) {
        this.config = pluginConfig;
        this.catalogTable = catalogTable;
        this.seaTunnelRowType = catalogTable.getSeaTunnelRowType();
    }

    @Override
    public EmqxSinkWriter createWriter(SinkWriter.Context context) throws IOException {
        return new EmqxSinkWriter(seaTunnelRowType, config);
    }

    @Override
    public String getPluginName() {
        return EmqxSinkFactory.IDENTIFIER;
    }

    @Override
    public Optional<CatalogTable> getWriteCatalogTable() {
        return Optional.ofNullable(catalogTable);
    }
}

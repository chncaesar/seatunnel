package org.apache.seatunnel.connectors.seatunnel.emqx.sink;

import com.google.auto.service.AutoService;
import org.apache.seatunnel.api.configuration.util.OptionRule;
import org.apache.seatunnel.api.table.connector.TableSink;
import org.apache.seatunnel.api.table.factory.Factory;
import org.apache.seatunnel.api.table.factory.TableSinkFactory;
import org.apache.seatunnel.api.table.factory.TableSinkFactoryContext;
import org.apache.seatunnel.connectors.seatunnel.emqx.config.EmqxOptions;


@AutoService(Factory.class)
public class EmqxSinkFactory implements TableSinkFactory {
    public static final String IDENTIFIER = "Emqx";
    @Override
    public String factoryIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public OptionRule optionRule() {
        return OptionRule.builder()
                .required(EmqxOptions.TOPIC,
                        EmqxOptions.BROKER,
                        EmqxOptions.CLIENT_ID)
                .optional(EmqxOptions.USERNAME,
                        EmqxOptions.PASSWORD,
                        EmqxOptions.QOS,
                        EmqxOptions.CLEAN_SESSION,
                        EmqxOptions.AUTOMATIC_RECONNECT,
                        EmqxOptions.BATCH_SIZE)
                .build();
    }

    @Override
    public TableSink createSink(TableSinkFactoryContext context) {
        return () -> new EmqxSink(context.getOptions(), context.getCatalogTable());
    }


}

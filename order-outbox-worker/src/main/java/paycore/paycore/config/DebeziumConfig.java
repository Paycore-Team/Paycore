package paycore.paycore.config;

import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.debezium.inbound.DebeziumMessageProducer;
import org.springframework.messaging.MessageChannel;

@Configuration
@RequiredArgsConstructor
public class DebeziumConfig {
    @Bean
    public MessageChannel debeziumInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer debeziumMessageProducer(
            DebeziumEngine.Builder<ChangeEvent<byte[], byte[]>> debeziumEngineBuilder,
            MessageChannel debeziumInputChannel
    ) {
        DebeziumMessageProducer debeziumMessageProducer = new DebeziumMessageProducer(debeziumEngineBuilder);
        debeziumMessageProducer.setOutputChannel(debeziumInputChannel);

        return debeziumMessageProducer;
    }
}
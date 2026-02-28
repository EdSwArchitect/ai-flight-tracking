package com.militarytracker.svc.publisher;

import com.militarytracker.common.json.JsonMapper;
import com.militarytracker.model.api.AcItem;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Future;

public class FlightKafkaPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(FlightKafkaPublisher.class);

    private final KafkaProducer<String, String> producer;
    private final String topic;

    public FlightKafkaPublisher(KafkaProducer<String, String> producer, String topic) {
        this.producer = producer;
        this.topic = topic;
    }

    public int publish(List<AcItem> items) {
        int successCount = 0;
        for (AcItem item : items) {
            try {
                String jsonValue = JsonMapper.get().writeValueAsString(item);
                String key = item.getHex();

                ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, jsonValue);
                Future<RecordMetadata> future = producer.send(record, (metadata, exception) -> {
                    if (exception != null) {
                        LOG.error("Failed to send record for hex={}: {}", item.getHex(), exception.getMessage());
                    } else {
                        LOG.trace("Sent record for hex={} to partition={} offset={}",
                                item.getHex(), metadata.partition(), metadata.offset());
                    }
                });
                successCount++;
            } catch (Exception e) {
                LOG.error("Error serializing AcItem hex={}: {}", item.getHex(), e.getMessage());
            }
        }
        producer.flush();
        return successCount;
    }

    public void close() {
        producer.close();
    }
}

package com.militarytracker.dbingestor.consumer;

import com.militarytracker.common.json.JsonMapper;
import com.militarytracker.dbingestor.metrics.IngestorMetrics;
import com.militarytracker.dbingestor.service.IngestionService;
import com.militarytracker.model.api.AcItem;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

public class FlightKafkaConsumer implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(FlightKafkaConsumer.class);

    private final KafkaConsumer<String, String> consumer;
    private final IngestionService ingestionService;
    private final String topic;
    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    public FlightKafkaConsumer(KafkaConsumer<String, String> consumer,
                                IngestionService ingestionService,
                                String topic) {
        this.consumer = consumer;
        this.ingestionService = ingestionService;
        this.topic = topic;
    }

    @Override
    public void run() {
        consumer.subscribe(Collections.singletonList(topic));
        LOG.info("Subscribed to topic: {}", topic);

        try {
            while (!shutdown.get()) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> record : records) {
                    try {
                        IngestorMetrics.RECORDS_READ_FROM_KAFKA.inc();
                        AcItem item = JsonMapper.get().readValue(record.value(), AcItem.class);
                        ingestionService.ingest(item);
                    } catch (Exception e) {
                        LOG.error("Error processing record at offset={}: {}", record.offset(), e.getMessage());
                        IngestorMetrics.RECORDS_FAILED.inc();
                    }
                }
                if (!records.isEmpty()) {
                    consumer.commitSync();
                }
            }
        } finally {
            consumer.close();
            LOG.info("Kafka consumer closed");
        }
    }

    public void shutdown() {
        shutdown.set(true);
    }
}

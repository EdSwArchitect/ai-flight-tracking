package com.militarytracker.dbingestor.consumer;

import com.militarytracker.dbingestor.service.IngestionService;
import com.militarytracker.model.api.AcItem;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightKafkaConsumerTest {

    @Mock
    private KafkaConsumer<String, String> kafkaConsumer;

    @Mock
    private IngestionService ingestionService;

    @Test
    void shouldProcessRecordsAndCommit() {
        String json = "{\"hex\":\"AE1234\",\"lat\":38.0,\"lon\":-77.0,\"alt_baro\":35000}";
        TopicPartition tp = new TopicPartition("military_flights", 0);
        ConsumerRecord<String, String> record = new ConsumerRecord<>("military_flights", 0, 0, "AE1234", json);
        ConsumerRecords<String, String> records = new ConsumerRecords<>(Map.of(tp, List.of(record)));
        ConsumerRecords<String, String> emptyRecords = new ConsumerRecords<>(Map.of());

        when(kafkaConsumer.poll(any(Duration.class)))
                .thenReturn(records)
                .thenReturn(emptyRecords);

        FlightKafkaConsumer consumer = new FlightKafkaConsumer(kafkaConsumer, ingestionService, "military_flights");

        // Run in a separate thread and stop after a short time
        Thread thread = new Thread(consumer);
        thread.start();

        // Give it time to process
        try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        consumer.shutdown();
        try { thread.join(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        verify(ingestionService, atLeastOnce()).ingest(any(AcItem.class));
        verify(kafkaConsumer, atLeastOnce()).commitSync();
    }
}

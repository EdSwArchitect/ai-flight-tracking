package com.militarytracker.svc.publisher;

import com.militarytracker.model.api.AcItem;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightKafkaPublisherTest {

    @Mock
    private KafkaProducer<String, String> mockProducer;

    private FlightKafkaPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new FlightKafkaPublisher(mockProducer, "military_flights");
    }

    @Test
    void shouldPublishAllItemsToKafka() {
        AcItem item1 = new AcItem();
        item1.setHex("AE1234");
        item1.setLat(38.0);
        item1.setLon(-77.0);

        AcItem item2 = new AcItem();
        item2.setHex("AE5678");
        item2.setLat(39.0);
        item2.setLon(-76.0);

        when(mockProducer.send(any(), any())).thenReturn(null);

        int count = publisher.publish(List.of(item1, item2));

        assertEquals(2, count);
        verify(mockProducer, times(2)).send(any(ProducerRecord.class), any());
        verify(mockProducer).flush();
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldUseHexAsKafkaKey() {
        AcItem item = new AcItem();
        item.setHex("AE1234");
        item.setLat(38.0);
        item.setLon(-77.0);

        when(mockProducer.send(any(), any())).thenReturn(null);

        publisher.publish(List.of(item));

        ArgumentCaptor<ProducerRecord<String, String>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(mockProducer).send(captor.capture(), any());

        ProducerRecord<String, String> record = captor.getValue();
        assertEquals("military_flights", record.topic());
        assertEquals("AE1234", record.key());
        assertNotNull(record.value());
        assertTrue(record.value().contains("AE1234"));
    }

    @Test
    void shouldHandleEmptyList() {
        int count = publisher.publish(List.of());

        assertEquals(0, count);
        verify(mockProducer, never()).send(any(), any());
        verify(mockProducer).flush();
    }
}

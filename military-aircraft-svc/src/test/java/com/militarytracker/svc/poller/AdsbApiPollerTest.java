package com.militarytracker.svc.poller;

import com.militarytracker.model.api.AcItem;
import com.militarytracker.model.api.V2Response;
import com.militarytracker.svc.publisher.FlightKafkaPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdsbApiPollerTest {

    @Mock
    private AdsbApiClient mockApiClient;

    @Mock
    private FlightKafkaPublisher mockPublisher;

    private AdsbApiPoller poller;

    @BeforeEach
    void setUp() {
        poller = new AdsbApiPoller(mockApiClient, mockPublisher);
    }

    @Test
    void shouldFetchAndPublishOnSuccess() throws Exception {
        AcItem item = new AcItem();
        item.setHex("AE1234");

        V2Response response = new V2Response(1709052000L, 1, List.of(item));
        when(mockApiClient.fetchMilitaryAircraft()).thenReturn(response);
        when(mockPublisher.publish(anyList())).thenReturn(1);

        poller.run();

        verify(mockApiClient).fetchMilitaryAircraft();
        verify(mockPublisher).publish(List.of(item));
    }

    @Test
    void shouldNotPublishWhenNoAircraft() throws Exception {
        V2Response response = new V2Response(1709052000L, 0, List.of());
        when(mockApiClient.fetchMilitaryAircraft()).thenReturn(response);

        poller.run();

        verify(mockApiClient).fetchMilitaryAircraft();
        verify(mockPublisher, never()).publish(anyList());
    }

    @Test
    void shouldHandleApiException() throws Exception {
        when(mockApiClient.fetchMilitaryAircraft()).thenThrow(new AdsbApiException("API error"));

        poller.run(); // should not throw

        verify(mockApiClient).fetchMilitaryAircraft();
        verify(mockPublisher, never()).publish(anyList());
    }
}

package com.militarytracker.api.handler;

import com.militarytracker.api.repository.FlightReadRepository;
import com.militarytracker.model.dto.FlightSummaryDto;
import com.militarytracker.model.dto.GeoBoxRequest;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeoBoxFlightHandlerTest {

    @Mock
    private FlightReadRepository repository;

    @Mock
    private Context ctx;

    private GeoBoxFlightHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GeoBoxFlightHandler(repository);
    }

    @Test
    void handle_validRequest_returnFlights() throws Exception {
        GeoBoxRequest request = new GeoBoxRequest(50.0, 40.0, -70.0, -80.0);

        FlightSummaryDto dto = new FlightSummaryDto();
        dto.setId(1L);
        dto.setHexIcao("AE1234");

        when(ctx.bodyAsClass(GeoBoxRequest.class)).thenReturn(request);
        when(repository.findWithinBoundingBox(50.0, 40.0, -70.0, -80.0))
                .thenReturn(List.of(dto));

        handler.handle(ctx);

        verify(repository).findWithinBoundingBox(50.0, 40.0, -70.0, -80.0);
        verify(ctx).json(List.of(dto));
    }

    @Test
    void handle_invalidBoundingBox_returns400() throws Exception {
        // north < south is invalid
        GeoBoxRequest request = new GeoBoxRequest(30.0, 50.0, -70.0, -80.0);

        when(ctx.bodyAsClass(GeoBoxRequest.class)).thenReturn(request);
        when(ctx.status(400)).thenReturn(ctx);

        handler.handle(ctx);

        verify(ctx).status(400);
        verify(ctx).json(Map.of("error", "Invalid bounding box coordinates"));
    }

    @Test
    void handle_invalidBody_returns400() throws Exception {
        when(ctx.bodyAsClass(GeoBoxRequest.class)).thenThrow(new RuntimeException("Bad JSON"));
        when(ctx.status(400)).thenReturn(ctx);

        handler.handle(ctx);

        verify(ctx).status(400);
        verify(ctx).json(Map.of("error", "Invalid request body"));
    }

    @Test
    void handle_repositoryThrows_returns500() throws Exception {
        GeoBoxRequest request = new GeoBoxRequest(50.0, 40.0, -70.0, -80.0);

        when(ctx.bodyAsClass(GeoBoxRequest.class)).thenReturn(request);
        when(repository.findWithinBoundingBox(50.0, 40.0, -70.0, -80.0))
                .thenThrow(new RuntimeException("DB error"));
        when(ctx.status(500)).thenReturn(ctx);

        handler.handle(ctx);

        verify(ctx).status(500);
    }
}

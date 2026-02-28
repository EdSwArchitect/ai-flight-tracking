package com.militarytracker.api.handler;

import com.militarytracker.api.repository.FlightReadRepository;
import com.militarytracker.model.dto.FlightSummaryDto;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListFlightsHandlerTest {

    @Mock
    private FlightReadRepository repository;

    @Mock
    private Context ctx;

    private ListFlightsHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ListFlightsHandler(repository);
    }

    @Test
    void handle_defaultLimitAndOffset_returnsFlights() throws Exception {
        FlightSummaryDto dto = new FlightSummaryDto();
        dto.setId(1L);
        dto.setHexIcao("AE1234");
        dto.setFlight("TEST01");

        when(ctx.queryParam("limit")).thenReturn(null);
        when(ctx.queryParam("offset")).thenReturn(null);
        when(repository.listFlights(100, 0)).thenReturn(List.of(dto));

        handler.handle(ctx);

        verify(repository).listFlights(100, 0);
        verify(ctx).json(List.of(dto));
    }

    @Test
    void handle_customLimitAndOffset_passesToRepository() throws Exception {
        when(ctx.queryParam("limit")).thenReturn("50");
        when(ctx.queryParam("offset")).thenReturn("10");
        when(repository.listFlights(50, 10)).thenReturn(List.of());

        handler.handle(ctx);

        verify(repository).listFlights(50, 10);
        verify(ctx).json(List.of());
    }

    @Test
    void handle_invalidLimit_usesDefault() throws Exception {
        when(ctx.queryParam("limit")).thenReturn("abc");
        when(ctx.queryParam("offset")).thenReturn("0");
        when(repository.listFlights(100, 0)).thenReturn(List.of());

        handler.handle(ctx);

        verify(repository).listFlights(100, 0);
    }

    @Test
    void handle_repositoryThrows_returns500() throws Exception {
        when(ctx.queryParam("limit")).thenReturn(null);
        when(ctx.queryParam("offset")).thenReturn(null);
        when(repository.listFlights(anyInt(), anyInt())).thenThrow(new RuntimeException("DB error"));
        when(ctx.status(500)).thenReturn(ctx);

        handler.handle(ctx);

        verify(ctx).status(500);
    }
}

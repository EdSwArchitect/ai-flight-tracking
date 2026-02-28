package com.militarytracker.api.handler;

import com.militarytracker.api.repository.FlightReadRepository;
import com.militarytracker.model.dto.FlightSummaryDto;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetFlightHandlerTest {

    @Mock
    private FlightReadRepository repository;

    @Mock
    private Context ctx;

    private GetFlightHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetFlightHandler(repository);
    }

    @Test
    void handle_existingFlight_returnsDto() throws Exception {
        FlightSummaryDto dto = new FlightSummaryDto();
        dto.setId(42L);
        dto.setHexIcao("AE1234");
        dto.setFlight("TEST01");

        when(ctx.pathParam("id")).thenReturn("42");
        when(repository.getFlightById(42L)).thenReturn(dto);

        handler.handle(ctx);

        verify(repository).getFlightById(42L);
        verify(ctx).json(dto);
    }

    @Test
    void handle_nonExistentFlight_returns404() throws Exception {
        when(ctx.pathParam("id")).thenReturn("999");
        when(repository.getFlightById(999L)).thenReturn(null);
        when(ctx.status(404)).thenReturn(ctx);

        handler.handle(ctx);

        verify(ctx).status(404);
        verify(ctx).json(Map.of("error", "Flight not found: 999"));
    }

    @Test
    void handle_invalidId_returns400() throws Exception {
        when(ctx.pathParam("id")).thenReturn("not-a-number");
        when(ctx.status(400)).thenReturn(ctx);

        handler.handle(ctx);

        verify(ctx).status(400);
    }

    @Test
    void handle_repositoryThrows_returns500() throws Exception {
        when(ctx.pathParam("id")).thenReturn("1");
        when(repository.getFlightById(1L)).thenThrow(new RuntimeException("DB error"));
        when(ctx.status(500)).thenReturn(ctx);

        handler.handle(ctx);

        verify(ctx).status(500);
    }
}

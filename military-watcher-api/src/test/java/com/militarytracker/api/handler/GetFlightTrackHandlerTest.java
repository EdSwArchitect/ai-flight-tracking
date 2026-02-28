package com.militarytracker.api.handler;

import com.militarytracker.api.repository.FlightReadRepository;
import com.militarytracker.model.dto.TrackPointDto;
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
class GetFlightTrackHandlerTest {

    @Mock
    private FlightReadRepository repository;

    @Mock
    private Context ctx;

    private GetFlightTrackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetFlightTrackHandler(repository);
    }

    @Test
    void handle_existingTrack_returnsPoints() throws Exception {
        TrackPointDto p1 = new TrackPointDto();
        p1.setId(1L);
        p1.setLat(38.0);
        p1.setLon(-97.0);

        TrackPointDto p2 = new TrackPointDto();
        p2.setId(2L);
        p2.setLat(38.5);
        p2.setLon(-97.5);

        when(ctx.pathParam("id")).thenReturn("42");
        when(repository.getFlightTrack(42L)).thenReturn(List.of(p1, p2));

        handler.handle(ctx);

        verify(repository).getFlightTrack(42L);
        verify(ctx).json(List.of(p1, p2));
    }

    @Test
    void handle_noTrack_returns404() throws Exception {
        when(ctx.pathParam("id")).thenReturn("999");
        when(repository.getFlightTrack(999L)).thenReturn(List.of());
        when(ctx.status(404)).thenReturn(ctx);

        handler.handle(ctx);

        verify(ctx).status(404);
        verify(ctx).json(Map.of("error", "No track found for flight: 999"));
    }

    @Test
    void handle_invalidId_returns400() throws Exception {
        when(ctx.pathParam("id")).thenReturn("abc");
        when(ctx.status(400)).thenReturn(ctx);

        handler.handle(ctx);

        verify(ctx).status(400);
    }

    @Test
    void handle_repositoryThrows_returns500() throws Exception {
        when(ctx.pathParam("id")).thenReturn("1");
        when(repository.getFlightTrack(1L)).thenThrow(new RuntimeException("DB error"));
        when(ctx.status(500)).thenReturn(ctx);

        handler.handle(ctx);

        verify(ctx).status(500);
    }
}

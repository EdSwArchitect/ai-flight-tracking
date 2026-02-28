package com.militarytracker.svc.poller;

import com.militarytracker.model.api.V2Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdsbApiClientTest {

    @SuppressWarnings("unchecked")
    @Test
    void shouldDeserializeSuccessfulResponse() throws Exception {
        String jsonResponse = new String(
                getClass().getResourceAsStream("/sample-v2-response.json").readAllBytes());

        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        AdsbApiClient client = new AdsbApiClient("https://api.adsb.lol/v2/mil", mockClient);
        V2Response response = client.fetchMilitaryAircraft();

        assertNotNull(response);
        assertEquals(2, response.getTotal());
        assertEquals(2, response.getAc().size());
        assertEquals("AE1234", response.getAc().get(0).getHex());
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldThrowOnNon200Status() throws Exception {
        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(500);
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        AdsbApiClient client = new AdsbApiClient("https://api.adsb.lol/v2/mil", mockClient);

        assertThrows(AdsbApiException.class, client::fetchMilitaryAircraft);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldSendAcceptJsonHeader() throws Exception {
        String jsonResponse = "{\"now\":0,\"total\":0,\"ac\":[]}";

        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        AdsbApiClient client = new AdsbApiClient("https://api.adsb.lol/v2/mil", mockClient);
        client.fetchMilitaryAircraft();

        verify(mockClient).send(argThat(request ->
                request.headers().firstValue("Accept").orElse("").equals("application/json")
        ), any());
    }
}

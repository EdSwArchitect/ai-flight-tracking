package com.militarytracker.svc.poller;

import com.militarytracker.common.json.JsonMapper;
import com.militarytracker.model.api.V2Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class AdsbApiClient {

    private static final Logger LOG = LoggerFactory.getLogger(AdsbApiClient.class);

    private final HttpClient httpClient;
    private final String apiUrl;

    public AdsbApiClient(String apiUrl) {
        this.apiUrl = apiUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public AdsbApiClient(String apiUrl, HttpClient httpClient) {
        this.apiUrl = apiUrl;
        this.httpClient = httpClient;
    }

    public V2Response fetchMilitaryAircraft() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        LOG.debug("Fetching military aircraft data from {}", apiUrl);

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new AdsbApiException("ADS-B API returned status: " + response.statusCode());
        }

        return JsonMapper.get().readValue(response.body(), V2Response.class);
    }
}

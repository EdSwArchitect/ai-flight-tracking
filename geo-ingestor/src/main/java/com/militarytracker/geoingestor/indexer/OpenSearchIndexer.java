package com.militarytracker.geoingestor.indexer;

import java.io.StringReader;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.IndexResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.json.Json;
import jakarta.json.JsonObject;

/**
 * Indexes GeoJSON documents into OpenSearch.
 */
@Component
public class OpenSearchIndexer {

    private static final Logger log = LoggerFactory.getLogger(OpenSearchIndexer.class);

    private final OpenSearchClient client;
    private final String indexName;

    public OpenSearchIndexer(OpenSearchClient client,
                             @Value("${opensearch.index}") String indexName) {
        this.client = client;
        this.indexName = indexName;
    }

    /**
     * Indexes a GeoJSON document into OpenSearch.
     *
     * @param hex     the aircraft hex identifier
     * @param geoJson the GeoJSON Feature string to index
     * @return true if indexing succeeded, false otherwise
     */
    public boolean index(String hex, String geoJson) {
        try {
            String documentId = hex + "_" + System.currentTimeMillis();

            JsonObject jsonObject = Json.createReader(new StringReader(geoJson)).readObject();

            IndexRequest<JsonObject> request = new IndexRequest.Builder<JsonObject>()
                    .index(indexName)
                    .id(documentId)
                    .document(jsonObject)
                    .build();

            IndexResponse response = client.index(request);
            log.debug("Indexed document id={} result={}", documentId, response.result());
            return true;
        } catch (Exception e) {
            log.error("Failed to index GeoJSON document for hex={}", hex, e);
            return false;
        }
    }
}

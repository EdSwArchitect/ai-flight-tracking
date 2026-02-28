package com.militarytracker.geoingestor.converter;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.militarytracker.model.api.AcItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Converts an {@link AcItem} to a GeoJSON Feature string using Jackson.
 * <p>
 * Output format:
 * <pre>
 * {
 *   "type": "Feature",
 *   "geometry": {
 *     "type": "Point",
 *     "coordinates": [lon, lat, alt]
 *   },
 *   "properties": {
 *     "hex": "...",
 *     "flight": "...",
 *     "type": "...",
 *     "alt_baro": ...,
 *     "gs": ...,
 *     "track": ...,
 *     "timestamp": "..."
 *   }
 * }
 * </pre>
 */
@Component
public class GeoJsonConverter {

    private static final Logger log = LoggerFactory.getLogger(GeoJsonConverter.class);

    private final ObjectMapper objectMapper;

    public GeoJsonConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Converts an AcItem to a GeoJSON Feature JSON string.
     *
     * @param item the aircraft item with lat/lon populated
     * @return GeoJSON Feature string, or null if conversion fails
     */
    public String convert(AcItem item) {
        try {
            Map<String, Object> feature = new LinkedHashMap<>();
            feature.put("type", "Feature");

            // Geometry
            Map<String, Object> geometry = new LinkedHashMap<>();
            geometry.put("type", "Point");

            double lon = item.getLon();
            double lat = item.getLat();
            double alt = item.getAltBaroFeet() != null ? item.getAltBaroFeet().doubleValue() : 0.0;
            geometry.put("coordinates", new double[]{lon, lat, alt});

            feature.put("geometry", geometry);

            // Properties
            Map<String, Object> properties = new LinkedHashMap<>();
            properties.put("hex", item.getHex());
            properties.put("flight", item.getFlight());
            properties.put("type", item.getAircraftType());
            properties.put("alt_baro", item.getAltBaroFeet());
            properties.put("gs", item.getGroundSpeed());
            properties.put("track", item.getTrack());
            properties.put("timestamp", Instant.now().toString());

            feature.put("properties", properties);

            return objectMapper.writeValueAsString(feature);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert AcItem to GeoJSON: hex={}", item.getHex(), e);
            return null;
        }
    }
}

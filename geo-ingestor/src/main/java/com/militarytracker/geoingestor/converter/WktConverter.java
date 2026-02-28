package com.militarytracker.geoingestor.converter;

import com.militarytracker.model.api.AcItem;
import org.springframework.stereotype.Component;

/**
 * Converts an {@link AcItem} to a WKT (Well-Known Text) POINT Z string.
 * <p>
 * Output format: {@code POINT Z(lon lat alt)}
 */
@Component
public class WktConverter {

    /**
     * Converts an AcItem to a WKT POINT Z string.
     *
     * @param item the aircraft item with lat/lon populated
     * @return WKT string in format "POINT Z(lon lat alt)"
     */
    public String convert(AcItem item) {
        double lon = item.getLon();
        double lat = item.getLat();
        double alt = item.getAltBaroFeet() != null ? item.getAltBaroFeet().doubleValue() : 0.0;

        return String.format("POINT Z(%s %s %s)", lon, lat, alt);
    }
}

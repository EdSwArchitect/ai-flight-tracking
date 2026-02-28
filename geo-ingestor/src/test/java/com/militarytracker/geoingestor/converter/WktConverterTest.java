package com.militarytracker.geoingestor.converter;

import com.militarytracker.model.api.AcItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WktConverterTest {

    private WktConverter converter;

    @BeforeEach
    void setUp() {
        converter = new WktConverter();
    }

    @Test
    void convertShouldProducePointZ() {
        AcItem item = new AcItem();
        item.setLat(38.8977);
        item.setLon(-77.0365);
        item.setAltBaro(35000);

        String wkt = converter.convert(item);
        assertEquals("POINT Z(-77.0365 38.8977 35000.0)", wkt);
    }

    @Test
    void convertShouldHandleOnGround() {
        AcItem item = new AcItem();
        item.setLat(40.0);
        item.setLon(-74.0);
        item.setAltBaro("ground");

        String wkt = converter.convert(item);
        assertEquals("POINT Z(-74.0 40.0 0.0)", wkt);
    }

    @Test
    void convertShouldHandleNullAltitude() {
        AcItem item = new AcItem();
        item.setLat(35.0);
        item.setLon(-80.0);

        String wkt = converter.convert(item);
        assertEquals("POINT Z(-80.0 35.0 0.0)", wkt);
    }
}

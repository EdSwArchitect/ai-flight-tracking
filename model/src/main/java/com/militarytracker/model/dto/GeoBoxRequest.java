package com.militarytracker.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoBoxRequest {

    private double north;
    private double south;
    private double east;
    private double west;

    public GeoBoxRequest() {
    }

    public GeoBoxRequest(double north, double south, double east, double west) {
        this.north = north;
        this.south = south;
        this.east = east;
        this.west = west;
    }

    public double getNorth() {
        return north;
    }

    public void setNorth(double north) {
        this.north = north;
    }

    public double getSouth() {
        return south;
    }

    public void setSouth(double south) {
        this.south = south;
    }

    public double getEast() {
        return east;
    }

    public void setEast(double east) {
        this.east = east;
    }

    public double getWest() {
        return west;
    }

    public void setWest(double west) {
        this.west = west;
    }

    public boolean isValid() {
        return north >= -90 && north <= 90
                && south >= -90 && south <= 90
                && east >= -180 && east <= 180
                && west >= -180 && west <= 180
                && north > south;
    }
}

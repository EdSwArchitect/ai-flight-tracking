package com.militarytracker.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FlightSummaryDto {

    private long id;
    private String hexIcao;
    private String aircraftType;
    private String flight;
    private Integer altBaro;
    private Double groundSpeed;
    private Double track;
    private Double lat;
    private Double lon;
    private Integer altGeom;
    private boolean onGround;
    private String seenAt;

    public FlightSummaryDto() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getHexIcao() {
        return hexIcao;
    }

    public void setHexIcao(String hexIcao) {
        this.hexIcao = hexIcao;
    }

    public String getAircraftType() {
        return aircraftType;
    }

    public void setAircraftType(String aircraftType) {
        this.aircraftType = aircraftType;
    }

    public String getFlight() {
        return flight;
    }

    public void setFlight(String flight) {
        this.flight = flight;
    }

    public Integer getAltBaro() {
        return altBaro;
    }

    public void setAltBaro(Integer altBaro) {
        this.altBaro = altBaro;
    }

    public Double getGroundSpeed() {
        return groundSpeed;
    }

    public void setGroundSpeed(Double groundSpeed) {
        this.groundSpeed = groundSpeed;
    }

    public Double getTrack() {
        return track;
    }

    public void setTrack(Double track) {
        this.track = track;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Integer getAltGeom() {
        return altGeom;
    }

    public void setAltGeom(Integer altGeom) {
        this.altGeom = altGeom;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public String getSeenAt() {
        return seenAt;
    }

    public void setSeenAt(String seenAt) {
        this.seenAt = seenAt;
    }
}

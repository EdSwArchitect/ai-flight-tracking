package com.militarytracker.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TrackPointDto {

    private long id;
    @JsonProperty("latitude")
    private double lat;
    @JsonProperty("longitude")
    private double lon;
    private Integer altBaro;
    private Double groundSpeed;
    private Double track;
    @JsonProperty("timestamp")
    private String seenAt;

    public TrackPointDto() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
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

    public String getSeenAt() {
        return seenAt;
    }

    public void setSeenAt(String seenAt) {
        this.seenAt = seenAt;
    }
}

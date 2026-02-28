package com.militarytracker.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FlightDetailDto {

    private long id;
    private String hexIcao;
    private String registration;
    private String aircraftType;
    private String description;
    private String operator;
    private String country;
    private String flight;
    private Integer altBaro;
    private Integer altGeom;
    private Double groundSpeed;
    private Double track;
    private Integer verticalRate;
    private String squawk;
    private String category;
    private Double lat;
    private Double lon;
    private boolean onGround;
    private String seenAt;

    public FlightDetailDto() {
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

    public String getRegistration() {
        return registration;
    }

    public void setRegistration(String registration) {
        this.registration = registration;
    }

    public String getAircraftType() {
        return aircraftType;
    }

    public void setAircraftType(String aircraftType) {
        this.aircraftType = aircraftType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
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

    public Integer getAltGeom() {
        return altGeom;
    }

    public void setAltGeom(Integer altGeom) {
        this.altGeom = altGeom;
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

    public Integer getVerticalRate() {
        return verticalRate;
    }

    public void setVerticalRate(Integer verticalRate) {
        this.verticalRate = verticalRate;
    }

    public String getSquawk() {
        return squawk;
    }

    public void setSquawk(String squawk) {
        this.squawk = squawk;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

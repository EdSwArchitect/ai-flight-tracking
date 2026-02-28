package com.militarytracker.model.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AcItem {

    @JsonProperty("hex")
    private String hex;

    @JsonProperty("flight")
    private String flight;

    @JsonProperty("r")
    private String registration;

    @JsonProperty("t")
    private String aircraftType;

    @JsonProperty("desc")
    private String description;

    @JsonProperty("ownOp")
    private String operator;

    @JsonProperty("lat")
    private Double lat;

    @JsonProperty("lon")
    private Double lon;

    @JsonProperty("alt_baro")
    private Object altBaro;

    @JsonProperty("alt_geom")
    private Integer altGeom;

    @JsonProperty("gs")
    private Double groundSpeed;

    @JsonProperty("track")
    private Double track;

    @JsonProperty("baro_rate")
    private Integer verticalRate;

    @JsonProperty("squawk")
    private String squawk;

    @JsonProperty("category")
    private String category;

    @JsonProperty("nav_altitude_mcp")
    private Integer navAltitudeMcp;

    @JsonProperty("nav_heading")
    private Double navHeading;

    @JsonProperty("dbFlags")
    private Integer dbFlags;

    public AcItem() {
    }

    public String getHex() {
        return hex;
    }

    public void setHex(String hex) {
        this.hex = hex;
    }

    public String getFlight() {
        return flight != null ? flight.trim() : null;
    }

    public void setFlight(String flight) {
        this.flight = flight;
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

    public Object getAltBaro() {
        return altBaro;
    }

    public void setAltBaro(Object altBaro) {
        this.altBaro = altBaro;
    }

    /**
     * Returns barometric altitude in feet, or null if on ground or unavailable.
     */
    public Integer getAltBaroFeet() {
        if (altBaro instanceof Number) {
            return ((Number) altBaro).intValue();
        }
        return null;
    }

    /**
     * Returns true if the aircraft is reporting "ground" for barometric altitude.
     */
    public boolean isOnGround() {
        return "ground".equals(altBaro);
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

    public Integer getNavAltitudeMcp() {
        return navAltitudeMcp;
    }

    public void setNavAltitudeMcp(Integer navAltitudeMcp) {
        this.navAltitudeMcp = navAltitudeMcp;
    }

    public Double getNavHeading() {
        return navHeading;
    }

    public void setNavHeading(Double navHeading) {
        this.navHeading = navHeading;
    }

    public Integer getDbFlags() {
        return dbFlags;
    }

    public void setDbFlags(Integer dbFlags) {
        this.dbFlags = dbFlags;
    }
}

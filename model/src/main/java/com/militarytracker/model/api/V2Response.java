package com.militarytracker.model.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class V2Response {

    @JsonProperty("now")
    private long now;

    @JsonProperty("total")
    private int total;

    @JsonProperty("ac")
    private List<AcItem> ac;

    public V2Response() {
    }

    public V2Response(long now, int total, List<AcItem> ac) {
        this.now = now;
        this.total = total;
        this.ac = ac;
    }

    public long getNow() {
        return now;
    }

    public void setNow(long now) {
        this.now = now;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<AcItem> getAc() {
        return ac;
    }

    public void setAc(List<AcItem> ac) {
        this.ac = ac;
    }
}
